package Server.serviceRegister.impl;

import java.net.InetSocketAddress;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import Server.serviceRegister.ServiceRegister;

public class ZKServiceRegister implements ServiceRegister {

    private CuratorFramework client;    // ZK连接客户端

    private static final String ZK_PATH = "127.0.0.1:2181";

    private static final String ROOT_PATH = "MyRPC";

    public ZKServiceRegister() {
        client = CuratorFrameworkFactory.builder()
                                        .connectString(ZK_PATH)
                                        .sessionTimeoutMs(40000)    // Server和zk的连接session在40s没通信后关闭
                                        .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                                        .namespace(ROOT_PATH)
                                        .build();
        client.start();
        System.out.println("zk 连接成功");
    }

    @Override
    public void registerService(String serviceName, InetSocketAddress serviceAddress) {
        try {
            // zk理解成一个文件夹就好
            // 比如说先create一个/service，接着create一个/service/a，create一个/service/b，那么此时的结构就是service下有a和b两个节点
            // 不要理解成map的形式，不会相互覆盖

            String path = "/" + serviceName + "/" + getServiceAddress(serviceAddress);
            client.create()
                .creatingParentContainersIfNeeded()         // 递归创建，比如路径是/service/a，如果没有service节点就会自动创建。注意自动创建的父节点是持久的
                .withMode(CreateMode.EPHEMERAL)             // 临时节点，服务器和zk的连接断开后就删除
                .forPath(path);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public String getServiceAddress(InetSocketAddress serviceAddress) {
        return serviceAddress.getHostName() + ":" + serviceAddress.getPort();
    }
    
}
