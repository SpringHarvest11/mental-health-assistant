package com.nie.mentalpojo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArticleStatusDTO {

    //1 发布，2 下线
    @NotNull(message = "请选择要变更的状态")
    @Min(value = 1, message = "状态只能是1（发布）或2（下线）")
    @Max(value = 2, message = "状态只能是1（发布）或2（下线）")
    private Integer status;
}
