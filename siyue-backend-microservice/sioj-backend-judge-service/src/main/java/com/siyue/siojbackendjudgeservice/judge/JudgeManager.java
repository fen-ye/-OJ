package com.siyue.siojbackendjudgeservice.judge;

import com.siyue.siojbackendjudgeservice.judge.strategy.DefaultJudgeStrategyImpl;
import com.siyue.siojbackendjudgeservice.judge.strategy.JavaJudgeStrategyImpl;
import com.siyue.siojbackendjudgeservice.judge.strategy.JudgeContext;
import com.siyue.siojbackendjudgeservice.judge.strategy.JudgeStrategy;
import com.siyue.siojbackendmodel.model.codesandbox.JudgeInfo;
import com.siyue.siojbackendmodel.model.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

/**
 * 判题管理 服务 可简化调用
 */
@Service
public class JudgeManager {

    /**
     * 执行判题操作，根据提交的编程语言选择合适的判题策略，并调用相应的策略进行判题。
     *
     * @param judgeContext 判题上下文对象，包含判题所需的所有信息，如提交的代码、语言等。
     * @return 返回判题结果信息，包含判题结果、执行时间、内存消耗等。
     */
    JudgeInfo doJudge(JudgeContext judgeContext) {
        // 获取提交的题目信息
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        // 获取提交的编程语言
        String language = questionSubmit.getLanguage();

        // 默认使用默认的判题策略
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategyImpl();

        // 如果提交的语言是Java，则使用Java判题策略
        if ("java".equals(language)) {
            judgeStrategy = new JavaJudgeStrategyImpl();
        }

        // 调用选择的判题策略进行判题，并返回判题结果
        return judgeStrategy.doJudge(judgeContext);
    }

}
