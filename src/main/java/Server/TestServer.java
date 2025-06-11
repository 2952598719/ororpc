package Server;

import Server.provider.ServiceProvider;
import Server.server.RpcServer;
import Server.server.impl.SimpleRPCServer;
import common.service.UserService;
import common.service.impl.UserServiceImpl;

public class TestServer {

    public static void main(String[] args) {
        UserService userService = new UserServiceImpl();

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.provideServiceInterface(userService);

        RpcServer rpcServer = new SimpleRPCServer(serviceProvider);
        rpcServer.start(9999);
    }
    
}
