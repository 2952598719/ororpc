package top.orosirian.server.serviceRegister;

import java.net.InetSocketAddress;

// 服务注册接口
public interface ServiceRegister {

    void registerService(String serviceName, InetSocketAddress serviceAddress);
    
}
