package Server;

import Server.provider.ServiceProvider;
import Server.server.RpcServer;
import Server.server.impl.NettyRPCServer;
import Server.server.impl.SimpleRPCServer;
import Server.server.impl.ThreadPoolRPCServer;
import common.service.UserService;
import common.service.impl.UserServiceImpl;

public class TestServer {

    public static void main(String[] args) {
        UserService userService = new UserServiceImpl();
        ServiceProvider serviceProvider = new ServiceProvider("127.0.0.1", 9999);
        serviceProvider.provideServiceInterface(userService, true);

        RpcServer rpcServer = getServer(serviceProvider, 0);
        rpcServer.start(9999);
    }

    private static RpcServer getServer(ServiceProvider serviceProvider, int type) {
        RpcServer rpcServer = null;
        switch (type) {
            case 0:
                rpcServer = new NettyRPCServer(serviceProvider);
                break;
            case 1:
                rpcServer = new SimpleRPCServer(serviceProvider);
                break;
            case 2:
                rpcServer = new ThreadPoolRPCServer(serviceProvider);
                break;
            default:
                rpcServer = new NettyRPCServer(serviceProvider);
                break;
        }
        return rpcServer;
    }
    
}
