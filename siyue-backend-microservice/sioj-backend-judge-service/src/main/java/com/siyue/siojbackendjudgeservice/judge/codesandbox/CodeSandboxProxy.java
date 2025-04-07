package com.siyue.siojbackendjudgeservice.judge.codesandbox;

import com.siyue.siojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import com.siyue.siojbackendmodel.model.codesandbox.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 代理模式就是增强代码沙箱的实现，通过代理的方式，在调用代码沙箱之前和之后进行一些操作，如记录请求和响应信息等。（增强能力）
 * CodeSandboxProxy 类是一个代理类，用于代理 CodeSandbox 接口的实现。
 * 该类通过日志记录代码沙箱的请求和响应信息，便于调试和监控。
 * 好处：不用改变原本的代码沙箱实现类，而且对于调用者来说，调用方式几乎没有改变，也不需要在每个调用沙箱的地方写统计代码。
 */
@Slf4j
//@AllArgsConstructor
public class CodeSandboxProxy implements CodeSandbox {

    private final CodeSandbox codeSandbox;

    public CodeSandboxProxy(CodeSandbox codeSandbox) {
        this.codeSandbox = codeSandbox;
    }

    /**
     *
     * 执行代码请求并返回执行结果。
     * 该方法首先记录请求参数，然后调用实际的代码沙箱执行代码，最后记录并返回执行结果。
     *
     * @param executeCodeRequest 包含代码执行请求的参数对象
     * @return ExecuteCodeResponse 包含代码执行结果的对象
     */
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        // 记录代码沙箱请求参数
        log.info("代码沙箱请求参数：{}", executeCodeRequest);

        // 调用实际的代码沙箱执行代码
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);

        // 记录代码沙箱响应结果
        log.info("代码沙箱响应结果：{}", executeCodeResponse);

        return executeCodeResponse;
    }
}

