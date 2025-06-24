package top.orosirian.server.serviceRegister.impl;

import java.net.InetSocketAddress;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import top.orosirian.server.serviceRegister.ServiceRegister;

public class ZKServiceRegister implements ServiceRegister {

    private static final String ZK_PATH = "127.0.0.1:2181";

    private static final String ROOT_PATH = "MyRPC";

    private static final String RETRY_PATH = "CanRetry";

    private CuratorFramework client;    // ZK连接客户端

    public ZKServiceRegister() {
        client = CuratorFrameworkFactory.builder()
                                        .connectString(ZK_PATH)
                                        .sessionTimeoutMs(40000)    // Server和zk的连接session在40s没通信后关闭
                                        .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                                        .namespace(ROOT_PATH)
                                        .build();
        client.start();
        System.out.println("zk连接中...");
    }

    @Override
    public void registerService(String serviceName, InetSocketAddress serviceAddress) {
        try {
            // zk理解成一个文件夹就好
            // 比如说先create一个/service，接着create一个/service/a，create一个/service/b，那么此时的结构就是service下有a和b两个节点
            // 不要理解成map的形式，不会相互覆盖

            String path = "/" + serviceName + "/" + getServiceAddress(serviceAddress);
            if(client.checkExists().forPath(path) != null) {
                return;
            }
            client.create()
                // creatingParentsIfNeeded自动创建的父节点是持久的，creatingParentContainersIfNeeded在子节点全删除之后也会删除
                .creatingParentsIfNeeded()         // 递归创建，比如路径是/service/a，如果没有service节点就会自动创建
                .withMode(CreateMode.EPHEMERAL)             // 临时节点，服务器和zk的连接断开后就删除
                .forPath(path);
            if(canRetry) {
                String retryPath = "/" + RETRY_PATH + "/" + serviceName;    // retry相关就不用带上地址了，只要能够确认能否重试即可
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(retryPath);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public String getServiceAddress(InetSocketAddress serviceAddress) {
        return serviceAddress.getHostName() + ":" + serviceAddress.getPort();
    }
    
}
