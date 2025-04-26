package com.campus.forum.service.impl;

import com.campus.forum.entity.Activity;
import com.campus.forum.entity.UserBehavior;
import com.campus.forum.mapper.ActivityMapper;
import com.campus.forum.mapper.UserBehaviorMapper;
import com.campus.forum.service.RecommendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 协同过滤推荐服务（Item-based CF）
 *
 * 算法说明：
 * 1. 构建用户-物品评分矩阵（从 user_behavior 表）
 * 2. 计算物品相似度（余弦相似度）
 * 3. 对目标用户预测候选物品评分
 * 4. 取 TopN，冷启动兜底热门内容
 *
 * 踩坑记录：
 * - AI 生成的余弦相似度代码未处理向量模为0的情况，会产生 NaN，需要加除零保护
 * - 新用户无行为时直接返回热门内容兜底
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendServiceImpl implements RecommendService {

    private final UserBehaviorMapper behaviorMapper;
    private final ActivityMapper activityMapper;

    @Override
    public List<Activity> recommendActivities(Long userId, int topN) {
        // Step1: 获取目标用户的行为记录
        List<UserBehavior> userBehaviors = behaviorMapper.selectByUserIdAndType(userId, "activity");
        if (userBehaviors.isEmpty()) {
            // 冷启动兜底：返回近期热门活动
            log.info("用户 {} 无行为记录，返回热门活动（冷启动兜底）", userId);
            return activityMapper.selectHotActivities(topN);
        }

        // Step2: 构建目标用户的交互活动集和评分
        Map<Long, Double> userScores = userBehaviors.stream().collect(
                Collectors.groupingBy(UserBehavior::getTargetId,
                        Collectors.summingDouble(b -> b.getScore()))
        );
        Set<Long> interactedIds = userScores.keySet();

        // Step3: 获取候选活动（未交互过的）
        List<Activity> candidates = activityMapper.selectExcludingInteracted(userId, topN * 5);
        if (candidates.isEmpty()) {
            return activityMapper.selectHotActivities(topN);
        }

        // Step4: 对每个候选活动计算预测评分
        Map<Long, Double> predScores = new HashMap<>();
        for (Activity candidate : candidates) {
            double numerator = 0.0, denominator = 0.0;
            for (Long iId : interactedIds) {
                double sim = computeCosineSim(iId, candidate.getId());
                if (sim > 0) {
                    numerator += sim * userScores.get(iId);
                    denominator += Math.abs(sim);
                }
            }
            // 关键：除零保护（AI 常见遗漏点）
            if (denominator > 0) {
                predScores.put(candidate.getId(), numerator / denominator);
            }
        }

        // Step5: 按预测分降序排序，取 TopN
        return candidates.stream()
                .filter(a -> predScores.containsKey(a.getId()))
                .sorted((a, b) -> Double.compare(
                        predScores.getOrDefault(b.getId(), 0.0),
                        predScores.getOrDefault(a.getId(), 0.0)))
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * 计算两个活动之间的余弦相似度
     * 基于共同交互用户的行为分值
     * 注意：至少需要2个共同交互用户才有意义，避免噪声
     */
    private double computeCosineSim(Long itemA, Long itemB) {
        // 简化实现：基于活动的点赞数/浏览数的向量相似度
        // 实际生产环境应基于用户行为矩阵计算
        Activity a = activityMapper.selectById(itemA);
        Activity b = activityMapper.selectById(itemB);
        if (a == null || b == null) return 0.0;

        double[] vecA = {a.getLikeCount(), a.getCollectCount(), a.getCommentCount()};
        double[] vecB = {b.getLikeCount(), b.getCollectCount(), b.getCommentCount()};

        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < vecA.length; i++) {
            dot += vecA[i] * vecB[i];
            normA += vecA[i] * vecA[i];
            normB += vecB[i] * vecB[i];
        }
        // 除零保护
        if (normA == 0 || normB == 0) return 0.0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
