package com.emailmanager.service.impl;

import com.emailmanager.entity.Announcement;
import com.emailmanager.mapper.AnnouncementMapper;
import com.emailmanager.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告服务实现类
 */
@Service
public class AnnouncementServiceImpl implements AnnouncementService {
    @Autowired
    private AnnouncementMapper announcementMapper;

    @Override
    @Transactional
    public void publishAnnouncement(Announcement announcement) {
        announcement.setStatus(1);
        // 修正：在插入前设置 createdTime 和 updatedTime
        announcement.setPublishTime(LocalDateTime.now());
        announcement.setCreatedTime(LocalDateTime.now());
        announcement.setUpdatedTime(LocalDateTime.now());
        announcementMapper.insert(announcement);
    }

    @Override
    @Transactional
    public void updateAnnouncement(Announcement announcement) {
        // 修正：在更新前设置 updatedTime
        announcement.setUpdatedTime(LocalDateTime.now());
        announcementMapper.update(announcement);
    }

    @Override
    @Transactional
    public void deleteAnnouncement(Long id) {
        announcementMapper.deleteById(id);
    }

    @Override
    public Announcement findById(Long id) {
        return announcementMapper.findById(id);
    }

    @Override
    public List<Announcement> findAll() {
        return announcementMapper.findAll();
    }

    @Override
    public List<Announcement> findByPublisherId(Long publisherId) {
        return announcementMapper.findByPublisherId(publisherId);
    }

    @Override
    public List<Announcement> findByStatus(Integer status) {
        return announcementMapper.findByStatus(status);
    }

    @Override
    public List<Announcement> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return announcementMapper.findByTimeRange(startTime, endTime);
    }

    @Override
    public List<Announcement> findLatest(int limit) {
        return announcementMapper.findLatest(limit);
    }
}
