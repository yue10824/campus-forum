package com.campus.forum.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.entity.PostCollection;

public interface CollectionService {
    boolean toggleCollection(Long userId, Long targetId, String targetType);
    Page<PostCollection> myCollections(Long userId, String targetType, int pageNum, int pageSize);
    boolean isCollected(Long userId, Long targetId, String targetType);
}
