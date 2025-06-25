package com.emailmanager.mapper;

import com.emailmanager.entity.Email;
public interface EmailMapper {
    int insert(Email email);
    int update(Email email);
    Email findWithAttachmentsById(Long id);
    int deleteById(Long id);
}