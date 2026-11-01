package com.nie.mentalserver.handler;

import com.nie.mental.common.exception.BusinessException;
import com.nie.mental.common.result.Result;
import com.nie.mental.common.result.ResultCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理类
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     *处理 @RequestBody 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleValidException( MethodArgumentNotValidException e){
        BindingResult bindingResult = e.getBindingResult();
        String message = bindingResult.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        if( message.isEmpty()){
            message =" 参数校验失败";
        }
        return Result.error(ResultCode.PARAM_ERROR.getCode(),ResultCode.PARAM_ERROR.getMsg(),message);
    }

    /**
     *处理 @RequestParam or @PathVariable 参数校验异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<String> handleConstraintException(ConstraintViolationException e){
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        return Result.error(ResultCode.PARAM_ERROR.getCode(),ResultCode.PARAM_ERROR.getMsg(),message);
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException( BusinessException e){
        //如果携带了参数
        if( e.getData() != null){
            return Result.error(e.getCode(),e.getMessage(),e.getData());
        }
        return Result.error(e.getCode(),e.getMessage(),null);
    }

//    /**
//     *兜底的全局异常处理
//     */
//    @ExceptionHandler(Exception.class)
//    public Result<String> handleException( Exception e){
//        log.info("系统发生未知错误", e);
//        return Result.error(ResultCode.SYSTEM_ERROR.getCode(),ResultCode.SYSTEM_ERROR.getMsg(),null);
//    }
}
