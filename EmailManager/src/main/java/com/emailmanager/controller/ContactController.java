package com.emailmanager.controller;

import com.emailmanager.dto.ContactDTO;
import com.emailmanager.entity.Contact;
import com.emailmanager.service.ContactService;
import com.emailmanager.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest; // 引入 HttpServletRequest
import java.util.List;

@RestController
@RequestMapping("/contacts")
public class ContactController {

    private final ContactService contactService;

    @Autowired
    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public Result<List<Contact>> getAllContacts(HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        List<Contact> contacts = contactService.findByUserId(currentUserId);
        return Result.success(contacts);
    }

    @GetMapping("/{id}")
    public Result<Contact> getContactById(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        Contact contact = contactService.findById(id, currentUserId);
        return contact != null ? Result.success(contact) : Result.failure(404, "联系人未找到");
    }

    @PostMapping
    public Result<Void> addContact(@RequestBody ContactDTO contactDTO, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        Contact contact = new Contact();
        contact.setName(contactDTO.getName());
        contact.setEmail(contactDTO.getEmail());
        contact.setPhone(contactDTO.getPhone());
        contact.setBirthday(contactDTO.getBirthday());
        contact.setRemark(contactDTO.getRemark());
        contact.setUserId(currentUserId);

        try {
            contactService.addContact(contact);
            return Result.success("联系人添加成功", null);
        } catch (RuntimeException e) {
            return Result.failure(400, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Void> updateContact(@PathVariable Long id, @RequestBody ContactDTO contactDTO, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        Contact contactToUpdate = new Contact();
        contactToUpdate.setId(id);
        contactToUpdate.setUserId(currentUserId);
        contactToUpdate.setName(contactDTO.getName());
        contactToUpdate.setEmail(contactDTO.getEmail());
        contactToUpdate.setPhone(contactDTO.getPhone());
        contactToUpdate.setBirthday(contactDTO.getBirthday());
        contactToUpdate.setRemark(contactDTO.getRemark());

        try {
            contactService.updateContact(contactToUpdate);
            return Result.success("联系人更新成功", null);
        } catch (RuntimeException e) {
            return Result.failure(400, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteContact(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        try {
            contactService.deleteContact(id, currentUserId);
            return Result.success("联系人删除成功", null);
        } catch (RuntimeException e) {
            return Result.failure(404, e.getMessage());
        }
    }

    @GetMapping("/search")
    public Result<List<Contact>> searchContacts(@RequestParam String keyword, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        List<Contact> contactsByName = contactService.searchByName(currentUserId, keyword);
        List<Contact> contactsByEmail = contactService.searchByEmail(currentUserId, keyword);
        contactsByName.removeAll(contactsByEmail);
        contactsByName.addAll(contactsByEmail);
        return Result.success(contactsByName);
    }
}