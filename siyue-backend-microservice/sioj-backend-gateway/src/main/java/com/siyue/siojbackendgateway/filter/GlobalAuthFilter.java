package com.siyue.siojbackendgateway.filter;

import cn.hutool.core.text.AntPathMatcher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class GlobalAuthFilter implements GlobalFilter, Ordered {

    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    /**
 * 过滤器方法，用于在请求到达网关时进行路径和权限的校验。
 * 该方法会检查请求路径是否包含 "inner"，如果包含则拒绝访问，只允许内部调用。
 * 如果路径校验通过，则继续执行后续的过滤器或目标服务。
 *
 * @param exchange 包含当前请求和响应的上下文对象，用于获取请求信息和设置响应信息。
 *                 - 通过 `exchange.getRequest()` 可以获取当前请求的详细信息。
 *                 - 通过 `exchange.getResponse()` 可以设置响应的状态码和内容。
 * @param chain 过滤器链，用于继续执行后续的过滤器或目标服务。
 *              - 调用 `chain.filter(exchange)` 可以将请求传递给下一个过滤器或目标服务。
 * @return Mono<Void> 表示异步处理的结果，通常用于返回响应或继续执行过滤器链。
 *                    - 如果路径校验失败，返回一个包含错误信息的响应。
 *                    - 如果路径校验通过，返回继续执行过滤器链的结果。
 */
@Override
public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    // 获取当前请求对象
    ServerHttpRequest serverHttpRequest = exchange.getRequest();
    // 获取请求的路径
    String path = serverHttpRequest.getURI().getPath();

    // 判断请求路径是否包含 "inner"，如果包含则拒绝访问，只允许内部调用
    if (antPathMatcher.match("/ **/inner/** ", path)) {
        // 获取响应对象
        ServerHttpResponse response = exchange.getResponse();
        // 设置响应状态码为 403（无权限）
        response.setStatusCode(HttpStatus.FORBIDDEN);
        // 获取数据缓冲区工厂
        DataBufferFactory dataBufferFactory = response.bufferFactory();
        // 将错误信息 "无权限" 转换为字节数组并包装为数据缓冲区
        DataBuffer dataBuffer = dataBufferFactory.wrap("无权限".getBytes(StandardCharsets.UTF_8));
        // 返回包含错误信息的响应
        return response.writeWith(Mono.just(dataBuffer));
    }

    // TODO: 实现统一的权限校验逻辑，通过 JWT 获取登录用户信息并进行权限验证
    // 如果路径校验通过，继续执行后续的过滤器或目标服务
    return chain.filter(exchange);
}


    /**
     * 优先级提到最高
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
