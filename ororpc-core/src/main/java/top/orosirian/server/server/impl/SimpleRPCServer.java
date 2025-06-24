package top.orosirian.server.server.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import lombok.AllArgsConstructor;
import top.orosirian.server.provider.ServiceProvider;
import top.orosirian.server.server.RpcServer;
import top.orosirian.server.server.WorkThread;

@AllArgsConstructor
public class SimpleRPCServer implements RpcServer {

    private ServiceProvider serviceProvider;

    @Override
    public void start(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("[] 服务器已启动");
            while(true) {
                Socket socket = serverSocket.accept();  // 阻塞到有连接
                new Thread(new WorkThread(socket, serviceProvider)).start();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {

    }
    
}
