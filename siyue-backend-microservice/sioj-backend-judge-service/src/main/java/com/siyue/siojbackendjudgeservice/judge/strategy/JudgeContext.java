package com.siyue.siojbackendjudgeservice.judge.strategy;

import com.siyue.siojbackendmodel.model.codesandbox.JudgeInfo;
import com.siyue.siojbackendmodel.model.dto.question.JudgeCase;
import com.siyue.siojbackendmodel.model.entity.Question;
import com.siyue.siojbackendmodel.model.entity.QuestionSubmit;
import lombok.Data;

import java.util.List;

/**
 * 判题上下文
 */
@Data
public class JudgeContext {
    private JudgeInfo judgeInfo;

    private List<String> inputList;

    private List<String> outputList;

    private List<JudgeCase> judgeCaseList;

    private Question question;

    private QuestionSubmit questionSubmit;

}
