package Server.provider;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import Server.serviceRegister.ServiceRegister;
import Server.serviceRegister.impl.ZKServiceRegister;

public class ServiceProvider {

    private String host;

    private int port;

    private Map<String, Object> interfaceProvider;  // 存放服务实例

    private ServiceRegister serviceRegister;

    public ServiceProvider(String host, int port) {
        this.host = host;
        this.port = port;
        this.interfaceProvider = new HashMap<>();
        this.serviceRegister = new ZKServiceRegister();
    }

    // 注册服务到本地
    public void provideServiceInterface(Object service) {
        String serviceName = service.getClass().getName();
        Class<?>[] interfaceName = service.getClass().getInterfaces();
        for(Class<?> clazz : interfaceName) {
            interfaceProvider.put(clazz.getName(), service);    // 一方面注册到本机
            serviceRegister.registerService(clazz.getName(), new InetSocketAddress(host, port));
        }
    }

    // 获取服务实例
    public Object getService(String interfaceName) {
        return interfaceProvider.get(interfaceName);
    }



}

