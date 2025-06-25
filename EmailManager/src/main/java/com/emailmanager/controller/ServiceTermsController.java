package com.emailmanager.controller;

import com.emailmanager.entity.ServiceTerms;
import com.emailmanager.service.ServiceTermsService;
import com.emailmanager.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/service-terms") // 所有API都受AdminInterceptor保护
public class ServiceTermsController {

    private final ServiceTermsService serviceTermsService;

    @Autowired
    public ServiceTermsController(ServiceTermsService serviceTermsService) {
        this.serviceTermsService = serviceTermsService;
    }

    /**
     * 获取所有服务条款列表
     * API: GET /admin/service-terms
     */
    @GetMapping
    public Result<List<ServiceTerms>> getAllServiceTerms() {
        List<ServiceTerms> terms = serviceTermsService.findAll();
        return Result.success(terms);
    }

    /**
     * 获取单个服务条款详情
     * API: GET /admin/service-terms/{id}
     */
    @GetMapping("/{id}")
    public Result<ServiceTerms> getServiceTermsById(@PathVariable Long id) {
        ServiceTerms term = serviceTermsService.findById(id);
        return term != null ? Result.success(term) : Result.failure(404, "服务条款未找到");
    }

    /**
     * 发布一个新版本的服务条款
     * 这会自动将旧的生效版本设为过期
     * API: POST /admin/service-terms/publish
     */
    @PostMapping("/publish")
    public Result<ServiceTerms> publishNewVersion(@RequestBody ServiceTerms serviceTerms) {
        // Service层已有完善的逻辑来处理版本号和状态
        serviceTermsService.publishNewVersion(serviceTerms);
        return Result.success("新版本服务条款发布成功", serviceTerms);
    }

    /**
     * 更新一个（通常是草稿状态的）服务条款
     * API: PUT /admin/service-terms/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> updateServiceTerms(@PathVariable Long id, @RequestBody ServiceTerms serviceTerms) {
        serviceTerms.setId(id);
        try {
            serviceTermsService.updateServiceTerms(serviceTerms);
            return Result.success("服务条款更新成功", null);
        } catch (RuntimeException e) {
            return Result.failure(400, e.getMessage());
        }
    }

    /**
     * 手动更新一个服务条款的状态
     * API: PUT /admin/service-terms/{id}/status
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody ServiceTerms statusUpdate) {
        Integer newStatus = statusUpdate.getStatus();
        if (newStatus == null) {
            return Result.failure(400, "状态不能为空");
        }
        serviceTermsService.updateStatus(id, newStatus);
        return Result.success("状态更新成功", null);
    }
}