package Client.rpcClient.impl;

import java.net.InetSocketAddress;

import Client.netty.nettyInitializer.NettyClientInitializer;
import Client.rpcClient.RpcClient;
import Client.serviceCenter.ServiceCenter;
import Client.serviceCenter.impl.ZKServiceCenter;
import common.Message.RpcRequest;
import common.Message.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;

public class NettyRpcClient implements RpcClient {

    // 不要一看到bootstrap就吓得屁滚尿流，这里就是个配置器而已，不是操作系统那个bootstrap
    // 把配置参数传进去之后拿来创建连接，没什么神奇高深作用
    private static final Bootstrap bootstrap;

    // netty的线程模型核心，相当于一个线程池，管理一组EventLoop（默认2*CPU核数）
    private static final EventLoopGroup eventLoopGroup;
    
    private ServiceCenter serviceCenter;

    public NettyRpcClient() {
        serviceCenter = new ZKServiceCenter();
    }

    static {
        eventLoopGroup = new NioEventLoopGroup();                       // 线程组，也就是收发网络请求的“工人”，默认有CPU核数*2（IO密集型）
        bootstrap = new Bootstrap()
                        .group(eventLoopGroup)                          // 指定线程组
                        .channel(NioSocketChannel.class)    // 指定通道类型为Nio
                        .handler(new NettyClientInitializer());         // 指定一个请求进出时经过的流程
    }

    @Override
    public RpcResponse sendRequest(RpcRequest request) {
        InetSocketAddress serviceAddress = serviceCenter.discoverService(request.getInterfaceName());
        String host = serviceAddress.getHostName();
        int port = serviceAddress.getPort();
        try {
            // bootstrap.connect是个异步行为，所以返回ChannelFuture，在执行完成后可以通过.channel来获取连接
            // 所以要等待它创建完毕才能去获取channel，因此要使用.sync来同步（用人话说也就是阻塞在那，直到connect完毕）
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            Channel channel = channelFuture.channel();  // channel类似于socket
            // 发送请求
            channel.writeAndFlush(request);
            // 通道获取完数据才会将其转换为对象，此时才能从中获取，所以要阻塞
            channel.closeFuture().sync();
            // channel和主线程是两个线程，所以需要用缓冲区通信，channel把对象放在attr代表的缓冲区中，主线程根据引用名从中获取
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("RPCResponse");    // 某个channel的名字
            RpcResponse response = channel.attr(key).get();
            return response;
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    
}
