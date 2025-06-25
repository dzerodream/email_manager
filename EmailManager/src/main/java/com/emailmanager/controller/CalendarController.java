package com.emailmanager.controller;

import com.emailmanager.entity.Contact;
import com.emailmanager.entity.Holiday;
import com.emailmanager.service.ContactService;
import com.emailmanager.service.HolidayService;
import com.emailmanager.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest; // 引入 HttpServletRequest
import java.time.LocalDate;
import java.util.List;

/**
 * 日历功能控制器 (Token认证适配版)
 */
@RestController
@RequestMapping("/calendar")
public class CalendarController {

    private final HolidayService holidayService;
    private final ContactService contactService;

    @Autowired
    public CalendarController(HolidayService holidayService, ContactService contactService) {
        this.holidayService = holidayService;
        this.contactService = contactService;
    }

    @GetMapping("/holidays")
    public Result<List<Holiday>> getHolidaysByMonth(@RequestParam int year, @RequestParam int month) {
        if (month < 1 || month > 12) {
            return Result.failure(400, "无效的月份");
        }
        List<Holiday> holidays = holidayService.findByYearAndMonth(year, month);
        return Result.success(holidays);
    }

    @GetMapping("/birthdays")
    public Result<List<Contact>> getBirthdaysByMonth(@RequestParam int month, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        List<Contact> contacts = contactService.findBirthdaysByMonth(currentUserId, month);
        return Result.success(contacts);
    }

    @GetMapping("/birthdays/today")
    public Result<List<Contact>> getTodayBirthdays(HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        List<Contact> contacts = contactService.findTodayBirthdays(currentUserId);
        return Result.success(contacts);
    }

    @GetMapping("/birthdays/upcoming")
    public Result<List<Contact>> getUpcomingBirthdays(@RequestParam(defaultValue = "30") int days, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days);
        List<Contact> contacts = contactService.findUpcomingBirthdays(currentUserId, startDate, endDate);
        return Result.success(contacts);
    }
}