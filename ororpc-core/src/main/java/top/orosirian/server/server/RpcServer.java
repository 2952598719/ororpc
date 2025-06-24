package top.orosirian.server.server;

public interface RpcServer {
    
    void start(int port);   // 开启监听

    void stop();
}
