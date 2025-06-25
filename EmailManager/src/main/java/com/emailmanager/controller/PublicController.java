package com.emailmanager.controller;

import com.emailmanager.entity.Announcement;
import com.emailmanager.entity.ServiceTerms;
import com.emailmanager.service.AnnouncementService;
import com.emailmanager.service.ServiceTermsService;
import com.emailmanager.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公共API控制器
 * 提供无需登录即可访问的公共信息
 */
@RestController
@RequestMapping("/public") // 所有API都以/public为前缀
public class PublicController {

    private final AnnouncementService announcementService;
    private final ServiceTermsService serviceTermsService;

    @Autowired
    public PublicController(AnnouncementService announcementService, ServiceTermsService serviceTermsService) {
        this.announcementService = announcementService;
        this.serviceTermsService = serviceTermsService;
    }

    /**
     * 获取所有当前生效的系统公告
     * API: GET /public/announcements
     */
    @GetMapping("/announcements")
    public Result<List<Announcement>> getActiveAnnouncements() {
        // 查询状态为 1 (启用) 的公告
        List<Announcement> announcements = announcementService.findByStatus(1);
        return Result.success(announcements);
    }

    /**
     * 获取当前生效的服务条款
     * API: GET /public/service-terms/current
     */
    @GetMapping("/service-terms/current")
    public Result<ServiceTerms> getCurrentServiceTerms() {
        ServiceTerms currentTerms = serviceTermsService.findCurrent();
        return Result.success(currentTerms);
    }
}
