// 文件路径: com/emailmanager/service/impl/SpamRuleServiceImpl.java
package com.emailmanager.service.impl;

import com.emailmanager.entity.SpamRule;
import com.emailmanager.mapper.SpamRuleMapper;
import com.emailmanager.service.SpamRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 垃圾邮件规则服务实现类
 */
@Service
public class SpamRuleServiceImpl implements SpamRuleService {

    @Autowired
    private SpamRuleMapper spamRuleMapper;

    @Override
    @Transactional
    public boolean addRule(SpamRule spamRule) {
        if (spamRuleMapper.findByUserIdAndRule(spamRule.getUserId(), spamRule.getRuleType(), spamRule.getRuleContent()) != null) {
            return false;
        }
        spamRule.setStatus(1);
        spamRule.setCreatedTime(LocalDateTime.now());
        spamRule.setUpdatedTime(LocalDateTime.now());
        return spamRuleMapper.insert(spamRule) > 0;
    }

    @Override
    @Transactional
    public boolean updateRule(SpamRule spamRule) {
        SpamRule existingRule = spamRuleMapper.findById(spamRule.getId());
        if (existingRule == null) {
            return false;
        }
        SpamRule conflictRule = spamRuleMapper.findByUserIdAndRule(spamRule.getUserId(), spamRule.getRuleType(), spamRule.getRuleContent());
        if (conflictRule != null && !conflictRule.getId().equals(spamRule.getId())) {
            return false;
        }
        spamRule.setUpdatedTime(LocalDateTime.now());
        return spamRuleMapper.update(spamRule) > 0;
    }

    @Override
    @Transactional
    public boolean deleteRule(Long id) {
        return spamRuleMapper.deleteById(id) > 0;
    }

    @Override
    public SpamRule findById(Long id) {
        return spamRuleMapper.findById(id);
    }

    @Override
    public List<SpamRule> findByUserId(Long userId) {
        return spamRuleMapper.findByUserId(userId);
    }

    @Override
    public List<SpamRule> findGlobalRules() {
        return spamRuleMapper.findGlobalRules();
    }

    @Override
    @Transactional
    public boolean enableRule(Long id) {
        return spamRuleMapper.updateStatus(id, 1) > 0;
    }

    @Override
    @Transactional
    public boolean disableRule(Long id) {
        return spamRuleMapper.updateStatus(id, 0) > 0;
    }

    /**
     * 检查邮件是否匹配垃圾邮件规则（修正版）
     * 严格区分全局规则和个人规则的作用域。
     */
    @Override
    public boolean isSpam(Long userId, String senderEmail, String subject, String content) {
        // 1. 检查所有启用的全局规则
        List<SpamRule> globalRules = spamRuleMapper.findGlobalRules();
        if (matchesAnyRule(globalRules, senderEmail, subject, content)) {
            return true;
        }

        // 2. 检查当前收件人自己的、所有启用的个人规则
        List<SpamRule> personalRules = spamRuleMapper.findByUserId(userId);
        if (matchesAnyRule(personalRules, senderEmail, subject, content)) {
            return true;
        }

        // 3. 所有规则都未匹配
        return false;
    }

    /**
     * 辅助方法：检查邮件信息是否匹配给定的规则列表中的任何一条规则。
     */
    private boolean matchesAnyRule(List<SpamRule> rules, String senderEmail, String subject, String content) {
        if (rules == null || rules.isEmpty()) {
            return false;
        }

        for (SpamRule rule : rules) {
            // 只检查状态为启用的规则
            if (rule.getStatus() != null && rule.getStatus() == 1) {
                String ruleContent = rule.getRuleContent();
                if (ruleContent == null || ruleContent.isEmpty()) {
                    continue; // 跳过内容为空的规则
                }

                String targetText = null;
                switch (rule.getRuleType()) {
                    case "SENDER":
                        targetText = senderEmail;
                        break;
                    case "SUBJECT":
                        targetText = subject;
                        break;
                    case "CONTENT":
                        targetText = content;
                        break;
                }

                if (targetText != null) {
                    // 使用不区分大小写的包含检查
                    if (targetText.toLowerCase().contains(ruleContent.toLowerCase())) {
                        return true; // 匹配成功，立即返回
                    }
                }
            }
        }
        return false; // 遍历完所有规则都未匹配
    }

    @Override
    @Transactional
    public int importRules(Long userId, List<SpamRule> rules) {
        int successCount = 0;
        for (SpamRule rule : rules) {
            rule.setUserId(userId);
            rule.setStatus(1);
            rule.setCreatedTime(LocalDateTime.now());
            rule.setUpdatedTime(LocalDateTime.now());
            try {
                if (spamRuleMapper.findByUserIdAndRule(userId, rule.getRuleType(), rule.getRuleContent()) == null) {
                    if (spamRuleMapper.insert(rule) > 0) {
                        successCount++;
                    }
                }
            } catch (Exception e) {
                System.err.println("导入垃圾邮件规则失败: " + rule.getRuleContent() + ", 错误: " + e.getMessage());
            }
        }
        return successCount;
    }

    @Override
    public List<SpamRule> exportRules(Long userId) {
        return findByUserId(userId);
    }
}