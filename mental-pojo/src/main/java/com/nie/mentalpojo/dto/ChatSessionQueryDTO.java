package com.nie.mentalpojo.dto;

import lombok.Data;

/**
 * 会话分页查询参数
 */
@Data
public class ChatSessionQueryDTO {

    //用户端参数
    private Integer pageNum;
    private Integer pageSize;

    //管理端参数
    private Integer currentPage;
    private Integer size;
}
