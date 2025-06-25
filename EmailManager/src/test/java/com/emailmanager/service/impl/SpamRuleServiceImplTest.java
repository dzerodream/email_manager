// 文件路径: com/emailmanager/service/impl/SpamRuleServiceImplTest.java
package com.emailmanager.service.impl;

import com.emailmanager.entity.SpamRule;
import com.emailmanager.mapper.SpamRuleMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpamRuleServiceImplTest {

    @Mock
    private SpamRuleMapper spamRuleMapper;

    @InjectMocks
    private SpamRuleServiceImpl spamRuleService;

    @Test
    @DisplayName("判断垃圾邮件 - 被全局规则拦截")
    void testIsSpam_BlockedByGlobalRule() {
        SpamRule globalRule = new SpamRule(null, 0L, null, "SUBJECT", "广告", 1, 1, null, null);
        when(spamRuleMapper.findGlobalRules()).thenReturn(Arrays.asList(globalRule));

        boolean result = spamRuleService.isSpam(1L, "any@sender.com", "【紧急广告】", "some content");

        assertTrue(result);
        verify(spamRuleMapper, never()).findByUserId(anyLong()); // 确认未继续检查个人规则
    }

    @Test
    @DisplayName("判断垃圾邮件 - 被个人规则拦截")
    void testIsSpam_BlockedByPersonalRule() {
        SpamRule personalRule = new SpamRule(null, 1L, null, "SENDER", "spam.com", 0, 1, null, null);
        when(spamRuleMapper.findGlobalRules()).thenReturn(Arrays.asList()); // 全局规则为空
        when(spamRuleMapper.findByUserId(1L)).thenReturn(Arrays.asList(personalRule));

        boolean result = spamRuleService.isSpam(1L, "user@spam.com", "正常主题", "some content");

        assertTrue(result);
    }

    @Test
    @DisplayName("判断垃圾邮件 - 非垃圾邮件")
    void testIsSpam_NotSpam() {
        when(spamRuleMapper.findGlobalRules()).thenReturn(Collections.emptyList());
        when(spamRuleMapper.findByUserId(1L)).thenReturn(Collections.emptyList());

        boolean result = spamRuleService.isSpam(1L, "friend@good.com", "你好", "...");

        assertFalse(result);
    }
}