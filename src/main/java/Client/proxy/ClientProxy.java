package Client.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import Client.IOClient;
import common.Message.RpcRequest;
import common.Message.RpcResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ClientProxy implements InvocationHandler {

    private String host;
    
    private int port;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest request = RpcRequest.builder()
                                        .interfaceName(method.getDeclaringClass().getName())
                                        .methodName(method.getName())
                                        .params(args)
                                        .paramsType(method.getParameterTypes())
                                        .build();
        RpcResponse response = IOClient.sendRequest(host, port, request);
        return response.getData();
    }

    public <T>T getProxy(Class<T> clazz) {
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        return (T)o;
    }
    
}
