// 文件路径: com/emailmanager/service/impl/ServiceTermsServiceImplTest.java
package com.emailmanager.service.impl;

import com.emailmanager.entity.ServiceTerms;
import com.emailmanager.mapper.ServiceTermsMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceTermsServiceImplTest {

    @Mock
    private ServiceTermsMapper serviceTermsMapper;

    @InjectMocks
    private ServiceTermsServiceImpl serviceTermsService;

    @Test
    @DisplayName("发布新版本 - 应将旧的生效版本设为过期")
    void testPublishNewVersion() {
        ServiceTerms oldActiveTerms = new ServiceTerms();
        oldActiveTerms.setId(1L);
        oldActiveTerms.setStatus(1); // 状态为生效

        ServiceTerms newTerms = new ServiceTerms();
        newTerms.setTitle("v2.0");

        when(serviceTermsMapper.findCurrent()).thenReturn(oldActiveTerms);
        when(serviceTermsMapper.findLatestVersion()).thenReturn(1);

        serviceTermsService.publishNewVersion(newTerms);

        // 验证旧版本被更新为过期状态（status=2）
        verify(serviceTermsMapper, times(1)).updateStatus(1L, 2);
        // 验证新版本被插入
        verify(serviceTermsMapper, times(1)).insert(newTerms);
        // 验证新版本的版本号和状态被正确设置
        assertEquals(2, newTerms.getVersion());
        assertEquals(1, newTerms.getStatus());
    }

    @Test
    @DisplayName("更新服务条款 - 失败（尝试更新已生效的条款）")
    void testUpdateServiceTerms_Failure_IsActive() {
        ServiceTerms activeTerms = new ServiceTerms();
        activeTerms.setId(1L);
        activeTerms.setStatus(1); // 状态为生效

        when(serviceTermsMapper.findById(1L)).thenReturn(activeTerms);

        assertThrows(RuntimeException.class, () -> serviceTermsService.updateServiceTerms(activeTerms));
    }
}