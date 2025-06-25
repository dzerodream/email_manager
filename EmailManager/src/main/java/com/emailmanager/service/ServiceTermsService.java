package com.emailmanager.service;

import com.emailmanager.entity.ServiceTerms;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务条款服务接口
 */
public interface ServiceTermsService {
    /**
     * 创建服务条款
     */
    void createServiceTerms(ServiceTerms serviceTerms);

    /**
     * 更新服务条款
     */
    void updateServiceTerms(ServiceTerms serviceTerms);

    /**
     * 删除服务条款
     */
    void deleteServiceTerms(Long id);

    /**
     * 根据ID查询服务条款
     */
    ServiceTerms findById(Long id);

    /**
     * 查询所有服务条款
     */
    List<ServiceTerms> findAll();

    /**
     * 根据版本查询服务条款
     */
    ServiceTerms findByVersion(Integer version);

    /**
     * 根据状态查询服务条款
     */
    List<ServiceTerms> findByStatus(Integer status);

    /**
     * 查询当前生效的服务条款
     */
    ServiceTerms findCurrent();

    /**
     * 查询指定日期生效的服务条款
     */
    ServiceTerms findByEffectiveDate(LocalDateTime date);

    /**
     * 更新服务条款状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 发布新版本服务条款
     */
    void publishNewVersion(ServiceTerms serviceTerms);
} 