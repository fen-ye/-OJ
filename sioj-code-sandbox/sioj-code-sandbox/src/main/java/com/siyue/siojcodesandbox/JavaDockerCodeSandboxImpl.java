package com.siyue.siojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.WordTree;
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
import org.springframework.util.StopWatch;
import sun.rmi.transport.StreamRemoteCall;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class JavaDockerCodeSandboxImpl implements CodeSandbox {

    private static final String GLOBAL_CODE_PATH_NAME = "tempCode";

    private static final String GLOBAL_JAVA_EXE_NAME = "Main.java";

    private static final String SECURITY_MANAGER_CLASS = "MySecurityManager";

    private static final String MY_SECURITY_MANAGER_PATH = "D:\\Learning\\OJ\\sioj-code-sandbox\\src\\main\\resources\\security";

    private static final long TIMEOUT_MILLISECONDS = 5000L;

    private static final boolean FIRST_INIT = false;

    public static void main(String[] args) {
        JavaDockerCodeSandboxImpl javaNativeCodeSandbox = new JavaDockerCodeSandboxImpl();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 2", "2 3"));
        String code = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.java", StandardCharsets.UTF_8);
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

    // 创建容器，把文件复制到docker容器内

    DockerClient dockerClient = DockerClientBuilder.getInstance().build();

    // 拉取镜像
    String image = "openjdk:8-alpine";
    if (FIRST_INIT) {
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
        PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
            @Override
            public void onNext(PullResponseItem item) {
                System.out.println("下载镜像：" + item.getStatus());
                super.onNext(item);
            }
        };
        try {
            pullImageCmd
                    .exec(pullImageResultCallback)
                    .awaitCompletion();
        } catch (InterruptedException e) {
            System.out.println("拉取镜像失败");
            throw new RuntimeException(e);
        }
    }


    // 创建容器

    CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
    HostConfig hostConfig = new HostConfig();
    // 内存限制
    hostConfig.withMemory(100 * 1000 * 1000L);
    hostConfig.withMemorySwap(0L);
    hostConfig.withCpuCount(1L);
    // 用linux
//    hostConfig.withSecurityOpts(Arrays.asList(""));
    // 放到自己的工作空间中 Volume容器挂载目录
    hostConfig.setBinds(new Bind(userCodePathName, new Volume("/app")));

    CreateContainerResponse createContainerResponse = containerCmd
            .withNetworkDisabled(true)
            .withReadonlyRootfs(true)
            .withHostConfig(hostConfig)
            .withAttachStderr(true)
            .withAttachStdin(true)
            .withAttachStdout(true)
            .withTty(true)
            .exec();

    System.out.println(createContainerResponse);
    String containerId = createContainerResponse.getId();

    // 启动容器
    dockerClient.startContainerCmd(containerId).exec();


    // 执行命令
    List<ExecuteMessage> executeMessageList = new ArrayList<>();

    for (String input : inputList) {
        StopWatch stopWatch = new StopWatch();
        String[] splitInput = input.split(" ");
        String [] cmdS = new String [] {"java", "-cp", "/app", "Main"};
        cmdS = ArrayUtil.append(cmdS, splitInput);
        ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                .withAttachStderr(true).withAttachStdin(true).withAttachStdout(true).withCmd(cmdS).exec();

        System.out.println("创建执行命令" + execCreateCmdResponse.toString());
        ExecuteMessage executeMessage = new ExecuteMessage();
        final String[] message = {null};
        final String[] errorMessage = {null};

        String execcID = execCreateCmdResponse.getId();
        final boolean[] timeOut = {true};
        ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
            @Override
            public void onComplete() {
                // 如果执行完成就表示没有超时，因为在执行时限定了执行了时间 .awaitCompletion(TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
                timeOut[0] = false;
                super.onComplete();
            }

            @Override
            public void onNext(Frame frame) {
                StreamType streamType = frame.getStreamType();
                if (streamType.STDERR.equals(streamType)) {
                    errorMessage[0] = new String(frame.getPayload());
                    System.out.println("输出错误结果：" + errorMessage[0]);
                }
                else {
                    message[0] = new String(frame.getPayload());
                    System.out.println("输出结果：" + message[0]);
                }
                super.onNext(frame);
            }
        };

        final long[] memoryMax = {0};
        // 获取占用的内存
        StatsCmd statsCmd = dockerClient.statsCmd(containerId);
        ResultCallback<Statistics> execStatistics = statsCmd.exec(new ResultCallback<Statistics>() {
            @Override
            public void onNext(Statistics statistics) {
                System.out.println("内存信息：" + statistics.getMemoryStats().getUsage());
                memoryMax[0] = Math.max(memoryMax[0], statistics.getMemoryStats().getUsage());
            }

            @Override
            public void onStart(Closeable closeable) {

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }

            @Override
            public void close() throws IOException {

            }
        });
        statsCmd.exec(execStatistics);

        try {
            stopWatch.start();
            dockerClient.execStartCmd(execcID)
                    .exec(execStartResultCallback)
                    .awaitCompletion(TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
            stopWatch.stop();
            statsCmd.close();
        } catch (InterruptedException e) {
            System.out.println("程序执行异常！！！");
            throw new RuntimeException(e);
        }
        executeMessage.setMessage(message[0]);
        executeMessage.setError(errorMessage[0]);
        executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
        executeMessage.setMemory(memoryMax[0]);
        executeMessageList.add(executeMessage);

    }

    // 封装结果，与之前完全一致
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
