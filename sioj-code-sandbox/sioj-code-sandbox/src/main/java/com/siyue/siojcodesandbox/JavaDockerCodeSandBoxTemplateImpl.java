package com.siyue.siojcodesandbox;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ArrayUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.siyue.siojcodesandbox.model.ExecuteCodeRequest;
import com.siyue.siojcodesandbox.model.ExecuteCodeResponse;
import com.siyue.siojcodesandbox.model.ExecuteMessage;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 继承 JavaCodeSandBoxTemplate，使用模板方法模式。
 * Java 原生代码沙箱实现（直接复用模板代码）
 */
@Component
public class JavaDockerCodeSandBoxTemplateImpl extends JavaCodeSandBoxTemplate {

    private static final long TIMEOUT_MILLISECONDS = 5000L;

    private static final boolean FIRST_INIT = false;

    public static void main(String[] args) {
        JavaDockerCodeSandBoxTemplateImpl javaDockerCodeSandBoxTemplateImpl = new JavaDockerCodeSandBoxTemplateImpl();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 2", "2 3"));
        String code = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.java", StandardCharsets.UTF_8);
        System.out.println("这是我的代码：" + code);
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");
//        javaNativeCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(javaDockerCodeSandBoxTemplateImpl.executeCode(executeCodeRequest));
    }


    @Override
    public List<ExecuteMessage> runFile(List<String> inputList, File userCodeFile) {

        String userCodePathName = userCodeFile.getParentFile().getAbsolutePath();

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
        // 用linux 安全策略
//        hostConfig.withSecurityOpts(Arrays.asList(""));
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
        return executeMessageList;
    }
}
