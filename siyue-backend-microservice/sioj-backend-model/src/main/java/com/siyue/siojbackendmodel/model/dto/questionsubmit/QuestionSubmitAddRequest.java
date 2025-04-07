package com.siyue.siojbackendmodel.model.dto.questionsubmit;

import lombok.Data;

import java.io.Serializable;


/**
 * 题目提交请求
 */
@Data
public class QuestionSubmitAddRequest implements Serializable {

    /**
     * 编程语言
     */
    private String language;

    /**
     * 用户代码
     */
    private String code;

    /**
     * 题目 id
     */
    private Long questionId;

}