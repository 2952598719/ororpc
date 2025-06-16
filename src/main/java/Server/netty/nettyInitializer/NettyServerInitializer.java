package Server.netty.nettyInitializer;

import Server.netty.handler.NettyServerHandler;
import Server.provider.ServiceProvider;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

    private ServiceProvider serviceProvider;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // 出站处理器固定是要先写，入站后写
        // gemini：如果出站放在下面，那么因为数据到自定义handler就返回了，就导致数据沉不到出站处理器部分，导致没有附加上长度字段就发出去
        // 但这样的意思就是说ctx.writeAndFlush(response)不走全部，只从当前就返回了，而按照我了解的netty，应该是从底部而非此处开始的

        // 下面代码和NettyClientInitializer一致，就不写注释了
        // 网络<->
            // 入站
        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
        pipeline.addLast(new ObjectDecoder((className) -> Class.forName(className)));
        pipeline.addLast(new NettyServerHandler(serviceProvider));
           // 出站
        pipeline.addLast(new LengthFieldPrepender(4));
        pipeline.addLast(new ObjectEncoder());
        // <->主机
    }
    
}
