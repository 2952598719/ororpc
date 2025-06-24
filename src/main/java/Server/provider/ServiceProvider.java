package Server.provider;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import Server.ratelimit.provider.RateLimitProvider;
import Server.serviceRegister.ServiceRegister;
import Server.serviceRegister.impl.ZKServiceRegister;

public class ServiceProvider {

    private String host;

    private int port;

    private Map<String, Object> interfaceProvider;  // 存放服务实例

    private ServiceRegister serviceRegister;

    private RateLimitProvider rateLimitProvider;

    public ServiceProvider(String host, int port) {
        this.host = host;
        this.port = port;
        this.interfaceProvider = new HashMap<>();
        this.serviceRegister = new ZKServiceRegister();
        this.rateLimitProvider = new RateLimitProvider();
    }

    // 注册服务
    public void provideServiceInterface(Object service, boolean canRetry) {
        // 这个类可能会实现多个接口，把每个接口都注册一下，这样就都能调用到了
        Class<?>[] interfaceName = service.getClass().getInterfaces();
        for(Class<?> clazz : interfaceName) {
            interfaceProvider.put(clazz.getName(), service);    // 一方面注册到本机
            serviceRegister.registerService(clazz.getName(), new InetSocketAddress(host, port), canRetry);
        }
    }

    // 获取服务实例
    public Object getService(String interfaceName) {
        return interfaceProvider.get(interfaceName);
    }

    public RateLimitProvider getRateLimitProvider() {
        return this.rateLimitProvider;
    }



}

