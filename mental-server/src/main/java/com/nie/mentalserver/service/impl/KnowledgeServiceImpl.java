package com.nie.mentalserver.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.nie.mental.common.result.PageResult;
import com.nie.mentalpojo.dto.ArticlePageQueryDTO;
import com.nie.mentalpojo.entity.KnowledgeCategory;
import com.nie.mentalpojo.vo.KnowledgeArticlePageVO;
import com.nie.mentalpojo.vo.KnowledgeCategoryTreeVO;
import com.nie.mentalserver.mapper.KnowledgeMapper;
import com.nie.mentalserver.service.KnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class KnowledgeServiceImpl implements KnowledgeService {

    @Autowired
    private KnowledgeMapper knowledgeMapper;

    /**
     * 文章分类树
     * @return
     */
    @Override
    public List<KnowledgeCategoryTreeVO> categoryTree() {
        // 1. 查询所有启用的分类（status=1），已经按sort_order升序排好序
        List<KnowledgeCategory> categories = knowledgeMapper.listEnabled();

        // 2. LinkedHashMap：用来缓存所有节点，key=分类id，value=树形VO对象
        // LinkedHashMap 保留插入顺序，保证排序和数据库一致
        Map<Integer, KnowledgeCategoryTreeVO> nodeMap = new LinkedHashMap<>();
        for (KnowledgeCategory category : categories) {
            nodeMap.put(category.getId(), new KnowledgeCategoryTreeVO(
                    category.getId(),
                    category.getCategoryName(),
                    new ArrayList<>()
            ));
        }

        // 3. 根节点集合，最终返回给前端
        List<KnowledgeCategoryTreeVO> roots = new ArrayList<>();
        for (KnowledgeCategory category : categories) {
            // 拿到当前节点VO
            KnowledgeCategoryTreeVO node = nodeMap.get(category.getId());
            // 获取父id
            Integer parentId = category.getParentId();
            // 从map里找父节点
            KnowledgeCategoryTreeVO parent = parentId == null || parentId == 0 ? null : nodeMap.get(parentId);

            if (parent == null) {
                // 父节点不存在 → 当前是根节点，加入roots
                roots.add(node);
            } else {
                // 找到父节点，把当前节点加到父节点的children子列表
                parent.getChildren().add(node);
            }
        }
        return roots;
    }

    /**
     * 文章分页
     * @param query
     * @return
     */
    @Override
    public PageResult<KnowledgeArticlePageVO> pageArticles(Integer userType, ArticlePageQueryDTO query){

        boolean admin = Integer.valueOf(2).equals(userType);
        // 游客和普通用户只能看已发布；管理员不传 status 时查询全部状态
        Integer status = 1;
        if (admin) {
            status = query.getStatus();
        }

        Integer page = query.getCurrentPage();
        page = page == null || page <= 0 ? 1 : page;
        Integer size = query.getSize();
        size = size == null ||size <= 0 ||size >10 ? 10 : size;

        String sortField;
        String sortDirection;
        if (admin) {
            sortField = "readCount";
            sortDirection = "desc";
        } else {
            sortField = "readCount".equals(query.getSortField()) ? "readCount" : "publishedAt";
            sortDirection = "asc".equalsIgnoreCase(query.getSortDirection()) ? "asc" : "desc";
        }

        String title = StringUtils.hasText(query.getTitle()) ? query.getTitle().trim() : null;

        PageHelper.startPage(page,size);
        Page<KnowledgeArticlePageVO> list =(Page<KnowledgeArticlePageVO>) knowledgeMapper.page(title,query.getCategoryId(),status,sortField,sortDirection);
        return new PageResult<>(list.getTotal(),list.getResult());
    }
}
