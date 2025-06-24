package Client.serviceCenter.impl;

import java.net.InetSocketAddress;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import Client.cache.ServiceCache;
import Client.serviceCenter.ServiceCenter;
import Client.serviceCenter.balance.LoadBalance;
import Client.serviceCenter.balance.impl.ConsistencyBalance;
import Client.serviceCenter.zkWatcher.WatchZK;

public class ZKServiceCenter implements ServiceCenter {

    private static final String ZK_PATH = "127.0.0.1:2181";

    private static final String ROOT_PATH = "MyRPC";

    private static final String RETRY_PATH = "CanRetry";

    private CuratorFramework client;    // zk客户端

    private ServiceCache cache;

    private LoadBalance loadBalance = new ConsistencyBalance();

    public ZKServiceCenter() throws InterruptedException {
        this.client = CuratorFrameworkFactory.builder()
                                            .connectString(ZK_PATH)     // zk的地址固定，作为生产者和消费者的预备知识
                                            .namespace(ROOT_PATH)       // 命名空间为MyRPC
                                            .sessionTimeoutMs(40000)           // client在40s内需要发送一次心跳，否则判断
                                            .retryPolicy(new ExponentialBackoffRetry(1000, 3))  // client无法连接到zk服务器时，按照1s-2s-4s的时间来重新尝试连接
                                            .build();
        this.client.start();
        cache = new ServiceCache();
        WatchZK watcher = new WatchZK(this.client, cache);
        watcher.watchToUpdate(ROOT_PATH);
        System.out.println("zk连接中...");
    }

    @Override
    public InetSocketAddress discoverService(String serviceName) {
        try {
            List<String> serviceList = cache.getServiceAddrFromCache(serviceName);
            if(serviceList == null) {
                serviceList = client.getChildren().forPath("/" + serviceName);    // 得到/serviceName文件夹下的所有服务
            }
            String servicePath = loadBalance.selectAddr(serviceList);   // 如果有多个服务，此时先只获取第一个
            return parseAddress(servicePath);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean checkRetry(String serviceName) {
        boolean canRetry = false;
        try {
            List<String> whiteList = client.getChildren().forPath("/" + RETRY_PATH);
            for(String whiteService : whiteList) {
                if(serviceName.equals(whiteService)) {
                    canRetry = true;
                    System.out.printf("服务%s在白名单上，可进行重试\n", serviceName);
                    break;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return canRetry;
    }

    private InetSocketAddress parseAddress(String address) {
        String[] parts = address.split(":");
        return new InetSocketAddress(parts[0], Integer.valueOf(parts[1]));
    }
    
}
