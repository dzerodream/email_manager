// 文件路径: com/emailmanager/controller/HolidayController.java
package com.emailmanager.controller;

import com.emailmanager.dto.HolidayDTO; // 1. 引入新的DTO
import com.emailmanager.entity.Holiday;
import com.emailmanager.service.HolidayService;
import com.emailmanager.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 节假日管理控制器（管理员权限）
 */
@RestController
@RequestMapping("/admin/holidays")
public class HolidayController {

    private final HolidayService holidayService;

    @Autowired
    public HolidayController(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    /**
     * 获取指定年份的所有节假日
     */
    @GetMapping
    public Result<List<Holiday>> getHolidaysByYear(@RequestParam int year) {
        List<Holiday> holidays = holidayService.findByYear(year);
        return Result.success(holidays);
    }

    /**
     * 获取单个节假日详情
     */
    @GetMapping("/{id}")
    public Result<Holiday> getHolidayById(@PathVariable Long id) {
        Holiday holiday = holidayService.findById(id);
        return holiday != null ? Result.success(holiday) : Result.failure(404, "节假日未找到");
    }

    /**
     * 添加一个新的节假日或调休记录
     */
    @PostMapping
    public Result<Holiday> addHoliday(@RequestBody HolidayDTO holidayDTO) { // 2. 修改参数类型为HolidayDTO
        // 3. 手动将DTO转换为实体，解决字段名不匹配问题
        Holiday holiday = new Holiday();
        holiday.setHolidayName(holidayDTO.getName()); // 关键映射
        holiday.setHolidayDate(holidayDTO.getHolidayDate());
        holiday.setIsWorkday(holidayDTO.getIsWorkday());
        holiday.setDescription(holidayDTO.getDescription());

        boolean success = holidayService.addHoliday(holiday);

        if (success) {
            // 返回包含新生成ID的完整对象
            return Result.success("节假日添加成功", holiday);
        } else {
            return Result.failure(400, "添加失败，可能日期已存在");
        }
    }

    /**
     * 更新一个节假日信息
     */
    @PutMapping("/{id}")
    public Result<Void> updateHoliday(@PathVariable Long id, @RequestBody HolidayDTO holidayDTO) { // 4. 修改参数类型为HolidayDTO
        Holiday existingHoliday = holidayService.findById(id);
        if (existingHoliday == null) {
            return Result.failure(404, "节假日未找到");
        }

        // 5. 手动将DTO转换为实体
        Holiday holidayToUpdate = new Holiday();
        holidayToUpdate.setId(id); // 确保ID被设置
        holidayToUpdate.setHolidayName(holidayDTO.getName()); // 关键映射
        holidayToUpdate.setHolidayDate(holidayDTO.getHolidayDate());
        holidayToUpdate.setIsWorkday(holidayDTO.getIsWorkday());
        holidayToUpdate.setDescription(holidayDTO.getDescription());

        boolean success = holidayService.updateHoliday(holidayToUpdate);

        return success ? Result.success("更新成功", null) : Result.failure(500, "更新失败");
    }

    /**
     * 删除一个节假日记录
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteHoliday(@PathVariable Long id) {
        boolean success = holidayService.deleteHoliday(id);
        return success ? Result.success("删除成功", null) : Result.failure(404, "节假日未找到，删除失败");
    }
}