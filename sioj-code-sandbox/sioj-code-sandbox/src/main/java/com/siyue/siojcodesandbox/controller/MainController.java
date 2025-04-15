package com.siyue.siojcodesandbox.controller;

import com.siyue.siojcodesandbox.JavaDockerCodeSandBoxTemplateImpl;
import com.siyue.siojcodesandbox.JavaNativeCodeSandBoxTemplateImpl;
import com.siyue.siojcodesandbox.model.ExecuteCodeRequest;
import com.siyue.siojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/")
public class MainController {

    // 定义鉴权请求头和密钥
    private static final String AUTH_HEADER = "auth";
    private static final String AUTH_REQUEST_STR = "secretAuth";

    @Resource
    private JavaDockerCodeSandBoxTemplateImpl javaDockerCodeSandBoxTemplate;

    @Resource
    private JavaNativeCodeSandBoxTemplateImpl javaNativeCodeSandBoxTemplate;

    @GetMapping("/health")
    public String healthCheck() {
        return "ok";
    }

    @PostMapping("/executeCode")
    ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest request,
                                    HttpServletResponse response)  {
        // 基本的认证
        String header = request.getHeader(AUTH_HEADER);
        if (!header.equals(AUTH_REQUEST_STR)) {
            response.setStatus(403);
            return null;
        }
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandBoxTemplate.executeCode(executeCodeRequest);
        if (executeCodeResponse == null) {
            throw new RuntimeException("请求参数为空");
        }
        return executeCodeResponse;
    }
}