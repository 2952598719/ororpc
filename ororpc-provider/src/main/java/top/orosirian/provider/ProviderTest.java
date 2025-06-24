package top.orosirian.provider;

import lombok.extern.slf4j.Slf4j;
import top.orosirian.provider.impl.UserServiceImpl;
import top.orosirian.server.provider.ServiceProvider;
import top.orosirian.server.server.RpcServer;
import top.orosirian.server.server.impl.NettyRPCServer;
import top.orosirian.service.UserService;

@Slf4j
public class ProviderTest {

    public static void main(String[] args) {
        // 蠢到极点没有任何意义的KRpcApplication裹脚布，到头来干了啥，哦取了个host（还命名为ip）和port，人家dubbo没办法才弄这么复杂，你凑什么热闹？显得你能？
        String host = "localhost";
        int port = 9999;
        // 创建 UserService 实例
        UserService userService = new UserServiceImpl();
        ServiceProvider serviceProvider = new ServiceProvider(host, port);
        // 发布服务接口到 ServiceProvider
        serviceProvider.provideServiceInterface(userService);  // 可以设置是否支持重试

        // 启动 RPC 服务器并监听端口
        RpcServer rpcServer = new NettyRPCServer(serviceProvider);
        rpcServer.start(port);  // 启动 Netty RPC 服务，监听 port 端口
        log.info("RPC 服务端启动，监听端口" + port);
    }

}
