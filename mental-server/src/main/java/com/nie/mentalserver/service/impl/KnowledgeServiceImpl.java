package com.nie.mentalserver.service.impl;

import com.nie.mentalpojo.entity.KnowledgeCategory;
import com.nie.mentalpojo.vo.KnowledgeCategoryTreeVO;
import com.nie.mentalserver.mapper.KnowledgeMapper;
import com.nie.mentalserver.service.KnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        List<KnowledgeCategory> categories = knowledgeMapper.listEnabled();

        Map<Integer, KnowledgeCategoryTreeVO> nodeMap = new LinkedHashMap<>();
        for (KnowledgeCategory category : categories) {
            nodeMap.put(category.getId(), new KnowledgeCategoryTreeVO(
                    category.getId(), category.getCategoryName(), new ArrayList<>()));
        }

        List<KnowledgeCategoryTreeVO> roots = new ArrayList<>();
        for (KnowledgeCategory category : categories) {
            KnowledgeCategoryTreeVO node = nodeMap.get(category.getId());
            Integer parentId = category.getParentId();
            KnowledgeCategoryTreeVO parent = parentId == null ? null : nodeMap.get(parentId);
            if (parent == null) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        return roots;
    }
}
