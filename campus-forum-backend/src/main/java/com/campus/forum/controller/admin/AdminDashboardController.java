package com.campus.forum.controller.admin;

import com.campus.forum.common.Result;
import com.campus.forum.mapper.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 管理端 - 数据统计面板
 */
@Tag(name = "管理端-数据统计")
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final UserMapper userMapper;
    private final ActivityMapper activityMapper;
    private final ActivityRegistrationMapper registrationMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final SectionMapper sectionMapper;

    @Operation(summary = "获取统计概览")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> data = new HashMap<>();
        data.put("userCount", userMapper.selectCount(null));
        data.put("activityCount", activityMapper.selectCount(null));
        data.put("postCount", postMapper.selectCount(null));
        data.put("registrationCount", registrationMapper.selectCount(null));
        data.put("commentCount", commentMapper.selectCount(null));
        return Result.success(data);
    }

    @Operation(summary = "近7天趋势数据（真实数据库）")
    @GetMapping("/trend")
    public Result<Map<String, Object>> trend() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        List<String> labels = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            labels.add(LocalDate.now().minusDays(i).format(fmt));
        }

        List<Map<String, Object>> userRows = userMapper.countUsersByDay();
        List<Map<String, Object>> postRows = userMapper.countPostsByDay();

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("userData", fillTrend(labels, userRows));
        result.put("postData", fillTrend(labels, postRows));
        return Result.success(result);
    }

    @Operation(summary = "板块帖子分布")
    @GetMapping("/section-dist")
    public Result<List<Map<String, Object>>> sectionDist() {
        return Result.success(userMapper.selectSectionDist());
    }

    @Operation(summary = "用户等级分布")
    @GetMapping("/level-dist")
    public Result<List<Map<String, Object>>> levelDist() {
        return Result.success(userMapper.selectLevelDist());
    }

    private List<Long> fillTrend(List<String> labels, List<Map<String, Object>> rows) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String date = row.get("date").toString().substring(5); // "2026-06-24" -> "06-24"
            map.put(date, ((Number) row.get("count")).longValue());
        }
        List<Long> result = new ArrayList<>();
        for (String label : labels) {
            result.add(map.getOrDefault(label, 0L));
        }
        return result;
    }
}
