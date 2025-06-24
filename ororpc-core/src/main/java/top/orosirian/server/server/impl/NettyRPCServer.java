package top.orosirian.server.server.impl;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.AllArgsConstructor;
import top.orosirian.server.netty.nettyInitializer.NettyServerInitializer;
import top.orosirian.server.provider.ServiceProvider;
import top.orosirian.server.server.RpcServer;

@AllArgsConstructor
public class NettyRPCServer implements RpcServer {

    private ServiceProvider serviceProvider;

    @Override
    public void start(int port) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);  // 监听接受请求，分配给workGroup
        NioEventLoopGroup workGroup = new NioEventLoopGroup();            // 处理实际业务
        System.out.println("netty服务端已启动");
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workGroup)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(new NettyServerInitializer(serviceProvider));
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            channelFuture.channel().closeFuture().sync();
        } catch(InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    @Override
    public void stop() {

    }
    
}
