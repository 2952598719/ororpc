package Client.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import Client.rpcClient.RpcClient;
import Client.rpcClient.impl.NettyRpcClient;
import Client.rpcClient.impl.SimpleSocketRpcClient;
import common.Message.RpcRequest;
import common.Message.RpcResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ClientProxy implements InvocationHandler {

    private RpcClient rpcClient;

    public ClientProxy(String host, int port) {
        rpcClient = new NettyRpcClient(host, port);
    }

    public ClientProxy(String host, int port, int choose) {
        switch(choose) {
            case 0:
                rpcClient = new NettyRpcClient(host, port);
                break;
            case 1:
                rpcClient = new SimpleSocketRpcClient(host, port);
                break;
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest request = RpcRequest.builder()
                                        .interfaceName(method.getDeclaringClass().getName())
                                        .methodName(method.getName())
                                        .params(args)
                                        .paramsType(method.getParameterTypes())
                                        .build();
        RpcResponse response = rpcClient.sendRequest(request);
        return response.getData();
    }

    public <T>T getProxy(Class<T> clazz) {
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        return (T)o;
    }
    
}
