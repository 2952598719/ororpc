package Client.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import Client.retry.GuavaRetry;
import Client.rpcClient.RpcClient;
import Client.serviceCenter.ServiceCenter;
import Client.serviceCenter.impl.ZKServiceCenter;
import common.Message.RpcRequest;
import common.Message.RpcResponse;

public class ClientProxy implements InvocationHandler {

    private RpcClient rpcClient;

    private ServiceCenter serviceCenter;

    public ClientProxy(RpcClient rpcClient) throws InterruptedException {
        this.rpcClient = rpcClient;
        serviceCenter = new ZKServiceCenter();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest request = RpcRequest.builder()
                                        .interfaceName(method.getDeclaringClass().getName())
                                        .methodName(method.getName())
                                        .params(args)
                                        .paramsType(method.getParameterTypes())
                                        .build();
        RpcResponse response;
        // TODO:这里疑似不合理，应该是接口中某个方法可以重试，而不是以接口为单位重试，似乎需要修改zk存的信息
        if(serviceCenter.checkRetry(request.getInterfaceName())) {
            response = new GuavaRetry().sendServiceWithRetry(request, rpcClient);
        } else {
            response = rpcClient.sendRequest(request);
        }
        return response.getData();
    }

    @SuppressWarnings("unchecked")
    public <T>T getProxy(Class<T> clazz) {
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        return (T)o;
    }
    
}
