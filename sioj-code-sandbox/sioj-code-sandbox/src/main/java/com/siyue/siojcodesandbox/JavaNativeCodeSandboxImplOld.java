package com.siyue.siojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.WordTree;
import com.siyue.siojcodesandbox.model.ExecuteCodeRequest;
import com.siyue.siojcodesandbox.model.ExecuteCodeResponse;
import com.siyue.siojcodesandbox.model.ExecuteMessage;
import com.siyue.siojcodesandbox.model.JudgeInfo;
import com.siyue.siojcodesandbox.utils.ProcessUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class JavaNativeCodeSandboxImplOld implements CodeSandbox {

    private static final String GLOBAL_CODE_PATH_NAME = "tempCode";

    private static final String GLOBAL_JAVA_EXE_NAME = "Main.java";

    private static final String SECURITY_MANAGER_CLASS = "MySecurityManager";

    private static final String MY_SECURITY_MANAGER_PATH = "D:\\Learning\\OJ\\sioj-code-sandbox\\src\\main\\resources\\security";

    private static final long TIMEOUT_MILLISECONDS = 5000L;

    private static final List<String> BLACKLIST_INPUT_LIST = Arrays.asList("exec", "Files", "System.in", "System.out", "System.err");

    private static final WordTree WORD_TREE;

    static {
        // 通过BLACKLIST_INPUT_LIST初始化wordTree
        WORD_TREE = new WordTree();
        WORD_TREE.addWords(BLACKLIST_INPUT_LIST);

    }

    public static void main(String[] args) {
        JavaNativeCodeSandboxImplOld javaNativeCodeSandbox = new JavaNativeCodeSandboxImplOld();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 2", "2 3"));
        String code = ResourceUtil.readStr("testCode/unsafecode/runFileError.java", StandardCharsets.UTF_8);
        System.out.println("这是我的代码：" + code);
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");
//        javaNativeCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(javaNativeCodeSandbox.executeCode(executeCodeRequest));
    }

/**
 * 执行代码并返回执行结果。
 * 该方法接收一个包含代码和输入列表的请求对象，编译并执行代码，最后返回执行结果。
 *
 * @param executeCodeRequest 包含代码、输入列表等信息的请求对象
 * @return ExecuteCodeResponse 包含代码执行结果、输出列表、执行状态等信息的响应对象
 */
@Override
public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {

//    System.setSecurityManager(new DenySecurityManager());
    List<String> inputList = executeCodeRequest.getInputList();
    String code = executeCodeRequest.getCode();

    // 用黑名单 BLACKLIST_INPUT_LIST 校验代码 用hutool工具类实现
//    WORD_TREE.addWords(BLACKLIST_INPUT_LIST);
//    if (WORD_TREE.isMatch(code)) {
//        return ExecuteCodeResponse.builder()
//                .outputList(new ArrayList<>())
//                .message("代码中有非法字符")
//                .status(3)
//                .judgeInfo(new JudgeInfo())
//                .build();
//    }


    // 用黑名单 BLACKLIST_INPUT_LIST 校验代码
//    for (String input : inputList) {
//        for (String s : BLACKLIST_INPUT_LIST) {
//            if (input.contains(s)) {
//                return ExecuteCodeResponse.builder()
//                        .outputList(new ArrayList<>())
//                        .message("代码中有非法字符：" + s)
//                        .status(3)
//                        .judgeInfo(new JudgeInfo())
//                        .build();
//            }
//        }
//    }


    // String language = executeCodeRequest.getLanguage();

    // 获取当前工作目录，并创建全局代码存储路径
    String userDir = System.getProperty("user.dir");
    String globalCodePathName = userDir + File.separator + GLOBAL_CODE_PATH_NAME;

    if (!FileUtil.exist(globalCodePathName)) {
        FileUtil.mkdir(globalCodePathName);
    }
    // 为当前用户代码生成唯一路径，并将代码写入文件
    String userCodePathName = globalCodePathName + File.separator + UUID.randomUUID();
    String userCodePath = userCodePathName + File.separator + GLOBAL_JAVA_EXE_NAME;
    File userCodePathFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);

    // 编译代码
    String compileCmd = String.format("javac -encoding utf-8 %s", userCodePathFile.getAbsoluteFile());
    System.out.println("compileCmd：" + compileCmd);
    try {
        Process exec = Runtime.getRuntime().exec(compileCmd);
        ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(exec, "编译");
        System.out.println(executeMessage);
    } catch (Exception e) {
        return this.getErrorCodeResponse(e);
    }

    // 执行代码并收集执行结果
    List<ExecuteMessage> executeMessageList = new ArrayList<>();
    for (String input : inputList) {
        // Xmx 设置最大内存为256MB, 但不等于实际使用的内存，可能会超出，因为是Java虚拟机在运行时使用的内存。
        // 如果要更严格的限制要去系统层面设置，而不是JVM层面。
        // java -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=MySecurityManager Main
//        String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s", userCodePathName, input);
        String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=%s Main %s",
                userCodePathName, MY_SECURITY_MANAGER_PATH, SECURITY_MANAGER_CLASS, input);
        System.out.println("runCmd：" + runCmd);

        try {
            Process runProcess = Runtime.getRuntime().exec(runCmd);
            // 超时控制，守护线程
            new Thread(() -> {
                try {
                    Thread.sleep(TIMEOUT_MILLISECONDS);
                    runProcess.destroy();
                    System.out.println("超时了");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
            executeMessageList.add(executeMessage);
        } catch (Exception e) {
            return this.getErrorCodeResponse(e);
        }
    }

    // 收集执行结果并计算最大执行时间
    List<String> outputList = new ArrayList<>();
    ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();

    long maxTime = 0L;
    for (ExecuteMessage executeMessage : executeMessageList) {
        String errorMsg = executeMessage.getError();
        if (StrUtil.isNotBlank(errorMsg)) {
            executeCodeResponse.setMessage(errorMsg);
            executeCodeResponse.setStatus(3);
            break;
        }
        outputList.add(executeMessage.getMessage());
        Long tempTime = executeMessage.getTime();
        if (tempTime != null)
            maxTime = Math.max(maxTime, tempTime);
    }
    // 如果所有输入都成功执行，则设置状态为1（成功）
    if (outputList.size() == executeMessageList.size()) {
        executeCodeResponse.setStatus(1);
    }
    executeCodeResponse.setOutputList(outputList);
    JudgeInfo judgeInfo = new JudgeInfo();
    judgeInfo.setTime(maxTime);
    executeCodeResponse.setJudgeInfo(judgeInfo);

//     删除用户代码目录
    if (userCodePathFile.getParentFile() != null) {
        boolean del = FileUtil.del(userCodePathName);
        if (del) {
            System.out.println("删除成功");
        } else {
            System.out.println("删除失败");
        }
    }
    return executeCodeResponse;
}


    /**
     * 获取错误响应
     * @param e
     * @return
     */
    private ExecuteCodeResponse getErrorCodeResponse(Throwable e) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        executeCodeResponse.setStatus(2);
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return executeCodeResponse;
    }
}
