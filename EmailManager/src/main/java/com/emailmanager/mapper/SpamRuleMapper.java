package com.emailmanager.mapper;

import com.emailmanager.entity.SpamRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 垃圾邮件规则Mapper接口
 */
@Mapper
public interface SpamRuleMapper {
    /**
     * 插入垃圾邮件规则
     */
    int insert(SpamRule spamRule);

    /**
     * 根据ID查询垃圾邮件规则
     */
    SpamRule findById(Long id);

    /**
     * 更新垃圾邮件规则
     */
    int update(SpamRule spamRule);

    /**
     * 删除垃圾邮件规则
     */
    int deleteById(Long id);

    /**
     * 查询所有垃圾邮件规则 (通常不直接使用，但保留)
     */
    List<SpamRule> findAll();

    /**
     * 根据用户ID查询个人垃圾邮件规则
     */
    List<SpamRule> findByUserId(@Param("userId") Long userId);

    /**
     * 查询所有全局垃圾邮件规则
     */
    List<SpamRule> findGlobalRules();

    /**
     * 根据用户ID、规则类型和规则内容查询单个规则
     */
    SpamRule findByUserIdAndRule(@Param("userId") Long userId, @Param("ruleType") String ruleType, @Param("ruleContent") String ruleContent);

    /**
     * 更新垃圾邮件规则状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
