package com.siyue.siojbackendjudgeservice.judge.codesandbox.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.siyue.siojbackendcommon.common.ErrorCode;
import com.siyue.siojbackendcommon.exception.BusinessException;
import com.siyue.siojbackendjudgeservice.judge.codesandbox.CodeSandbox;
import com.siyue.siojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import com.siyue.siojbackendmodel.model.codesandbox.ExecuteCodeResponse;
import org.apache.commons.lang3.StringUtils;

/**
 * 实际远程代码沙箱
 */
public class RemoteCodeSandboxImpl implements CodeSandbox {

    // 定义鉴权请求头和密钥
    private static final String AUTH_HEADER = "auth";
    private static final String AUTH_REQUEST_STR = "secretAuth";
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("远程代码沙箱");
        String url = "http://192.168.204.130:8080/executeCode";
        String json = JSONUtil.toJsonStr(executeCodeRequest);
        String responseBody = HttpUtil.createPost(url)
                .header(AUTH_HEADER, AUTH_REQUEST_STR)
                .body(json)
                .execute()
                .body();
        if (StringUtils.isBlank(responseBody)) {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "远程沙箱调用错误");
        }

        return JSONUtil.toBean(responseBody, ExecuteCodeResponse.class);
    }
}
