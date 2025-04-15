package com.siyue.siojcodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ExecuteCodeRequest 类用于封装执行代码的请求信息。
 * 该类包含了执行代码所需的输入列表、代码内容以及编程语言。
 *
 * 使用 Lombok 注解自动生成 getter、setter、builder 以及构造函数。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeRequest {

    /**
     * 输入列表，包含执行代码时所需的输入数据。
     */
    private List<String> inputList;

    /**
     * 需要执行的代码内容。
     */
    private String code;

    /**
     * 代码所使用的编程语言。
     */
    private String language;
}

