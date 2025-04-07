package com.siyue.siojbackendjudgeservice.judge.codesandbox;


import com.siyue.siojbackendjudgeservice.judge.codesandbox.impl.ExampleCodeSandboxImpl;
import com.siyue.siojbackendjudgeservice.judge.codesandbox.impl.RemoteCodeSandboxImpl;
import com.siyue.siojbackendjudgeservice.judge.codesandbox.impl.ThirdCodeSandboxImpl;

/**
 * 代码沙箱静态工厂类，用于根据字符串类型获取对应的代码沙箱实例。
 */
public class CodeSandboxFactory {

    /**
     * 根据传入的代码沙箱类型字符串，返回对应的代码沙箱实例。
     *
     * @param type 代码沙箱类型，支持的值为 "remote"、"example" 和 "third"。
     * @return 返回对应类型的代码沙箱实例。如果类型不匹配，默认返回 ExampleCodeSandboxImpl 实例。
     */
    public static CodeSandbox newInstance(String type) {
        // 根据传入的 type 参数，返回对应的代码沙箱实例
        switch (type) {
            case "remote":
                return new RemoteCodeSandboxImpl();
            case "example":
                return new ExampleCodeSandboxImpl();
            case "third":
                return new ThirdCodeSandboxImpl();
            default:
                // 如果 type 不匹配任何已知类型，默认返回 ExampleCodeSandboxImpl 实例
                return new ExampleCodeSandboxImpl();
        }
    }
}

