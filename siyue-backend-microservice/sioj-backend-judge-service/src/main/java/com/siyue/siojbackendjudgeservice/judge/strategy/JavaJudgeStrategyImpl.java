package com.siyue.siojbackendjudgeservice.judge.strategy;

import cn.hutool.json.JSONUtil;
import com.siyue.siojbackendmodel.model.codesandbox.JudgeInfo;
import com.siyue.siojbackendmodel.model.dto.question.JudgeCase;
import com.siyue.siojbackendmodel.model.dto.question.JudgeConfig;
import com.siyue.siojbackendmodel.model.entity.Question;
import com.siyue.siojbackendmodel.model.enums.JudgeInfoMessageEnum;

import java.util.List;
import java.util.Optional;

public class JavaJudgeStrategyImpl implements JudgeStrategy {
    /**
     * 执行判题逻辑，根据判题上下文信息生成判题结果。
     *
     * @param judgeContext 判题上下文，包含判题所需的所有信息，如输入输出列表、题目信息、判题用例等。
     * @return JudgeInfo 返回判题结果，包含内存使用、时间消耗以及判题状态信息。
     */
    @Override
    public JudgeInfo doJudge(JudgeContext judgeContext) {

        // 从判题上下文中获取判题信息、输入输出列表、题目信息及判题用例
        JudgeInfo judgeInfo = judgeContext.getJudgeInfo();
        Long memory = Optional.ofNullable(judgeInfo.getMemory()).orElse(0L);
        Long time = Optional.ofNullable(judgeInfo.getTime()).orElse(0L);
        List<String> inputList = judgeContext.getInputList();
        List<String> outputList = judgeContext.getOutputList();
        Question question = judgeContext.getQuestion();
        List<JudgeCase> judgeCases = judgeContext.getJudgeCaseList();

        // 初始化判题结果为“通过”
        JudgeInfoMessageEnum judgeInfoMessageEnum = JudgeInfoMessageEnum.ACCEPTED;
        JudgeInfo judgeInfoResponse = new JudgeInfo();

        // 设置判题结果的内存和时间信息
        judgeInfoResponse.setMemory(memory);
        judgeInfoResponse.setTime(time);
        judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());

        // 检查输出列表与输入列表的长度是否一致，若不一致则返回“答案错误”
        if (outputList.size() != inputList.size()) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }

        // 遍历判题用例，检查每个用例的输出是否与预期输出一致，若不一致则返回“答案错误”
        for (int i = 0; i < judgeCases.size(); i++) {
            if (!judgeCases.get(i).getOutput().equals(outputList.get(i))) {
                judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
                judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
                return judgeInfoResponse;
            }
        }

        // 从题目信息中获取判题配置，包括时间限制和内存限制
        String judgeConfigStr = question.getJudgeConfig();
        JudgeConfig judgeConfig = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);
        long timeLimit = judgeConfig.getTimeLimit();
        long memoryLimit = judgeConfig.getMemoryLimit();

        // 检查时间是否超出限制，若超出则设置判题结果为“时间超限”
        // 例如 java可能本身需要额外的1秒中
        long JAVA_PROCESS_TIME = 1000L;
        if (time - JAVA_PROCESS_TIME> timeLimit) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
        }

        // 检查内存是否超出限制，若超出则设置判题结果为“内存超限”
        if (memory > memoryLimit) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
        }

        // 返回最终的判题结果
        return judgeInfoResponse;
    }
}
