package com.emailmanager.util;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class FileUploadUtil {

    // 定义文件上传的根路径（请根据您的实际情况修改）
    private static final String UPLOAD_ROOT_PATH = "D:/email_uploads/";

    /**
     * 保存上传的文件，并返回其相对访问路径
     * @param file Spring MVC的MultipartFile对象
     * @return 文件的相对存储路径，如果上传失败则返回null
     */
    public static String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 1. 生成按日期分的子目录路径 (例如: /2025/06/18/)
        String datePath = new SimpleDateFormat("/yyyy/MM/dd/").format(new Date());

        // 2. 创建完整的物理目录
        File directory = new File(UPLOAD_ROOT_PATH + datePath);
        if (!directory.exists()) {
            directory.mkdirs(); // 如果目录不存在，则递归创建
        }

        // 3. 生成新的唯一文件名
        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        // 获取文件扩展名 (例如: .jpg, .docx)
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        // 使用UUID生成唯一的文件名主干
        String newFileName = UUID.randomUUID().toString().replace("-", "") + extension;

        // 4. 准备目标文件对象
        File destFile = new File(directory, newFileName);

        // 5. 执行文件保存
        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null; // 保存失败
        }

        // 6. 返回可用于数据库存储和Web访问的相对路径
        return datePath + newFileName; // 例如: /2025/06/18/xxxxxxxx.jpg
    }
}