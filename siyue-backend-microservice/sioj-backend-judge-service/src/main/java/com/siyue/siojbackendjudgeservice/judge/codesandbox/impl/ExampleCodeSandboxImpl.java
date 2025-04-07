package com.siyue.siojbackendjudgeservice.judge.codesandbox.impl;


import com.siyue.siojbackendjudgeservice.judge.codesandbox.CodeSandbox;
import com.siyue.siojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import com.siyue.siojbackendmodel.model.codesandbox.ExecuteCodeResponse;
import com.siyue.siojbackendmodel.model.codesandbox.JudgeInfo;
import com.siyue.siojbackendmodel.model.enums.JudgeInfoMessageEnum;
import com.siyue.siojbackendmodel.model.enums.QuestionSubmitStatusEnum;

import java.util.List;

/**
 * 示例代码沙箱
 */
public class ExampleCodeSandboxImpl implements CodeSandbox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> inputList = executeCodeRequest.getInputList();
        executeCodeResponse.setOutputList(inputList);
        executeCodeResponse.setMessage("测试示例执行成功！");
        executeCodeResponse.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        executeCodeResponse.setJudgeInfo(JudgeInfo.builder().message(JudgeInfoMessageEnum.ACCEPTED.getText()).memory(100L).time(100L).build());
        return executeCodeResponse;
    }
}
