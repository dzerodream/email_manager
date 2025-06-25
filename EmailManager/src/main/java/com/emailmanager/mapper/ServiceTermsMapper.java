 package com.emailmanager.mapper;

import com.emailmanager.entity.ServiceTerms;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务条款Mapper接口
 */
@Mapper
public interface ServiceTermsMapper {
    /**
     * 插入服务条款
     */
    int insert(ServiceTerms serviceTerms);

    /**
     * 根据ID查询服务条款
     */
    ServiceTerms findById(Long id);

    /**
     * 更新服务条款
     */
    int update(ServiceTerms serviceTerms);

    /**
     * 删除服务条款
     */
    int deleteById(Long id);

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
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 查询最新版本号
     */
    Integer findLatestVersion();
}