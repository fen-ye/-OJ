package com.siyue.siojcodesandbox;

import com.siyue.siojcodesandbox.model.ExecuteCodeRequest;
import com.siyue.siojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.stereotype.Component;

/**
 * 继承 JavaCodeSandBoxTemplate，使用模板方法模式。
 * Java 原生代码沙箱实现（直接复用模板代码）
 */
@Component
public class JavaNativeCodeSandBoxTemplateImpl extends JavaCodeSandBoxTemplate {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }
}
