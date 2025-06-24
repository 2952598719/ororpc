package top.orosirian.client.proxy;

import lombok.extern.slf4j.Slf4j;
import top.orosirian.client.circuitBreaker.CircuitBreaker;
import top.orosirian.client.circuitBreaker.CircuitBreakerProvider;
import top.orosirian.client.retry.GuavaRetry;
import top.orosirian.client.rpcClient.RpcClient;
import top.orosirian.client.rpcClient.impl.NettyRpcClient;
import top.orosirian.client.servicecenter.ServiceCenter;
import top.orosirian.client.servicecenter.impl.ZKServiceCenter;
import top.orosirian.message.RpcRequest;
import top.orosirian.message.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Slf4j
public class ClientProxy implements InvocationHandler {

    private final RpcClient rpcClient;

    private final ServiceCenter serviceCenter;

    private final CircuitBreakerProvider circuitBreakerProvider;

    public ClientProxy() throws InterruptedException {
        this.rpcClient = new NettyRpcClient();
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
            log.warn("熔断器开启，请求被拒绝: {}", request);
            return null;
        }
        // 3.数据传输
        RpcResponse response;
        String methodSignature = getMethodSignature(request.getInterfaceName(), method);
        log.info("方法签名: {}", methodSignature);
        if(serviceCenter.checkRetry(request.getInterfaceName())) {
            try {
                log.info("尝试重试调用服务: {}", methodSignature);
                response = new GuavaRetry().sendServiceWithRetry(request, rpcClient);
            } catch(Exception e) {
                log.error("重试调用失败: {}", methodSignature, e);
                circuitBreaker.recordFailure();
                throw e;  // 将异常抛给调用者
            }
        } else {    // 只调用一次
            response = rpcClient.sendRequest(request);
        }
        // 4.记录response状态，上报给熔断器
        if(response != null) {
            if(response.getCode() == 200) {
                circuitBreaker.recordSuccess();
            } else if(response.getCode() == 500) {
                circuitBreaker.recordFailure();
            }
            log.info("收到响应: {} 状态码: {}", request.getInterfaceName(), response.getCode());
        }
        // 5.返回数据
        return response != null ? response.getData() : null;
    }

    @SuppressWarnings("unchecked")
    public <T>T getProxy(Class<T> clazz) {
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        return (T)o;
    }


    // 得到方法签名，格式为：接口名#方法名(参数类型1,参数类型2,参数类型3)
    private String getMethodSignature(String interfaceName, Method method) {
        StringBuilder builder = new StringBuilder();
        builder.append(interfaceName).append("#").append(method.getName()).append("(");
        Class<?>[] parameterTypes = method.getParameterTypes();
        for(int i = 0; i <= parameterTypes.length - 1; i++) {
            builder.append(parameterTypes[i].getName());
            if(i != parameterTypes.length - 1) {
                builder.append(",");
            } else {
                builder.append(")");
            }
        }
        return builder.toString();
    }

    public void close() {
        rpcClient.close();
        serviceCenter.close();
        // CircuitBreakerProvider没有要关的，所以不写close。
        // 这代码让人看得很烦躁，很想打人，有些地方写得很蠢
    }
    
}
