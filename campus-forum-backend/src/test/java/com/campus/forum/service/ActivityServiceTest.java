package com.campus.forum.service;

import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.entity.Activity;
import com.campus.forum.entity.ActivityRegistration;
import com.campus.forum.mapper.ActivityMapper;
import com.campus.forum.mapper.ActivityRegistrationMapper;
import com.campus.forum.mapper.PostLikeMapper;
import com.campus.forum.mapper.UserBehaviorMapper;
import com.campus.forum.service.impl.ActivityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UT-011~013: ActivityService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("活动服务测试")
class ActivityServiceTest {

    @Mock private ActivityMapper activityMapper;
    @Mock private ActivityRegistrationMapper registrationMapper;
    @Mock private PostLikeMapper postLikeMapper;
    @Mock private UserBehaviorMapper userBehaviorMapper;

    @InjectMocks
    private ActivityServiceImpl activityService;

    private Activity mockActivity;

    @BeforeEach
    void setUp() {
        mockActivity = new Activity();
        mockActivity.setId(1L);
        mockActivity.setTitle("测试活动");
        mockActivity.setMaxParticipants(2);
        mockActivity.setRegisteredCount(0);
        mockActivity.setStatus(1);
        mockActivity.setLikeCount(0);
        mockActivity.setViewCount(0);
    }

    /**
     * UT-011: 获取活动详情（浏览量+1）
     */
    @Test
    @DisplayName("UT-011: 获取活动详情 - 浏览量应+1")
    void getActivity_ShouldIncrementViewCount() {
        when(activityMapper.selectById(1L)).thenReturn(mockActivity);

        Activity result = activityService.getActivity(1L, null);

        assertEquals(1, result.getViewCount());
        verify(activityMapper).updateById(any(Activity.class));
    }

    /**
     * UT-012: 报名超员拦截
     */
    @Test
    @DisplayName("UT-012: 报名超员 - 应抛出BusinessException")
    void register_WhenFull_ThrowsException() {
        mockActivity.setRegisteredCount(2); // 已满
        when(activityMapper.selectById(1L)).thenReturn(mockActivity);
        when(registrationMapper.selectOne(any())).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> activityService.register(1L, 99L));
        assertTrue(ex.getMessage().contains("已满") || ex.getMessage().contains("超出"));
    }

    /**
     * UT-013: 重复报名拦截
     */
    @Test
    @DisplayName("UT-013: 重复报名 - 应抛出BusinessException")
    void register_Duplicate_ThrowsException() {
        when(activityMapper.selectById(1L)).thenReturn(mockActivity);
        ActivityRegistration existing = new ActivityRegistration();
        when(registrationMapper.selectOne(any())).thenReturn(existing);

        assertThrows(BusinessException.class,
                () -> activityService.register(1L, 1L));
    }
}
