package top.orosirian.client.servicecenter.zkWatcher;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;

import lombok.AllArgsConstructor;
import top.orosirian.client.cache.ServiceCache;

@Slf4j
@AllArgsConstructor
public class WatchZK {

    private CuratorFramework client;

    private ServiceCache cache;

    // 监听zk的修改，如果遇到zk修改了，则返回来修改本地缓存
    public void watchToUpdate(String path) throws InterruptedException {
        CuratorCache curatorCache = CuratorCache.build(client, "/");
        curatorCache.listenable().addListener(new CuratorCacheListener() {
            @Override
            public void event(Type type, ChildData oldData, ChildData data) {   // 事件类型、节点更新前状态，节点更新后状态
                // 卡哥忘写了一个原理，就是curatorCache.start时，会遍历目标路径下的所有节点，并触发NODE_CREATED事件

                switch(type.name()) {
                    case "NODE_CREATED":    // 得到创建节点的通知，直接放入缓存，此时oldData为空
                        String[] pathToCreate = parsePath(data);
                        if(pathToCreate.length <= 2) {
                            // path结构为"/<serviceName>/<serviceAddr>"，split结果为["", "<serviceName>", "<serviceAddr>"]
                            // 所以需要长度大于3才表明这个正常，否则可能是递归创建父节点
                            break;
                        } else {
                            String serviceName = pathToCreate[1];
                            String serviceAddr = pathToCreate[2];
                            cache.addService(serviceName, serviceAddr);
                            log.info("节点创建：服务名称 {} 地址 {}", serviceName, serviceAddr);
                        }
                        break;
                    case "NODE_CHANGED":    // 得到节点更新的通知，删除旧的，放入新的
                        if (oldData.getData() != null) {
                            log.debug("修改前的数据: {}", new String(oldData.getData()));
                        } else {
                            log.debug("节点第一次赋值!");
                        }
                        String[] oldPaths = parsePath(oldData);
                        String[] newPaths = parsePath(data);
                        cache.replaceServiceAddr(oldPaths[1], oldPaths[2], newPaths[2]);
                        log.info("节点更新：服务名称 {} 地址从 {} 更新为 {}", oldPaths[1], oldPaths[2], newPaths[2]);
                        break;
                    case "NODE_DELETE":     // 得到节点删除的通知，此时data为空
                        String[] pathToDelete = parsePath(oldData);
                        if(pathToDelete.length <= 2) {
                            break;
                        } else {
                            String serviceName = pathToDelete[1];
                            String serviceAddr = pathToDelete[2];
                            cache.deleteServiceAddr(serviceName, serviceAddr);
                            log.info("节点删除：服务名称 {} 地址 {}", serviceName, serviceAddr);
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        curatorCache.start();
    }

    // 获取更新后的路径，转换为
    public String[] parsePath(ChildData data) {
        String path = data.getPath();
        log.info("节点路径:{}",path);
        return path.split("/");
    }

    
}
