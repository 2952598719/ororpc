package Client.serviceCenter.impl;

import java.net.InetSocketAddress;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import Client.serviceCenter.ServiceCenter;

public class ZKServiceCenter implements ServiceCenter {

    private CuratorFramework client;    // zk客户端

    private static final String ZK_PATH = "127.0.0.1:2181";

    private static final String ROOT_PATH = "MyRPC";

    public ZKServiceCenter() {
        this.client = CuratorFrameworkFactory.builder()
                                            .connectString(ZK_PATH)     // zk的地址固定，作为生产者和消费者的预备知识
                                            .namespace(ROOT_PATH)       // 命名空间为MyRPC
                                            .sessionTimeoutMs(40000)           // client在40s内需要发送一次心跳，否则判断
                                            .retryPolicy(new ExponentialBackoffRetry(1000, 3))  // client无法连接到zk服务器时，按照1s-2s-4s的时间来重新尝试连接
                                            .build();
        this.client.start();
        System.out.println("zk连接成功");
    }

    @Override
    public InetSocketAddress discoverService(String serviceName) {
        try {
            List<String> services = client.getChildren().forPath("/" + serviceName);    // 得到/serviceName文件夹下的所有服务
            String servicePath = services.get(0);   // 如果有多个服务，此时先只获取第一个
            return parseAddress(servicePath);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private InetSocketAddress parseAddress(String address) {
        String[] parts = address.split(":");
        return new InetSocketAddress(parts[0], Integer.valueOf(parts[1]));
    }
    
}
