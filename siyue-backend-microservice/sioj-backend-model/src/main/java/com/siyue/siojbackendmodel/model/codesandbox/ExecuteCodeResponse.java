package com.siyue.siojbackendmodel.model.codesandbox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ExecuteCodeResponse 类用于表示代码执行后的响应信息。
 * 该类包含了代码执行的输出结果、状态信息以及相关的判题信息。
 *
 * 使用 Lombok 注解简化了 getter、setter、构造器等方法的生成。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeResponse {

    /**
     * 代码执行后的输出结果列表。
     * 每个元素代表一行输出。
     */
    private List<String> outputList;

    /**
     * 代码执行后的消息信息。
     * 通常用于描述执行结果或错误信息。
     */
    private String message;

    /**
     * 代码执行的状态码。
     * 用于表示执行的成功或失败状态。
     */
    private Integer status;

    /**
     * 代码执行后的判题信息。
     * 包含了与判题相关的详细信息，如执行时间、内存使用等。
     */
    public JudgeInfo judgeInfo;
}

