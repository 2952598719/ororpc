package Client.serviceCenter;

import java.net.InetSocketAddress;

// 服务注册中心接口
public interface ServiceCenter {

    InetSocketAddress discoverService(String serviceName);     // 根据serviceName，向注册中心询问该服务器的host:port

    boolean checkRetry(String serviceName);
    
}
