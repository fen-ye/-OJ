package com.siyue.siojbackendserviceclient.service;

import com.siyue.siojbackendmodel.model.entity.Question;
import com.siyue.siojbackendmodel.model.entity.QuestionSubmit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
* @author 81486
* @description 针对表【question(题目)】的数据库操作Service
* @createDate 2025-03-27 17:16:54
 *其实呢就是说其他服务调用这个接口然后就可以在对应的服务里面去实现这个Feign，毕竟要用其他服务的service
*/
@FeignClient(name = "sioj-backend-question-service", path = "/api/question/inner")
public interface QuestionFeignClient {

    @GetMapping("/get/id")
    Question getQuestionById(@RequestParam("questionId") long questionId);

    @GetMapping("/question_submit/get/id")
    QuestionSubmit getQuestionSubmitById(@RequestParam("questionSubmitId") long questionSubmitId);

    @PostMapping("/question_submit/update")
    boolean updateQuestionSubmitById(@RequestBody QuestionSubmit questionSubmit);

}
