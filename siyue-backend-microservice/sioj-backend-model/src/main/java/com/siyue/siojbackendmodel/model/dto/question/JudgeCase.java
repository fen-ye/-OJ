package com.siyue.siojbackendmodel.model.dto.question;

import lombok.Data;

@Data
public class JudgeCase {
    /**
     * 输入用例
     */
    String input;
    /**
     * 输出用例
     */
    String output;
}
