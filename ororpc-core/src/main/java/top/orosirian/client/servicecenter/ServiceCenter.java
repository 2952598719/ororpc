package top.orosirian.client.servicecenter;

import java.net.InetSocketAddress;

// 服务注册中心接口
public interface ServiceCenter {

    InetSocketAddress discoverService(String interfaceName);     // 根据接口名，向注册中心询问对应服务器的host:port列表，并根据负载均衡选择

    boolean checkRetry(String interfaceName, String methodSignature);

    void close();
    
}
