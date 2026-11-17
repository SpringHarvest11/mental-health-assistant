package com.nie.mentalserver.mapper;

import com.nie.mentalpojo.entity.KnowledgeCategory;
import com.nie.mentalpojo.vo.KnowledgeCategoryTreeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface KnowledgeMapper {

    @Select("""
            select id, parent_id, category_name, category_code, description,
                   sort_order, status, created_at, updated_at
            from knowledge_category
            where status = 1
            order by sort_order asc, id asc
            """)
    List<KnowledgeCategory> listEnabled();

}

