package com.emailmanager.controller;

import com.emailmanager.entity.Announcement;
import com.emailmanager.service.AnnouncementService;
import com.emailmanager.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest; // 引入 HttpServletRequest
import java.util.List;

@RestController
@RequestMapping("/admin/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @Autowired
    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping
    public Result<List<Announcement>> getAllAnnouncements() {
        List<Announcement> announcements = announcementService.findAll();
        return Result.success(announcements);
    }

    @GetMapping("/{id}")
    public Result<Announcement> getAnnouncementById(@PathVariable Long id) {
        Announcement announcement = announcementService.findById(id);
        return announcement != null ? Result.success(announcement) : Result.failure(404, "公告未找到");
    }

    /**
     * 发布新公告 (Token认证适配版)
     */
    @PostMapping
    public Result<Announcement> createAnnouncement(@RequestBody Announcement announcement, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        announcement.setPublisherId(currentUserId); // 设置发布者为当前管理员

        announcementService.publishAnnouncement(announcement);
        return Result.success("公告发布成功", announcement);
    }

    @PutMapping("/{id}")
    public Result<Void> updateAnnouncement(@PathVariable Long id, @RequestBody Announcement announcement) {
        announcement.setId(id);
        announcementService.updateAnnouncement(announcement);
        return Result.success("公告更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return Result.success("公告删除成功", null);
    }
}