package com.emailmanager.service;

import com.emailmanager.entity.Announcement;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告服务接口
 */
public interface AnnouncementService {
    /**
     * 发布公告
     */
    void publishAnnouncement(Announcement announcement);

    /**
     * 更新公告
     */
    void updateAnnouncement(Announcement announcement);

    /**
     * 删除公告
     */
    void deleteAnnouncement(Long id);

    /**
     * 根据ID查询公告
     */
    Announcement findById(Long id);

    /**
     * 查询所有公告
     */
    List<Announcement> findAll();

    /**
     * 根据发布者ID查询公告
     */
    List<Announcement> findByPublisherId(Long publisherId);

    /**
     * 根据状态查询公告
     */
    List<Announcement> findByStatus(Integer status);

    /**
     * 根据时间范围查询公告
     */
    List<Announcement> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询最新的公告
     */
    List<Announcement> findLatest(int limit);
} 