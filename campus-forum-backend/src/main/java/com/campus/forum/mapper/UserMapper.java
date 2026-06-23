package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.User;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(@Param("id") Long id);

    @Insert("INSERT IGNORE INTO user_follow(follower_id, followed_id) VALUES(#{followerId}, #{followedId})")
    void follow(@Param("followerId") Long followerId, @Param("followedId") Long followedId);

    @Delete("DELETE FROM user_follow WHERE follower_id = #{followerId} AND followed_id = #{followedId}")
    void unfollow(@Param("followerId") Long followerId, @Param("followedId") Long followedId);

    @Select("SELECT COUNT(1) > 0 FROM user_follow WHERE follower_id = #{followerId} AND followed_id = #{followedId}")
    boolean isFollowing(@Param("followerId") Long followerId, @Param("followedId") Long followedId);

    @Select("SELECT u.* FROM user u JOIN user_follow f ON u.id = f.follower_id WHERE f.followed_id = #{userId} LIMIT #{offset}, #{size}")
    List<User> findFollowers(@Param("userId") Long userId, @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(1) FROM user_follow WHERE followed_id = #{userId}")
    long countFollowers(@Param("userId") Long userId);

    @Select("SELECT u.* FROM user u JOIN user_follow f ON u.id = f.followed_id WHERE f.follower_id = #{userId} LIMIT #{offset}, #{size}")
    List<User> findFollowing(@Param("userId") Long userId, @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(1) FROM user_follow WHERE follower_id = #{userId}")
    long countFollowing(@Param("userId") Long userId);

    /** 增加经验值，并根据阈值自动升级 */
    @Update("UPDATE user SET exp = exp + #{delta}, " +
            "level = CASE " +
            "  WHEN exp + #{delta} < 50   THEN 1 " +
            "  WHEN exp + #{delta} < 150  THEN 2 " +
            "  WHEN exp + #{delta} < 300  THEN 3 " +
            "  WHEN exp + #{delta} < 500  THEN 4 " +
            "  WHEN exp + #{delta} < 800  THEN 5 " +
            "  WHEN exp + #{delta} < 1200 THEN 6 " +
            "  WHEN exp + #{delta} < 1800 THEN 7 " +
            "  WHEN exp + #{delta} < 2500 THEN 8 " +
            "  WHEN exp + #{delta} < 3500 THEN 9 " +
            "  ELSE 10 END " +
            "WHERE id = #{userId}")
    void addExp(@Param("userId") Long userId, @Param("delta") int delta);

    // 管理端统计
    @Select("SELECT COUNT(1) FROM user")
    long countAllUsers();

    @Select("SELECT DATE(created_at) as date, COUNT(1) as count FROM user " +
            "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) GROUP BY DATE(created_at) ORDER BY date")
    List<java.util.Map<String, Object>> countUsersByDay();

    @Select("SELECT COUNT(1) FROM post WHERE status=1")
    long countAllPosts();

    @Select("SELECT DATE(created_at) as date, COUNT(1) as count FROM post " +
            "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) AND status=1 GROUP BY DATE(created_at) ORDER BY date")
    List<java.util.Map<String, Object>> countPostsByDay();

    @Select("SELECT s.name as name, COUNT(p.id) as value FROM section s " +
            "LEFT JOIN post p ON p.section_id = s.id AND p.status = 1 " +
            "GROUP BY s.id, s.name ORDER BY s.sort_order")
    List<java.util.Map<String, Object>> selectSectionDist();

    @Select("SELECT CONCAT('Lv', level) as name, COUNT(1) as value FROM user GROUP BY level ORDER BY level")
    List<java.util.Map<String, Object>> selectLevelDist();

    /** 禁止用户发帖30分钟 */
    @Update("UPDATE user SET post_ban_until = #{banUntil} WHERE id = #{userId}")
    void banPost(@Param("userId") Long userId, @Param("banUntil") LocalDateTime banUntil);

    /** 解除禁止发帖 */
    @Update("UPDATE user SET post_ban_until = NULL WHERE id = #{userId}")
    void unbanPost(@Param("userId") Long userId);
}
