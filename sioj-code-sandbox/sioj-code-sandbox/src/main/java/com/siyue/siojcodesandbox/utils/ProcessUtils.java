package com.siyue.siojcodesandbox.utils;

import cn.hutool.core.util.StrUtil;
import com.siyue.siojcodesandbox.model.ExecuteMessage;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.util.StopWatch;

import javax.lang.model.element.NestingKind;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ProcessUtils {
    /**
     * 执行进程并获取执行结果信息
     *
     * 该方法会等待进程执行完成，并根据进程的退出值判断执行是否成功。
     * 如果进程执行失败（退出值不为0），则捕获并记录错误输出流的内容。
     * 如果进程执行成功，则捕获并记录标准输出流的内容。
     *
     * @param exec 要执行的进程对象
     * @return ExecuteMessage 包含进程执行结果信息的对象，包括退出值、标准输出和错误输出
     */
    public static ExecuteMessage runProcessAndGetMessage(Process exec, String opName) {
        ExecuteMessage executeMessage = new ExecuteMessage();

        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            // 等待进程执行完成，并获取退出值
            int exitValue = exec.waitFor();
            executeMessage.setExitValue(exitValue);

            // 如果进程执行失败（退出值不为0），处理错误输出
            if (exitValue != 0) {
                System.out.println(opName + "失败");

                // 读取并记录标准输出流的内容
                BufferedReader br = new BufferedReader(new InputStreamReader(exec.getInputStream()));
                StringBuilder outPutBuffer = new StringBuilder();
                String outPutLine;
                while ((outPutLine = br.readLine()) != null) {
                    outPutBuffer.append(outPutLine);
                }

                // 读取并记录错误输出流的内容
                BufferedReader brError = new BufferedReader(new InputStreamReader(exec.getErrorStream()));
                String outPutErrorLine;
                List<String> outPutErrorList = new ArrayList<>();
                while ((outPutErrorLine = brError.readLine()) != null) {
                    outPutErrorList.add(outPutErrorLine);
                }
                executeMessage.setError(StringUtils.join(outPutErrorList + "\n"));

            } else {
                // 如果进程执行成功，读取并记录标准输出流的内容
                BufferedReader br = new BufferedReader(new InputStreamReader(exec.getInputStream()));
                List<String> outPutList = new ArrayList<>();
                String outPutLine;
                while ((outPutLine = br.readLine()) != null) {
                    outPutList.add(outPutLine);
                }
                executeMessage.setMessage(StringUtils.join(outPutList, "\n"));
            }
            stopWatch.stop();
            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());

        } catch (InterruptedException | IOException e) {
            // 捕获并处理可能的异常
            e.printStackTrace();
        }
        return executeMessage;
    }

    /**
     * 执行交互式进程获取信息
     * @param exec
     * @param opName
     * @return
     */
    public static ExecuteMessage runProcessInterAndGetMessage(Process exec, String opName, String args) {
        ExecuteMessage executeMessage = new ExecuteMessage();

        try {
            // 向控制台输入数据
            OutputStream outputStream = exec.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            String [] stirs = args.split(" ");
            outputStreamWriter.write(StrUtil.join("\n", (Object) stirs) + "\n");
            // 相当于按了回车
            outputStreamWriter.flush();

            InputStream inputStream = exec.getInputStream();
            // 读取并记录标准输出流的内容
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder outPutBuffer = new StringBuilder();
            String outPutLine;
            while ((outPutLine = br.readLine()) != null) {
                outPutBuffer.append(outPutLine);
            }
            executeMessage.setMessage(outPutBuffer.toString());
            outputStreamWriter.close();
            outputStream.close();
            inputStream.close();
            exec.destroy();
        } catch (Exception e) {
            // 捕获并处理可能的异常
            e.printStackTrace();
        }
        return executeMessage;
    }
}

