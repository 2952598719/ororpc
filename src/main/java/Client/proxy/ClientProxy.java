package Client.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import Client.circuitBreaker.CircuitBreaker;
import Client.circuitBreaker.CircuitBreakerProvider;
import Client.retry.GuavaRetry;
import Client.rpcClient.RpcClient;
import Client.serviceCenter.ServiceCenter;
import Client.serviceCenter.impl.ZKServiceCenter;
import common.Message.RpcRequest;
import common.Message.RpcResponse;

public class ClientProxy implements InvocationHandler {

    private final RpcClient rpcClient;

    private final ServiceCenter serviceCenter;

    private final CircuitBreakerProvider circuitBreakerProvider;

    public ClientProxy(RpcClient rpcClient) throws InterruptedException {
        this.rpcClient = rpcClient;
        this.serviceCenter = new ZKServiceCenter();
        this.circuitBreakerProvider = new CircuitBreakerProvider();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        // 1.构建request
        RpcRequest request = RpcRequest.builder()
                                        .interfaceName(method.getDeclaringClass().getName())
                                        .methodName(method.getName())
                                        .params(args)
                                        .paramsType(method.getParameterTypes())
                                        .build();
        // 2.获取熔断器
        CircuitBreaker circuitBreaker = circuitBreakerProvider.getCircuitBreaker(method.getName());
        if(!circuitBreaker.allowRequest()) {
            return null;
        }
        // 3.数据传输
        RpcResponse response;
        // TODO:这里疑似不合理，应该是接口中某个方法可以重试，而不是以接口为单位重试，似乎需要修改zk存的信息
        if(serviceCenter.checkRetry(request.getInterfaceName())) {
            response = new GuavaRetry().sendServiceWithRetry(request, rpcClient);
        } else {
            response = rpcClient.sendRequest(request);
        }
        // 4.记录response状态，上报给熔断器
        if(response.getCode() == 200) {
            circuitBreaker.recordSuccess();
        } else if(response.getCode() == 500) {
            circuitBreaker.recordFailure();
        }
        // 5.返回数据
        return response.getData();
    }

    @SuppressWarnings("unchecked")
    public <T>T getProxy(Class<T> clazz) {
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        return (T)o;
    }
    
}
