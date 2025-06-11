package Server.provider;

import java.util.HashMap;
import java.util.Map;

public class ServiceProvider {

    private Map<String, Object> interfaceProvider;  // 存放服务实例

    public ServiceProvider() {
        this.interfaceProvider = new HashMap<>();
    }

    // 注册服务到本地
    public void provideServiceInterface(Object service) {
        String serviceName = service.getClass().getName();
        Class<?>[] interfaceName = service.getClass().getInterfaces();
        for(Class<?> clazz : interfaceName) {
            interfaceProvider.put(clazz.getName(), service);
        }
    }

    // 获取服务实例
    public Object getService(String interfaceName) {
        return interfaceProvider.get(interfaceName);
    }



}

