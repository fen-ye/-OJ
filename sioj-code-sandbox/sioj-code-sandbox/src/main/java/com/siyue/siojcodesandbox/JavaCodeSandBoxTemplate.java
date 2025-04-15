package com.siyue.siojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.siyue.siojcodesandbox.model.ExecuteCodeRequest;
import com.siyue.siojcodesandbox.model.ExecuteCodeResponse;
import com.siyue.siojcodesandbox.model.ExecuteMessage;
import com.siyue.siojcodesandbox.model.JudgeInfo;
import com.siyue.siojcodesandbox.utils.ProcessUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Slf4j
public abstract class JavaCodeSandBoxTemplate implements CodeSandbox {



    private static final String GLOBAL_CODE_PATH_NAME = "tempCode";

    private static final String GLOBAL_JAVA_EXE_NAME = "Main.java";

    private static final long TIMEOUT_MILLISECONDS = 5000L;

    /**
     * 1. 把代码保存为文件
     * @param code 用户代码
     * @return
     */
    public File saveCodeToFile(String code) {
        // 获取当前工作目录，并创建全局代码存储路径
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_PATH_NAME;

        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }
        // 为当前用户代码生成唯一路径，并将代码写入文件
        String userCodePathName = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodePathName + File.separator + GLOBAL_JAVA_EXE_NAME;
        return FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
    }

    /**
     * 2. 编译代码
     * @param userCodePathFile 代码文件
     * @return
     */
    public ExecuteMessage compileMessage (File userCodePathFile) {
        // 编译代码
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodePathFile.getAbsoluteFile());
        System.out.println("compileCmd：" + compileCmd);
        try {
            Process exec = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage compileMessage = ProcessUtils.runProcessAndGetMessage(exec, "编译");
            if (compileMessage.getExitValue() != 0) {
                throw new RuntimeException("编译错误");
            }
            return compileMessage;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 3. 执行代码，得到输出结果
     * @param inputList
     * @param userCodeFile
     * @return
     */
    public List<ExecuteMessage> runFile (List<String> inputList, File userCodeFile) {
        // 为当前用户代码生成唯一路径，并将代码写入文件
        String userCodePathName = userCodeFile.getParentFile().getPath();
        // 执行代码并收集执行结果
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String input : inputList) {
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s",
                    userCodePathName, input);
//           String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=%s Main %s",
//                    userCodePathName, MY_SECURITY_MANAGER_PATH, SECURITY_MANAGER_CLASS, input);
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
                throw new RuntimeException("程序执行错误：" + e);
            }
        }
        return executeMessageList;
    }

    /**
     * 4. 整理获取输出结果
     * @param executeMessageList
     * @return
     */
    private ExecuteCodeResponse getOutPutResponse(List<ExecuteMessage> executeMessageList) {
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

        return executeCodeResponse;
    }

    /**
     * 5. 删除文件
     * @param userCodePathFile
     * @return
     */
    public boolean deleteFile(File userCodePathFile) {
        if (userCodePathFile.getParentFile() != null) {
            String userCodePathName = userCodePathFile.getParentFile().getPath();
            return FileUtil.del(userCodePathName);
        }
        return true;
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
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();

        // 1.把代码保存为文件
        File userCodePathFile = saveCodeToFile(code);

        // 2.编译代码
        ExecuteMessage compileMessage = compileMessage(userCodePathFile);
        System.out.println(compileMessage);
        
        // 3.执行
        List<ExecuteMessage> executeMessages = runFile(inputList, userCodePathFile);

        // 4.收集整理结果
        ExecuteCodeResponse outPutResponse = getOutPutResponse(executeMessages);

        // 5.文件清理
        boolean del = deleteFile(userCodePathFile);
        if (!del) {
            log.error("文件清理失败！！！， userCodeFilePath = {}", userCodePathFile.getAbsoluteFile());
        }


        return outPutResponse;

    }



    /**
     * 6. 获取错误响应
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
