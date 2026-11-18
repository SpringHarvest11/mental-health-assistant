package com.nie.mentalpojo.dto;

import lombok.Data;

@Data
public class ArticlePageQueryDTO {

    //当前页，从 1 开始
    private Integer currentPage;
    //每页条数
    private Integer size;
    //总条数 前端会顺带传，后端忽略即可
    private Integer total;

    //标题模糊查询
    private String title;
    //分类 ID
    private Integer categoryId;
    //"0" 草稿 / "1" 已发布 / "2" 已下线（注意是字符串）
    private Integer status;

    //publishedAt 或 readCount
    private String sortField;
    //desc 或 asc
    private String sortDirection;



}
