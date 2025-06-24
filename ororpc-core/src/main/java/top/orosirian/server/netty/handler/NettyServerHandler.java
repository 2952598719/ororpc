package top.orosirian.server.netty.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import top.orosirian.message.RpcRequest;
import top.orosirian.message.RpcResponse;
import top.orosirian.server.provider.ServiceProvider;
import top.orosirian.server.ratelimit.RateLimit;

@AllArgsConstructor
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private ServiceProvider serviceProvider;    // 服务发现器，底层是map
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        RpcResponse response = getResponse(request);
        ctx.channel().writeAndFlush(response);  // 不要写成ctx.writeAndFlush，那会导致并非从链表尾部开始处理，而是从NettyServerHandler开始处理
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private RpcResponse getResponse(RpcRequest rpcRequest) {
        String interfaceName = rpcRequest.getInterfaceName();
        // 首先判断限流
        RateLimit rateLimit = serviceProvider.getRateLimitProvider().getRateLimit(interfaceName);
        if(!rateLimit.getToken()) {     // 获取令牌失败则限流，降级服务
            System.out.println("服务限流");
            return RpcResponse.fail("服务限流，接口 " + interfaceName + " 当前无法处理请求。请稍后再试。");
        }

        // 其次处理请求
        Object service = serviceProvider.getService(interfaceName);
        Method method = null;
        try {
            method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamsType());
            Object invoke = method.invoke(service, rpcRequest.getParams());
            return RpcResponse.success(invoke);
        } catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            System.out.println("方法执行错误");
            return RpcResponse.fail("方法执行错误");
        }
    }

}
