package com.siyue.siojbackendquestionservice.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siyue.siojbackendcommon.common.ErrorCode;
import com.siyue.siojbackendcommon.constant.CommonConstant;
import com.siyue.siojbackendcommon.exception.BusinessException;
import com.siyue.siojbackendcommon.utils.SqlUtils;
import com.siyue.siojbackendmodel.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.siyue.siojbackendmodel.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.siyue.siojbackendmodel.model.entity.Question;
import com.siyue.siojbackendmodel.model.entity.QuestionSubmit;
import com.siyue.siojbackendmodel.model.entity.User;
import com.siyue.siojbackendmodel.model.enums.QuestionSubmitLanguageEnum;
import com.siyue.siojbackendmodel.model.enums.QuestionSubmitStatusEnum;
import com.siyue.siojbackendmodel.model.vo.QuestionSubmitVO;
import com.siyue.siojbackendquestionservice.mapper.QuestionSubmitMapper;
import com.siyue.siojbackendquestionservice.rabbitmq.MyMessageProducer;
import com.siyue.siojbackendquestionservice.service.QuestionService;
import com.siyue.siojbackendquestionservice.service.QuestionSubmitService;
import com.siyue.siojbackendserviceclient.service.JudeFeignClient;
import com.siyue.siojbackendserviceclient.service.QuestionFeignClient;
import com.siyue.siojbackendserviceclient.service.UserFeignClient;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
* @author 81486
* @description 针对表【question_submit(题目提交)】的数据库操作Service实现
* @createDate 2025-03-27 17:17:29
*/
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
    implements QuestionSubmitService {
    @Resource
    private QuestionService questionService;

    @Resource
    private UserFeignClient userService;

    @Resource
    @Lazy
    private JudeFeignClient judeService;

    @Resource
    private MyMessageProducer myMessageProducer;

    /**
     * 提交题目
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        // todo 校验编程语言是否合法
        String language = questionSubmitAddRequest.getLanguage();
        // 判断编程语言是否合法
        if (QuestionSubmitLanguageEnum.getEnumByValue(language) == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言错误");
        long questionId = questionSubmitAddRequest.getQuestionId();
        // 根据题目ID获取题目信息
        Question question = questionService.getById(questionId);
        if (question == null) {
            // 如果题目不存在，抛出业务异常
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 获取当前用户的ID
        long userId = loginUser.getId();
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setUserId(userId);
        questionSubmit.setLanguage(language);
        questionSubmit.setCode(questionSubmitAddRequest.getCode());
        // 设置初始状态
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
        questionSubmit.setJudgeInfo("{}");
        questionSubmit.setQuestionId(questionSubmitAddRequest.getQuestionId());
        questionSubmit.setUserId(loginUser.getId());

        boolean save = this.save(questionSubmit);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目提交失败");
        }
        Long questionSubmitId = questionSubmit.getId();
        // 消息队列发送,发送消息
        // 发送消息
        myMessageProducer.sendMessage("code_exchange", "my_routingKey", String.valueOf(questionSubmitId));

        // todo 执行判题服务
//        CompletableFuture.runAsync(() -> {
//            judeService.doJudge(questionSubmitId);
//        });

        return questionSubmitId;
    }

    /**
     * 获取查询包装类，用于根据前端传入的查询条件构建MyBatis框架支持的查询类。
     *
     * @param questionSubmitQueryRequest 前端传入的查询请求对象，包含查询条件如语言、状态、问题ID、用户ID等。
     * @return QueryWrapper<QuestionSubmit> 返回构建好的查询包装类，用于MyBatis查询。
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        // 初始化查询包装类
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }

        // 从请求对象中提取查询条件
        String language = questionSubmitQueryRequest.getLanguage();
        Integer status = questionSubmitQueryRequest.getStatus();
        Long questionId = questionSubmitQueryRequest.getQuestionId();
        Long userId = questionSubmitQueryRequest.getUserId();
        int current = questionSubmitQueryRequest.getCurrent();
        int pageSize = questionSubmitQueryRequest.getPageSize();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();

        // 根据提取的条件构建查询条件
        queryWrapper.eq(StringUtils.isNotBlank(language), "language", language);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(QuestionSubmitStatusEnum.getEnumByValue(status) != null, "status", status);
        queryWrapper.eq("isDelete", false);

        // 根据排序字段和排序顺序，添加排序条件
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);

        return queryWrapper;
    }



    /**
     * 将QuestionSubmit对象转换为QuestionSubmitVO对象，并根据当前登录用户的权限决定是否隐藏代码信息。
     *
     * @param questionSubmit 需要转换的QuestionSubmit对象，包含提交的题目信息
     * @param loginUser HttpServletRequest对象，用于获取当前登录用户的信息
     * @return 返回转换后的QuestionSubmitVO对象，包含用户信息和提交的题目信息
     */
    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser) {
        // 将QuestionSubmit对象转换为QuestionSubmitVO对象
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
        long userId = loginUser.getId();

        // 如果当前用户ID与QuestionSubmit对象的用户ID相同，并且当前用户不是管理员，则隐藏代码信息
        if (userId == questionSubmit.getUserId() && !userService.isAdmin(loginUser)) {
            questionSubmitVO.setCode(null);
        }

        return questionSubmitVO;
    }



    /**
     * 将分页的QuestionSubmit对象转换为分页的QuestionSubmitVO对象，并填充用户信息。
     *
     * @param questionSubmitPage 包含QuestionSubmit对象的分页数据
     * @param loginUser HTTP请求对象，用于获取请求上下文信息
     * @return 包含QuestionSubmitVO对象的分页数据，QuestionSubmitVO中已填充用户信息
     */
    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser) {
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());

        if (CollUtil.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream().map(questionSubmit -> {
            return getQuestionSubmitVO(questionSubmit, loginUser);
        }).collect(Collectors.toList());
        questionSubmitVOPage.setRecords(questionSubmitVOList);
        return questionSubmitVOPage;
    }
}




