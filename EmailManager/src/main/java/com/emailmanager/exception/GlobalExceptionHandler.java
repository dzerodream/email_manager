package com.emailmanager.exception;

import com.emailmanager.vo.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

/**
 * 全局异常处理器
 * 使用 @ControllerAdvice 注解，Spring会自动扫描到它
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理所有未被特定方法处理的运行时异常
     * @param e 异常对象
     * @return 统一的错误响应
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody // 确保返回的是JSON而不是视图名
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 设置HTTP状态码为500
    public Result<Void> handleRuntimeException(RuntimeException e) {
        // 在服务器日志中打印完整的异常堆栈，方便调试
        e.printStackTrace();

        // 向前端返回一个通用的、模糊的错误信息，避免泄露内部细节
        return Result.failure(500, "服务器内部错误，请联系管理员");
    }

    /**
     * 您未来可以添加更多特定的异常处理器
     * 例如，处理自定义的业务异常
     */
    /*
    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 业务异常通常返回400
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.failure(e.getCode(), e.getMessage());
    }
    */
}