package com.nie.mental.common.result;

import lombok.Data;

/**
 * 后端统一返回结果
 * @param <T>
 */
@Data
public class Result <T> {

    private String code; // 200成功，-1失败
    private String msg; // 错误信息
    private T data; //数据

    public static <T> Result <T> success(){
        Result<T> result = new Result<>();
        result.code = ResultCode.SUCCESS.getCode();
        result.msg = ResultCode.SUCCESS.getMsg();
        return result;
    }

    public static <T> Result<T> success( T data){
        Result<T> result = new Result<>();
        result.code = ResultCode.SUCCESS.getCode();
        result.msg = ResultCode.SUCCESS.getMsg();
        result.data = data;
        return result;
    }

    public static <T> Result <T> error(){
        Result<T> result = new Result<>();
        result.code = ResultCode.ERROR.getCode();
        result.msg = ResultCode.ERROR.getMsg();
        return result;
    }

    public static <T> Result <T> error(String code, String msg ,T data){
        Result<T> result = new Result<>();
        result.code = code;
        result.msg = msg;
        result.data = data;
        return result;
    }

}
