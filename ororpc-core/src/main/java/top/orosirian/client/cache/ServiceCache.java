package top.orosirian.client.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ServiceCache {

    private final Map<String, List<String>> cache = new HashMap<>();

    public void addService(String serviceName, String address) {
        List<String> addrList = cache.getOrDefault(serviceName, new ArrayList<>());
        addrList.add(address);
        cache.put(serviceName, addrList);
        log.info("将name为{}和地址为{}的服务添加到本地缓存中", serviceName, address);
    }

    public void replaceServiceAddr(String serviceName, String oldAddr, String newAddr) {
        if(cache.containsKey(serviceName)) {
            List<String> addrList = cache.get(serviceName);
            addrList.remove(oldAddr);   // 服务数量导致的查询时间相比于其他时间一般不会太多，因此O(n)可以容忍
            addrList.add(newAddr);
            log.info("替换服务{}的地址{}为{}", serviceName, oldAddr, newAddr);
        } else {
            log.error("服务{}的地址列表中不存在{}，修改失败", serviceName, oldAddr);
        }
    }

    public List<String> getServiceAddr(String serviceName) {
        if(cache.containsKey(serviceName)) {
            return cache.get(serviceName);
        } else {
            log.warn("未发现服务{}", serviceName);
            return Collections.emptyList(); // 返回空列表，防止空指针异常
        }
    }
    
    public void deleteServiceAddr(String serviceName, String addr) {
        if(cache.containsKey(serviceName) && cache.get(serviceName).contains(addr)) {
            List<String> addrList = cache.get(serviceName);
            addrList.remove(addr);
            log.info("已将name为{}和地址为{}的服务从本地缓存中删除", serviceName, addr);
            if (addrList.isEmpty()) {
                cache.remove(serviceName);  // 移除该服务的缓存条目
                log.info("服务{}的地址列表为空，已从缓存中清除", serviceName);
            }
        } else {
            log.warn("未发现服务{}中的地址{}，删除失败", serviceName, addr);
        }
    }
}
