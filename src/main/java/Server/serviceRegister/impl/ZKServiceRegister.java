package Server.serviceRegister.impl;

import java.net.InetSocketAddress;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import Server.serviceRegister.ServiceRegister;

public class ZKServiceRegister implements ServiceRegister {

    private CuratorFramework client;

    private static final String ZK_PATH = "127.0.0.1:2181";

    private static final String ROOT_PATH = "MyRPC";

    public ZKServiceRegister() {
        RetryPolicy policy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                                        .connectString(ZK_PATH)
                                        .sessionTimeoutMs(40000)
                                        .retryPolicy(policy)
                                        .namespace(ROOT_PATH)
                                        .build();
        client.start();
        System.out.println("zk 连接成功");
    }

    @Override
    public void registerService(String serviceName, InetSocketAddress serviceAddress) {
        try {
            // 这个路径像是瞎写的，咋眼一看是/user/127.0.0.1:80，离天下之大谱，还有反过来写的
            // 但其实背后有目的，这里的/不是路径分隔符，是为了将serviceName创建成永久节点，服务提供者下线时，不删服务名，只删地址
            if(client.checkExists().forPath("/" + serviceName) == null) {
                client.create()
                        .creatingParentContainersIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath("/" + serviceName);
            }
            String path = "/" + serviceName + "/" + getServiceAddress(serviceAddress);
            client.create()
                .creatingParentContainersIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath(path);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public String getServiceAddress(InetSocketAddress serviceAddress) {
        return serviceAddress.getHostName() + ":" + serviceAddress.getPort();
    }
    
}
