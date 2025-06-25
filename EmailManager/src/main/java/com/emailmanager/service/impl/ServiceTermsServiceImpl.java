// com.emailmanager.service.impl/ServiceTermsServiceImpl.java
package com.emailmanager.service.impl;

import com.emailmanager.entity.ServiceTerms;
import com.emailmanager.mapper.ServiceTermsMapper;
import com.emailmanager.service.ServiceTermsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ServiceTermsServiceImpl implements ServiceTermsService {
    @Autowired
    private ServiceTermsMapper serviceTermsMapper;

    @Override
    @Transactional
    public void createServiceTerms(ServiceTerms serviceTerms) {
        serviceTerms.setStatus(0); // 默认创建为草稿
        serviceTerms.setCreatedTime(LocalDateTime.now());
        serviceTerms.setUpdatedTime(LocalDateTime.now());
        // 如果没有设置版本号，可以尝试获取一个（例如，如果允许用户手动创建版本号不连续的草稿）
        if (serviceTerms.getVersion() == null) {
            Integer latestVersion = serviceTermsMapper.findLatestVersion();
            serviceTerms.setVersion(latestVersion == null ? 1 : latestVersion + 1);
        }
        serviceTermsMapper.insert(serviceTerms);
    }

    @Override
    @Transactional
    public void updateServiceTerms(ServiceTerms serviceTerms) {
        ServiceTerms existingTerms = serviceTermsMapper.findById(serviceTerms.getId());
        if (existingTerms == null) {
            throw new RuntimeException("服务条款不存在");
        }
        // 通常只允许修改草稿状态(0)的条款的全部内容
        // 如果要修改已生效(1)或已过期(2)的，通常是修改其状态，而不是内容
        if (existingTerms.getStatus() == 1 &&
                (!existingTerms.getTitle().equals(serviceTerms.getTitle()) ||
                        !existingTerms.getContent().equals(serviceTerms.getContent())) ) {
            // throw new RuntimeException("已生效服务条款的内容不允许直接修改，请发布新版本");
            // 或者，如果允许修改生效条款的文字（不推荐），则需要额外逻辑
        }
        if (existingTerms.getStatus() == 2) {
            throw new RuntimeException("已过期服务条款不允许修改");
        }

        // 如果前端传入了status，并且与现有status不同，则调用updateStatus逻辑
        if (serviceTerms.getStatus() != null && !serviceTerms.getStatus().equals(existingTerms.getStatus())) {
            updateStatus(serviceTerms.getId(), serviceTerms.getStatus());
            // 更新状态后，重新获取一下，因为updateStatus可能改变了其他记录的状态
            existingTerms = serviceTermsMapper.findById(serviceTerms.getId());
        }

        // 更新其他字段
        existingTerms.setTitle(serviceTerms.getTitle());
        existingTerms.setContent(serviceTerms.getContent());
        if(serviceTerms.getEffectiveDate() != null) {
            existingTerms.setEffectiveDate(serviceTerms.getEffectiveDate());
        }
        existingTerms.setUpdatedTime(LocalDateTime.now());
        serviceTermsMapper.update(existingTerms);
    }

    @Override
    @Transactional
    public void deleteServiceTerms(Long id) {
        // 通常不物理删除，而是标记为某个特殊状态，或只允许删除草稿
        ServiceTerms existingTerms = serviceTermsMapper.findById(id);
        if (existingTerms == null) {
            throw new RuntimeException("服务条款不存在");
        }
        if (existingTerms.getStatus() == 1) { // 不允许删除生效中的
            throw new RuntimeException("不能删除正在生效的服务条款");
        }
        serviceTermsMapper.deleteById(id);
    }

    @Override
    public ServiceTerms findById(Long id) {
        return serviceTermsMapper.findById(id);
    }

    @Override
    public List<ServiceTerms> findAll() {
        return serviceTermsMapper.findAll();
    }

    @Override
    public ServiceTerms findByVersion(Integer version) {
        return serviceTermsMapper.findByVersion(version);
    }

    @Override
    public List<ServiceTerms> findByStatus(Integer status) {
        return serviceTermsMapper.findByStatus(status);
    }

    @Override
    public ServiceTerms findCurrent() {
        return serviceTermsMapper.findCurrent();
    }

    @Override
    public ServiceTerms findByEffectiveDate(LocalDateTime date) {
        return serviceTermsMapper.findByEffectiveDate(date);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer newStatus) {
        ServiceTerms termToUpdate = serviceTermsMapper.findById(id);
        if (termToUpdate == null) {
            throw new RuntimeException("服务条款不存在");
        }

        // 如果要将一个条款设为生效 (newStatus == 1)
        if (newStatus == 1) {
            // 1. 找到当前所有已生效的条款
            List<ServiceTerms> currentActiveTermsList = serviceTermsMapper.findByStatus(1);
            for (ServiceTerms activeTerm : currentActiveTermsList) {
                // 2. 如果不是当前要更新为生效的这个条款，则将其设为过期
                if (!activeTerm.getId().equals(id)) {
                    serviceTermsMapper.updateStatus(activeTerm.getId(), 2); // 2 = 已过期
                }
            }
            // 确保要生效的条款的生效日期不晚于当前
            if (termToUpdate.getEffectiveDate() == null || termToUpdate.getEffectiveDate().isAfter(LocalDateTime.now())) {
                termToUpdate.setEffectiveDate(LocalDateTime.now());
            }
        }
        // 如果要将一个条款设为草稿(0)或过期(2)，直接更新即可
        termToUpdate.setStatus(newStatus);
        termToUpdate.setUpdatedTime(LocalDateTime.now());
        serviceTermsMapper.update(termToUpdate); // 使用全量更新来更新status和updatedTime, effectiveDate
    }

    @Override
    @Transactional
    public void publishNewVersion(ServiceTerms newServiceTerms) {
        // 1. 将所有当前已生效的服务条款设置为过期状态 (status = 2)
        List<ServiceTerms> currentActiveTermsList = serviceTermsMapper.findByStatus(1);
        for (ServiceTerms activeTerm : currentActiveTermsList) {
            serviceTermsMapper.updateStatus(activeTerm.getId(), 2);
        }

        // 2. 获取最新的版本号，并为新条款设置版本号
        Integer latestVersion = serviceTermsMapper.findLatestVersion();
        newServiceTerms.setVersion(latestVersion == null ? 1 : latestVersion + 1);

        // 3. 设置新条款的状态为生效，并设置生效日期和时间戳
        newServiceTerms.setStatus(1); // 1 = 生效
        if (newServiceTerms.getEffectiveDate() == null) {
            newServiceTerms.setEffectiveDate(LocalDateTime.now());
        }
        newServiceTerms.setCreatedTime(LocalDateTime.now());
        newServiceTerms.setUpdatedTime(LocalDateTime.now());

        // 4. 保存新版本的服务条款
        serviceTermsMapper.insert(newServiceTerms);
    }
}