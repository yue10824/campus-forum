package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.entity.PostCollection;
import com.campus.forum.mapper.PostCollectionMapper;
import com.campus.forum.service.CollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {

    private final PostCollectionMapper collectionMapper;

    @Override
    public boolean toggleCollection(Long userId, Long targetId, String targetType) {
        LambdaQueryWrapper<PostCollection> wrapper = new LambdaQueryWrapper<PostCollection>()
                .eq(PostCollection::getUserId, userId)
                .eq(PostCollection::getTargetId, targetId)
                .eq(PostCollection::getTargetType, targetType);
        PostCollection existing = collectionMapper.selectOne(wrapper);
        if (existing != null) {
            collectionMapper.delete(wrapper);
            return false;
        } else {
            PostCollection collection = new PostCollection();
            collection.setUserId(userId);
            collection.setTargetId(targetId);
            collection.setTargetType(targetType);
            collectionMapper.insert(collection);
            return true;
        }
    }

    @Override
    public Page<PostCollection> myCollections(Long userId, String targetType, int pageNum, int pageSize) {
        Page<PostCollection> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PostCollection> wrapper = new LambdaQueryWrapper<PostCollection>()
                .eq(PostCollection::getUserId, userId)
                .eq(targetType != null, PostCollection::getTargetType, targetType)
                .orderByDesc(PostCollection::getCreatedAt);
        return collectionMapper.selectPage(page, wrapper);
    }

    @Override
    public boolean isCollected(Long userId, Long targetId, String targetType) {
        LambdaQueryWrapper<PostCollection> wrapper = new LambdaQueryWrapper<PostCollection>()
                .eq(PostCollection::getUserId, userId)
                .eq(PostCollection::getTargetId, targetId)
                .eq(PostCollection::getTargetType, targetType);
        return collectionMapper.selectCount(wrapper) > 0;
    }
}
