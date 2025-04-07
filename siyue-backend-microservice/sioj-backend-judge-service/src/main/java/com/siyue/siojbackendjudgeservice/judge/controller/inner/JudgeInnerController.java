package com.siyue.siojbackendjudgeservice.judge.controller.inner;

import com.siyue.siojbackendjudgeservice.judge.JudgeService;
import com.siyue.siojbackendmodel.model.entity.QuestionSubmit;
import com.siyue.siojbackendserviceclient.service.JudeFeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/inner")
public class JudgeInnerController implements JudeFeignClient {


    @Resource
    private JudgeService judgeService;

    @PostMapping("/do")
    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {
        return judgeService.doJudge(questionSubmitId);
    }
}
