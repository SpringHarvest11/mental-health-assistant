package com.nie.mentalserver.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.nie.mental.common.exception.BusinessException;
import com.nie.mental.common.result.PageResult;
import com.nie.mental.common.utils.CurrentHolder;
import com.nie.mental.common.enumeration.UserType;
import com.nie.mentalpojo.dto.ArticleCreateDTO;
import com.nie.mentalpojo.dto.ArticlePageQueryDTO;
import com.nie.mentalpojo.dto.ArticleUpdateDTO;
import com.nie.mentalpojo.entity.KnowledgeArticle;
import com.nie.mentalpojo.entity.KnowledgeCategory;
import com.nie.mentalpojo.vo.KnowledgeArticleDetailVO;
import com.nie.mentalpojo.vo.KnowledgeArticlePageVO;
import com.nie.mentalpojo.vo.KnowledgeCategoryTreeVO;
import com.nie.mentalserver.mapper.KnowledgeMapper;
import com.nie.mentalserver.service.KnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    /**
     * 新增文章
     * @return 文章 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createArticle(ArticleCreateDTO dto) {
        if (knowledgeMapper.countEnabledCategory(dto.getCategoryId()) == 0) {
            throw new BusinessException("分类不存在或已停用");
        }

        String id = StringUtils.hasText(dto.getId())
                ? dto.getId().trim()
                : UUID.randomUUID().toString();
        if (knowledgeMapper.countArticle(id) > 0) {
            throw new BusinessException("文章已存在，请勿重复提交");
        }

        KnowledgeArticle article = KnowledgeArticle.builder()
                .id(id)
                .categoryId(dto.getCategoryId())
                .title(dto.getTitle().trim())
                .summary(StringUtils.hasText(dto.getSummary()) ? dto.getSummary().trim() : null)
                .content(dto.getContent())
                .coverImage(StringUtils.hasText(dto.getCoverImage()) ? dto.getCoverImage().trim() : null)
                .tags(StringUtils.hasText(dto.getTags()) ? dto.getTags().trim() : null)
                .authorId(CurrentHolder.getUserId())
                .readCount(0)
                .status(1)
                .publishedAt(LocalDateTime.now())
                .build();

        knowledgeMapper.insert(article);
        return id;
    }

    /**
     * 文章详情；非管理员只能查看已发布文章，且会累加阅读量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeArticleDetailVO articleDetail(String id, Integer userType) {
        KnowledgeArticleDetailVO detail = knowledgeMapper.detail(id);
        boolean admin = UserType.MANAGER.getUserType().equals(userType);
        if (detail == null || (!admin && !Integer.valueOf(1).equals(detail.getStatus()))) {
            throw new BusinessException("文章不存在或暂未发布");
        }

        // 管理员查看用于编辑，不累加阅读量
        if (!admin) {
            knowledgeMapper.increaseReadCount(id);
            detail.setReadCount(detail.getReadCount() + 1);
        }

        detail.setTagArray(splitTags(detail.getTags()));
        return detail;
    }

    /**
     * 更新文章基础信息（分类、标题、摘要、正文、封面、标签）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArticle(String id, ArticleUpdateDTO dto) {
        if (knowledgeMapper.countArticle(id) == 0) {
            throw new BusinessException("文章不存在");
        }
        if (knowledgeMapper.countEnabledCategory(dto.getCategoryId()) == 0) {
            throw new BusinessException("分类不存在或已停用");
        }

        KnowledgeArticle article = KnowledgeArticle.builder()
                .id(id)
                .categoryId(dto.getCategoryId())
                .title(dto.getTitle().trim())
                .summary(StringUtils.hasText(dto.getSummary()) ? dto.getSummary().trim() : null)
                .content(dto.getContent())
                .coverImage(StringUtils.hasText(dto.getCoverImage()) ? dto.getCoverImage().trim() : null)
                .tags(StringUtils.hasText(dto.getTags()) ? dto.getTags().trim() : null)
                .build();

        knowledgeMapper.update(article);
    }

    /**
     * 发布（status=1）或下线（status=2）文章
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArticleStatus(String id, Integer status) {
        if (knowledgeMapper.countArticle(id) == 0) {
            throw new BusinessException("文章不存在");
        }
        // 首次发布时写入发布时间，重新发布保留原始发布时间
        knowledgeMapper.updateStatus(id, status);
    }

    /**
     * 删除文章（用户收藏由数据库级联删除）
     */
    @Override
    public void deleteArticle(String id) {
        if (knowledgeMapper.deleteById(id) == 0) {
            throw new BusinessException("文章不存在");
        }
    }

    private List<String> splitTags(String tags) {
        List<String> tagArray = new ArrayList<>();
        if (!StringUtils.hasText(tags)) {
            return tagArray;
        }
        for (String tag : tags.split(",")) {
            String trimmed = tag.trim();
            if (!trimmed.isEmpty()) {
                tagArray.add(trimmed);
            }
        }
        return tagArray;
    }
}
