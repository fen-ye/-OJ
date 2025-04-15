package com.siyue.siojcodesandbox;


import com.siyue.siojcodesandbox.model.ExecuteCodeRequest;
import com.siyue.siojcodesandbox.model.ExecuteCodeResponse;

/**
 * CodeSandbox 接口定义了一个执行代码的方法。
 * 该接口通常用于在沙箱环境中执行代码请求，并返回执行结果。
 */
public interface CodeSandbox {

    /**
     * 执行代码请求并返回执行结果。
     *
     * @param executeCodeRequest 包含代码执行请求的参数，如代码内容、输入数据等。
     * @return 返回代码执行的结果，包含输出、执行状态等信息。
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}

