package com.siyue.siojbackendjudgeservice.judge;

import cn.hutool.json.JSONUtil;
import com.siyue.siojbackendcommon.common.ErrorCode;
import com.siyue.siojbackendcommon.exception.BusinessException;
import com.siyue.siojbackendjudgeservice.judge.codesandbox.CodeSandbox;
import com.siyue.siojbackendjudgeservice.judge.codesandbox.CodeSandboxFactory;
import com.siyue.siojbackendjudgeservice.judge.codesandbox.CodeSandboxProxy;
import com.siyue.siojbackendjudgeservice.judge.strategy.JudgeContext;
import com.siyue.siojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import com.siyue.siojbackendmodel.model.codesandbox.ExecuteCodeResponse;
import com.siyue.siojbackendmodel.model.codesandbox.JudgeInfo;
import com.siyue.siojbackendmodel.model.dto.question.JudgeCase;
import com.siyue.siojbackendmodel.model.entity.Question;
import com.siyue.siojbackendmodel.model.entity.QuestionSubmit;
import com.siyue.siojbackendmodel.model.enums.QuestionSubmitStatusEnum;
import com.siyue.siojbackendserviceclient.service.QuestionFeignClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService {

    @Resource
    private QuestionFeignClient questionFeignClient;
    @Resource
    private JudgeManager judgeManager;

    @Value("${codesandbox.type:example}")
    private String type;

    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {
        QuestionSubmit questionSubmit = questionFeignClient.getQuestionSubmitById(questionSubmitId);
        if (questionSubmit == null)
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目提交不存在");
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionFeignClient.getQuestionById(questionId);
        if (question == null)
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        Integer status = questionSubmit.getStatus();
        if (!status.equals(QuestionSubmitStatusEnum.WAITING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目正在判题中");
        }
        QuestionSubmit questionSubmitNew = new QuestionSubmit();
        questionSubmitNew.setId(questionSubmitId);
        questionSubmitNew.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        // 这一步更新只是把数据库中的这条记录更改成为题目正在判题中，防止重复判题
        boolean updateById = questionFeignClient.updateQuestionSubmitById(questionSubmitNew);
        if (!updateById)
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新失败");

        // 调用代码沙箱
        CodeSandbox codeSandbox =  CodeSandboxFactory.newInstance(type);
        codeSandbox =  new CodeSandboxProxy(codeSandbox);
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCases = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        String code = questionSubmit.getCode();
        String language = questionSubmit.getLanguage();
        List<String> inputList = judgeCases.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        List<String> outputList = executeCodeResponse.getOutputList();
        System.out.println("executeCodeResponse.getJudgeInfo()" + executeCodeResponse.getJudgeInfo());

        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
        judgeContext.setInputList(inputList);
        judgeContext.setOutputList(outputList);
        judgeContext.setJudgeCaseList(judgeCases);
        judgeContext.setQuestion(question);
        judgeContext.setQuestionSubmit(questionSubmit);

        JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);

        // 修改数据库的判题结果
        questionSubmitNew = new QuestionSubmit();
        questionSubmitNew.setId(questionSubmitId);
        questionSubmitNew.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        questionSubmitNew.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        boolean updateSubmit = questionFeignClient.updateQuestionSubmitById(questionSubmitNew);
        if (!updateSubmit)
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新失败");
        return questionFeignClient.getQuestionSubmitById(questionSubmitId);
    }
}
