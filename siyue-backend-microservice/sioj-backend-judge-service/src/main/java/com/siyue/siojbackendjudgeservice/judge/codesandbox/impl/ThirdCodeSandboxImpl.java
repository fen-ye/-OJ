package com.siyue.siojbackendjudgeservice.judge.codesandbox.impl;


import com.siyue.siojbackendjudgeservice.judge.codesandbox.CodeSandbox;
import com.siyue.siojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import com.siyue.siojbackendmodel.model.codesandbox.ExecuteCodeResponse;

/**
 * 第三方代码沙箱（调用第三方服务）
 */
public class ThirdCodeSandboxImpl implements CodeSandbox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("三方代码沙箱");
        return null;
    }
}
