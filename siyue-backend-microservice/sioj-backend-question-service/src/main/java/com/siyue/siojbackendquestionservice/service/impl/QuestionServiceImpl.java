package com.siyue.siojbackendquestionservice.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siyue.siojbackendcommon.common.ErrorCode;
import com.siyue.siojbackendcommon.constant.CommonConstant;
import com.siyue.siojbackendcommon.exception.BusinessException;
import com.siyue.siojbackendcommon.exception.ThrowUtils;
import com.siyue.siojbackendcommon.utils.SqlUtils;
import com.siyue.siojbackendmodel.model.dto.question.QuestionQueryRequest;
import com.siyue.siojbackendmodel.model.entity.Question;
import com.siyue.siojbackendmodel.model.entity.User;
import com.siyue.siojbackendmodel.model.vo.QuestionVO;
import com.siyue.siojbackendmodel.model.vo.UserVO;
import com.siyue.siojbackendquestionservice.mapper.QuestionMapper;
import com.siyue.siojbackendquestionservice.service.QuestionService;
import com.siyue.siojbackendserviceclient.service.UserFeignClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 81486
 * @description 针对表【question(题目)】的数据库操作Service实现
 * @createDate 2025-03-27 17:16:54
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
        implements QuestionService {

    @Resource
    private UserFeignClient userFeignClient;

    /**
     * 验证问题对象是否有效
     *
     * @param question 问题对象，包含标题、内容和标签等信息
     * @param add      表示是否在添加新问题时进行验证
     * @throws BusinessException 当参数为空或参数长度超过限制时抛出业务异常
     */
    @Override
    public void validQuestion(Question question, boolean add) {
        // 检查问题对象是否为null
        if (question == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String title = question.getTitle();
        String content = question.getContent();
        String tags = question.getTags();
        String answer = question.getAnswer();
        String judgeCase = question.getJudgeCase();
        String judgeConfig = question.getJudgeConfig();
        // 在添加新问题时，检查标题、内容和标签是否为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(title, content, tags), ErrorCode.PARAMS_ERROR);
        }
        // 检查标题长度是否超过限制
        if (StringUtils.isNotBlank(title) && title.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        // 检查内容长度是否超过限制
        if (StringUtils.isNotBlank(content) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
        }

        // 检查答案长度是否超过限制
        if (StringUtils.isNotBlank(answer) && answer.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "答案过长");
        }
        // 检查判题用例长度是否超过限制
        if (StringUtils.isNotBlank(judgeCase) && judgeCase.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "判题用例过长");
        }
        // 检查判题配置长度是否超过限制
        if (StringUtils.isNotBlank(judgeConfig) && judgeConfig.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "判题配置过长");
        }
    }

    /**
     * 获取查询包装类（用户根据哪些字段查询，根据前端传来的请求对象，得到mybatis框架支持的查询类）
     *
     * @param questionQueryRequest 前端传递的查询请求对象，包含查询条件和分页信息
     * @return QueryWrapper<Question> 返回MyBatis框架支持的查询包装类，用于构建SQL查询条件
     */
    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null) {
            return queryWrapper;
        }
        Long id = questionQueryRequest.getId();
        String title = questionQueryRequest.getTitle();
        String content = questionQueryRequest.getContent();
        List<String> tags = questionQueryRequest.getTags();
        String answer = questionQueryRequest.getAnswer();
        Long userId = questionQueryRequest.getUserId();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();

        // 根据请求对象中的字段，动态拼接查询条件
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        queryWrapper.like(StringUtils.isNotBlank(answer), "answer", answer);
        if (CollectionUtils.isNotEmpty(tags)) {
            // 遍历标签列表，为每个标签添加查询条件
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        // 根据排序字段和排序顺序，添加排序条件
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


    /**
     * 根据给定的Question对象和HttpServletRequest对象，生成并返回对应的QuestionVO对象。
     * 该函数主要用于将Question对象转换为QuestionVO对象，并关联查询用户信息。
     *
     * @param question 需要转换的Question对象，包含问题的基本信息。
     * @param request HttpServletRequest对象，用于处理HTTP请求（当前代码中未使用）。
     * @return 返回生成的QuestionVO对象，包含问题信息及关联的用户信息。
     */
    @Override
    public QuestionVO getQuestionVO(Question question, HttpServletRequest request) {
        // 将Question对象转换为QuestionVO对象
        QuestionVO questionVO = QuestionVO.objToVo(question);
        long questionId = question.getId();

        // 关联查询用户信息
        Long userId = question.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userFeignClient.getById(userId);
        }
        // 将User对象转换为UserVO对象，并设置到QuestionVO中
        UserVO userVO = userFeignClient.getUserVO(user);
        questionVO.setUserVO(userVO);

        return questionVO;
    }

    /**
     * 将分页的Question对象转换为分页的QuestionVO对象，并填充用户信息。
     *
     * @param questionPage 包含Question对象的分页数据
     * @param request HTTP请求对象，用于获取请求上下文信息
     * @return 包含QuestionVO对象的分页数据，QuestionVO中已填充用户信息
     */
    @Override
    public Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request) {
        // 获取分页中的Question列表
        List<Question> questionList = questionPage.getRecords();
        // 创建一个新的分页对象，用于存储转换后的QuestionVO对象
        Page<QuestionVO> questionVOPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());

        // 如果Question列表为空，直接返回空的分页对象
        if (CollUtil.isEmpty(questionList)) {
            return questionVOPage;
        }

        // 关联查询用户信息
        // 提取所有Question对象中的用户ID，并去重
        Set<Long> userIdSet = questionList.stream().map(Question::getUserId).collect(Collectors.toSet());
        // 根据用户ID批量查询用户信息，并按用户ID分组
        Map<Long, List<User>> userIdUserListMap = userFeignClient.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        // 填充信息
        // 将Question对象转换为QuestionVO对象，并填充对应的用户信息
        List<QuestionVO> questionVOList = questionList.stream().map(question -> {
            QuestionVO questionVO = QuestionVO.objToVo(question);
            Long userId = question.getUserId();
            User user = null;
            // 如果用户信息存在，则获取第一个用户对象
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            // 将用户信息转换为UserVO并设置到QuestionVO中
            questionVO.setUserVO(userFeignClient.getUserVO(user));
            return questionVO;
        }).collect(Collectors.toList());

        // 将填充后的QuestionVO列表设置到分页对象中
        questionVOPage.setRecords(questionVOList);
        return questionVOPage;
    }

}




