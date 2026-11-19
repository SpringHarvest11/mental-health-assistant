package com.nie.mentalserver.mapper;

import com.nie.mentalpojo.entity.KnowledgeArticle;
import com.nie.mentalpojo.entity.KnowledgeCategory;
import com.nie.mentalpojo.vo.KnowledgeArticleDetailVO;
import com.nie.mentalpojo.vo.KnowledgeArticlePageVO;
import com.nie.mentalpojo.vo.KnowledgeCategoryTreeVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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

    List<KnowledgeArticlePageVO> page(@Param("title") String title,
                                      @Param("categoryId") Integer categoryId,
                                      @Param("status") Integer status,
                                      @Param("sortField") String sortField,
                                      @Param("sortDirection") String sortDirection);

    @Select("select count(*) from knowledge_category where id = #{id} and status = 1")
    int countEnabledCategory(@Param("id") Integer id);

    KnowledgeArticleDetailVO detail(@Param("id") String id);

    @Update("update knowledge_article set read_count = coalesce(read_count, 0) + 1 where id = #{id}")
    int increaseReadCount(@Param("id") String id);

    @Select("select count(*) from knowledge_article where id = #{id}")
    int countArticle(@Param("id") String id);

    @Insert("""
            insert into knowledge_article
                (id, category_id, title, summary, content, cover_image, tags,
                 author_id, read_count, status, published_at)
            values
                (#{id}, #{categoryId}, #{title}, #{summary}, #{content}, #{coverImage}, #{tags},
                 #{authorId}, #{readCount}, #{status}, #{publishedAt})
            """)
    int insert(KnowledgeArticle article);

    @Update("""
            update knowledge_article
            set category_id = #{categoryId},
                title = #{title},
                summary = #{summary},
                content = #{content},
                cover_image = #{coverImage},
                tags = #{tags}
            where id = #{id}
            """)
    int update(KnowledgeArticle article);

    @Update("""
            update knowledge_article
            set status = #{status},
                published_at = case when #{status} = 1 then coalesce(published_at, now()) else published_at end
            where id = #{id}
            """)
    int updateStatus(@Param("id") String id, @Param("status") Integer status);

    @Delete("delete from knowledge_article where id = #{id}")
    int deleteById(@Param("id") String id);

}

