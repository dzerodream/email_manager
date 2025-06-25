package com.emailmanager.dto;

import lombok.Data;

@Data
public class SpamRuleDTO {
    private String ruleName;
    private String ruleType;  // "SENDER", "SUBJECT", 或 "CONTENT"
    private String ruleContent;
    // status 字段通常在创建时默认为启用，或由专门的API修改，所以DTO中可以不包含
}