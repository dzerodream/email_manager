package com.emailmanager.mapper;

import com.emailmanager.entity.Announcement;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告Mapper接口 (修正版)
 */
public interface AnnouncementMapper {

    int insert(Announcement announcement);

    Announcement findById(Long id);

    int update(Announcement announcement);

    int deleteById(Long id);

    List<Announcement> findAll();

    List<Announcement> findByPublisherId(@Param("publisherId") Long publisherId);

    List<Announcement> findByStatus(@Param("status") Integer status);

    List<Announcement> findByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    List<Announcement> findLatest(@Param("limit") int limit);

    // updateStatus 方法已从此接口中移除
}