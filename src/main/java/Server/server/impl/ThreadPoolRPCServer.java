package Server.server.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import Server.provider.ServiceProvider;
import Server.server.RpcServer;
import Server.server.WorkThread;

public class ThreadPoolRPCServer implements RpcServer {

    private final ThreadPoolExecutor threadPool;

    private ServiceProvider serviceProvider;

    public ThreadPoolRPCServer(ServiceProvider serviceProvider) {
        threadPool = new ThreadPoolExecutor(
                                Runtime.getRuntime().availableProcessors(),     // 可用处理其
                                1000, 
                                60,
                                TimeUnit.SECONDS,
                                new ArrayBlockingQueue<>(100)
                            );
        this.serviceProvider = serviceProvider;
    }

    @Override
    public void start(int port) {
        System.out.println("[] 服务端已启动");
        try {
            ServerSocket serverSocket = new ServerSocket();
            while(true) {
                Socket socket = serverSocket.accept();
                threadPool.execute(new WorkThread(socket, serviceProvider));
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void stop() {

    }
    
}
