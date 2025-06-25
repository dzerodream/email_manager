package com.emailmanager.service;

import com.emailmanager.entity.SpamRule;
import java.util.List;

public interface SpamRuleService {
    /**
     * 添加垃圾邮件规则
     * @param rule 规则信息
     * @return 是否添加成功
     */
    boolean addRule(SpamRule rule);

    /**
     * 更新垃圾邮件规则
     * @param rule 规则信息
     * @return 是否更新成功
     */
    boolean updateRule(SpamRule rule);

    /**
     * 删除垃圾邮件规则
     * @param id 规则ID
     * @return 是否删除成功
     */
    boolean deleteRule(Long id);

    /**
     * 根据ID查询规则
     * @param id 规则ID
     * @return 规则信息
     */
    SpamRule findById(Long id);

    /**
     * 查询用户的所有规则
     * @param userId 用户ID
     * @return 规则列表
     */
    List<SpamRule> findByUserId(Long userId);

    /**
     * 查询全局规则
     * @return 全局规则列表
     */
    List<SpamRule> findGlobalRules();

    /**
     * 启用规则
     * @param id 规则ID
     * @return 是否启用成功
     */
    boolean enableRule(Long id);

    /**
     * 禁用规则
     * @param id 规则ID
     * @return 是否禁用成功
     */
    boolean disableRule(Long id);

    /**
     * 检查邮件是否匹配垃圾邮件规则
     * @param userId 用户ID
     * @param sender 发件人
     * @param subject 主题
     * @param content 内容
     * @return 是否匹配垃圾邮件规则
     */
    boolean isSpam(Long userId, String sender, String subject, String content);

    /**
     * 导入规则
     * @param userId 用户ID
     * @param rules 规则列表
     * @return 成功导入的数量
     */
    int importRules(Long userId, List<SpamRule> rules);

    /**
     * 导出规则
     * @param userId 用户ID
     * @return 规则列表
     */
    List<SpamRule> exportRules(Long userId);
}