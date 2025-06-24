package top.orosirian.client.rpcClient.impl;

import top.orosirian.client.rpcClient.RpcClient;
import top.orosirian.message.RpcRequest;
import top.orosirian.message.RpcResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class SimpleSocketRpcClient implements RpcClient {

    private String host;
    
    private int port;

    public SimpleSocketRpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public RpcResponse sendRequest(RpcRequest request) {
        RpcResponse response = null;
        try {
            Socket socket = new Socket(host, port);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            oos.writeObject(request);
            oos.flush();
            response = (RpcResponse) ois.readObject();
        } catch (UnknownHostException e) {
            System.err.println("未知的主机: " + host);
        } catch (IOException e) {
            System.err.println("I/O 错误: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("无法识别的类: " + e.getMessage());
        }
        return response;
    }

    @Override
    public void close() {
        // 没什么好关的
    }
    
}
