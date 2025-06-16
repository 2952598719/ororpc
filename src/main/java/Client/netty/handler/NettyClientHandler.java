package Client.netty.handler;

import common.Message.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        AttributeKey<RpcResponse> key = AttributeKey.valueOf("RPCResponse");
        // attr就是个缓冲区，channel线程把自己获取到的对象放入其中后，主线程才能根据对象引用名，从缓冲区中获取内容
        ctx.channel().attr(key).set(response);
        ctx.channel().close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
