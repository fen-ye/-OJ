package com.siyue.siojbackendjudgeservice.judge;


import com.siyue.siojbackendmodel.model.entity.QuestionSubmit;

/**
 * 判题服务
 */
public interface JudgeService {

    QuestionSubmit doJudge(long questionSubmitId);
}
