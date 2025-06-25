package com.emailmanager.controller;

import com.emailmanager.dto.SpamRuleDTO;
import com.emailmanager.entity.SpamRule;
import com.emailmanager.service.SpamRuleService;
import com.emailmanager.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class SpamRuleController {

    private final SpamRuleService spamRuleService;

    @Autowired
    public SpamRuleController(SpamRuleService spamRuleService) {
        this.spamRuleService = spamRuleService;
    }

    // --- 普通用户 API (保持不变) ---
    @GetMapping("/spam-rules")
    public Result<List<SpamRule>> getMySpamRules(HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        List<SpamRule> rules = spamRuleService.findByUserId(currentUserId);
        return Result.success(rules);
    }

    @PostMapping("/spam-rules")
    public Result<SpamRule> addMySpamRule(@RequestBody SpamRuleDTO ruleDTO, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        SpamRule newRule = new SpamRule();
        newRule.setRuleName(ruleDTO.getRuleName());
        newRule.setRuleType(ruleDTO.getRuleType());
        newRule.setRuleContent(ruleDTO.getRuleContent());
        newRule.setUserId(currentUserId);
        newRule.setIsGlobal(0); // 个人规则
        boolean success = spamRuleService.addRule(newRule);
        // 返回创建后的对象，包含ID
        return success ? Result.success("个人规则添加成功", newRule) : Result.failure(400, "规则已存在或添加失败");
    }

    @PutMapping("/spam-rules/{id}")
    public Result<Void> updateMySpamRule(@PathVariable Long id, @RequestBody SpamRuleDTO ruleDTO, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        SpamRule existingRule = spamRuleService.findById(id);
        if (existingRule == null || !existingRule.getUserId().equals(currentUserId) || existingRule.getIsGlobal() == 1) {
            return Result.failure(404, "个人规则未找到或无权修改");
        }
        SpamRule ruleToUpdate = new SpamRule();
        ruleToUpdate.setId(id);
        ruleToUpdate.setUserId(currentUserId); // 确保userId不会被篡改
        ruleToUpdate.setIsGlobal(0);          // 确保isGlobal不会被篡改
        ruleToUpdate.setRuleName(ruleDTO.getRuleName());
        ruleToUpdate.setRuleType(ruleDTO.getRuleType());
        ruleToUpdate.setRuleContent(ruleDTO.getRuleContent());
        // status 通常由单独的 enable/disable 接口控制，如果需要在这里改，DTO也要加status
        // ruleToUpdate.setStatus(existingRule.getStatus()); // 保持原有状态，除非DTO里有
        boolean success = spamRuleService.updateRule(ruleToUpdate);
        return success ? Result.success("规则更新成功", null) : Result.failure(400, "更新失败，可能与现有规则冲突");
    }

    @DeleteMapping("/spam-rules/{id}")
    public Result<Void> deleteMySpamRule(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        SpamRule existingRule = spamRuleService.findById(id);
        if (existingRule == null || !existingRule.getUserId().equals(currentUserId) || existingRule.getIsGlobal() == 1) {
            return Result.failure(404, "个人规则未找到或无权删除");
        }
        spamRuleService.deleteRule(id);
        return Result.success("规则删除成功", null);
    }

    @PutMapping("/spam-rules/{id}/enable")
    public Result<Void> enableMySpamRule(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        SpamRule existingRule = spamRuleService.findById(id);
        if (existingRule == null || !existingRule.getUserId().equals(currentUserId) || existingRule.getIsGlobal() == 1) {
            return Result.failure(404, "个人规则未找到或无权操作");
        }
        spamRuleService.enableRule(id);
        return Result.success("规则已启用", null);
    }

    @PutMapping("/spam-rules/{id}/disable")
    public Result<Void> disableMySpamRule(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        SpamRule existingRule = spamRuleService.findById(id);
        if (existingRule == null || !existingRule.getUserId().equals(currentUserId) || existingRule.getIsGlobal() == 1) {
            return Result.failure(404, "个人规则未找到或无权操作");
        }
        spamRuleService.disableRule(id);
        return Result.success("规则已禁用", null);
    }

    // --- 管理员 API ---

    @GetMapping("/admin/spam-rules/global")
    public Result<List<SpamRule>> getGlobalSpamRules() {
        List<SpamRule> rules = spamRuleService.findGlobalRules();
        return Result.success(rules);
    }

    @PostMapping("/admin/spam-rules/global")
    public Result<SpamRule> addGlobalSpamRule(@RequestBody SpamRuleDTO ruleDTO, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId"); // 创建者ID
        SpamRule newRule = new SpamRule();
        newRule.setRuleName(ruleDTO.getRuleName());
        newRule.setRuleType(ruleDTO.getRuleType());
        newRule.setRuleContent(ruleDTO.getRuleContent());
        newRule.setUserId(currentUserId);
        newRule.setIsGlobal(1); // 全局规则
        boolean success = spamRuleService.addRule(newRule);
        return success ? Result.success("全局规则添加成功", newRule) : Result.failure(400, "规则已存在或添加失败");
    }

    // --- === 补全以下管理员API === ---

    @PutMapping("/admin/spam-rules/global/{id}")
    public Result<Void> updateGlobalSpamRule(@PathVariable Long id, @RequestBody SpamRuleDTO ruleDTO, HttpServletRequest request) {
        SpamRule existingRule = spamRuleService.findById(id);
        if (existingRule == null || existingRule.getIsGlobal() != 1) {
            return Result.failure(404, "全局规则未找到");
        }
        // Long creatorId = (Long) request.getAttribute("userId"); // 更新者ID，如果需要记录
        SpamRule ruleToUpdate = new SpamRule();
        ruleToUpdate.setId(id);
        ruleToUpdate.setUserId(existingRule.getUserId()); // 保持原始创建者ID
        ruleToUpdate.setIsGlobal(1); // 确保是全局规则
        ruleToUpdate.setRuleName(ruleDTO.getRuleName());
        ruleToUpdate.setRuleType(ruleDTO.getRuleType());
        ruleToUpdate.setRuleContent(ruleDTO.getRuleContent());
        // ruleToUpdate.setStatus(existingRule.getStatus()); // 保持原有状态，除非DTO里有
        boolean success = spamRuleService.updateRule(ruleToUpdate);
        return success ? Result.success("全局规则更新成功", null) : Result.failure(400, "更新失败，可能与现有规则冲突");
    }

    @DeleteMapping("/admin/spam-rules/global/{id}")
    public Result<Void> deleteGlobalSpamRule(@PathVariable Long id) {
        SpamRule existingRule = spamRuleService.findById(id);
        if (existingRule == null || existingRule.getIsGlobal() != 1) {
            return Result.failure(404, "全局规则未找到");
        }
        spamRuleService.deleteRule(id);
        return Result.success("全局规则删除成功", null);
    }

    @PutMapping("/admin/spam-rules/global/{id}/enable")
    public Result<Void> enableGlobalSpamRule(@PathVariable Long id) {
        SpamRule existingRule = spamRuleService.findById(id);
        if (existingRule == null || existingRule.getIsGlobal() != 1) {
            return Result.failure(404, "全局规则未找到");
        }
        spamRuleService.enableRule(id);
        return Result.success("全局规则已启用", null);
    }

    @PutMapping("/admin/spam-rules/global/{id}/disable")
    public Result<Void> disableGlobalSpamRule(@PathVariable Long id) {
        SpamRule existingRule = spamRuleService.findById(id);
        if (existingRule == null || existingRule.getIsGlobal() != 1) {
            return Result.failure(404, "全局规则未找到");
        }
        spamRuleService.disableRule(id);
        return Result.success("全局规则已禁用", null);
    }
}