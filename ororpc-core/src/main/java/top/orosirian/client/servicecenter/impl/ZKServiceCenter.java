package top.orosirian.client.servicecenter.impl;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import top.orosirian.client.cache.ServiceCache;
import top.orosirian.client.servicecenter.ServiceCenter;
import top.orosirian.client.servicecenter.balance.LoadBalance;
import top.orosirian.client.servicecenter.balance.impl.ConsistencyBalance;
import top.orosirian.client.servicecenter.zkWatcher.WatchZK;

@Slf4j
public class ZKServiceCenter implements ServiceCenter {

    private static final String ZK_PATH = "127.0.0.1:2181";

    private static final String ROOT_PATH = "MyRPC";

    private static final String RETRY_PATH = "CanRetry";

    private final CuratorFramework client;    // zk客户端

    private final ServiceCache cache;

    private final LoadBalance loadBalance = new ConsistencyBalance();

    private Set<String> retryServiceCache = new CopyOnWriteArraySet<>();

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
            List<String> addrList = cache.getServiceAddr(serviceName);
            if(addrList == null) {
                addrList = client.getChildren().forPath("/" + serviceName);    // 得到/serviceName文件夹下的所有服务
                for(String addr : addrList) {
                    cache.addService(serviceName, addr);   // 将服务地址添加到缓存中
                }
            }
            if(addrList.isEmpty()) {    // 仍然为空，则说明没有可用的服务
                log.warn("未找到服务：{}", serviceName);
                return null;
            }
            String servicePath = loadBalance.selectAddr(addrList);   // 如果有多个服务，此时先只获取第一个
            return parseAddress(servicePath);
        } catch(Exception e) {
            log.error("服务发现失败，服务名：{}", serviceName, e);
            return null;
        }
    }

    // TODO： 方法签名？
    @Override
    public boolean checkRetry(String serviceName) {
        if(retryServiceCache.isEmpty()) {   //
            try {
                CuratorFramework rootClient = client.usingNamespace(RETRY_PATH);
                InetSocketAddress serviceAddr = discoverService(serviceName);
                List<String> retryableMethods = rootClient.getChildren().forPath("/" + getServiceAddrString(serviceAddr));
                retryServiceCache.addAll(retryableMethods);
            } catch (Exception e) {
                log.error("获取重试服务列表失败", e);
            }
        }
        return retryServiceCache.contains(serviceName);
    }

    private InetSocketAddress parseAddress(String address) {
        String[] parts = address.split(":");
        return new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));
    }

    private String getServiceAddrString(InetSocketAddress serviceAddr) {
        return serviceAddr.getHostName() + ":" + serviceAddr.getPort();
    }
    
}
