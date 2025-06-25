package com.emailmanager.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Controller
@RequestMapping("/files")
public class FileController {

    // 从您的 FileUploadUtil 中获取文件上传的根路径
    // 注意：为了解耦，更好的做法是将此路径配置在 .properties 文件中，然后注入进来
    // 但为了简单，我们暂时硬编码
    private static final String UPLOAD_ROOT_PATH = "D:/email_uploads/";

    /**
     * 文件下载接口
     * 路径：/files/download/{year}/{month}/{day}/{filename}
     * 例如: /files/download/2025/06/22/xxxxxxxx.jpg
     *
     * @param year      年份
     * @param month     月份
     * @param day       日期
     * @param filename  文件名
     * @return ResponseEntity<Resource>
     * @throws UnsupportedEncodingException
     */
    @GetMapping("/download/{year}/{month}/{day}/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String year,
            @PathVariable String month,
            @PathVariable String day,
            @PathVariable String filename) throws UnsupportedEncodingException {

        // 1. 拼接完整的文件物理路径
        String relativePath = "/" + year + "/" + month + "/" + day + "/" + filename;
        File file = new File(UPLOAD_ROOT_PATH + relativePath);

        // 2. 检查文件是否存在且可读
        if (!file.exists() || !file.canRead()) {
            return ResponseEntity.notFound().build();
        }

        // 3. 将文件包装成Spring的Resource对象
        Resource resource = new FileSystemResource(file);

        // 4. 设置HTTP响应头
        HttpHeaders headers = new HttpHeaders();

        // Content-Disposition 头告诉浏览器这是一个需要下载的文件
        // URLEncoder.encode 用于处理文件名中的中文或特殊字符
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(filename, "UTF-8") + "\"");
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        headers.add(HttpHeaders.PRAGMA, "no-cache");
        headers.add(HttpHeaders.EXPIRES, "0");

        try {
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.length()) // 设置文件大小
                    .contentType(MediaType.APPLICATION_OCTET_STREAM) // 设置为通用的二进制流类型
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}