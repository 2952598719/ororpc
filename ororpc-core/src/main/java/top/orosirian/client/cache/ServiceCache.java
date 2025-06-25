package top.orosirian.client.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class ServiceCache {

    // 服务名 -> 服务地址列表
    private final Map<String, List<String>> addressCache = new HashMap<>();

    // 包含在set中的方法可以重试，以方法签名为粒度
    private Set<String> retryCache = new HashSet<>();

    public void addService(String interfaceName, String serverAddress) {
        List<String> serverAddressList = addressCache.getOrDefault(interfaceName, new ArrayList<>());
        serverAddressList.add(serverAddress);
        addressCache.put(interfaceName, serverAddressList);
        log.info("将name为{}和地址为{}的服务添加到本地缓存中", interfaceName, serverAddress);
    }

    public void replaceServiceAddress(String interfaceName, String oldAddress, String newAddress) {
        if(addressCache.containsKey(interfaceName) && addressCache.get(interfaceName).contains(oldAddress)) {
            List<String> addrList = addressCache.get(interfaceName);
            addrList.remove(oldAddress);   // 服务数量导致的查询时间相比于其他时间一般不会太多，因此O(n)可以容忍
            addrList.add(newAddress);
            log.info("替换服务{}的地址{}为{}", interfaceName, oldAddress, newAddress);
        } else {
            log.error("服务{}的地址列表中不存在{}，修改失败", interfaceName, newAddress);
        }
    }

    public List<String> getServiceAddressList(String interfaceName) {
        if(addressCache.containsKey(interfaceName)) {
            return addressCache.get(interfaceName);
        } else {
            log.warn("未发现服务{}", interfaceName);
            return Collections.emptyList(); // 返回空列表，防止空指针异常
        }
    }
    
    public void deleteServiceAddress(String interfaceName, String address) {
        if(addressCache.containsKey(interfaceName) && addressCache.get(interfaceName).contains(address)) {
            List<String> addressList = addressCache.get(interfaceName);
            addressList.remove(address);
            log.info("已将name为{}和地址为{}的服务从本地缓存中删除", interfaceName, address);
            if (addressList.isEmpty()) {
                addressCache.remove(interfaceName);  // 移除该服务的缓存条目
                log.info("服务{}的地址列表为空，已从缓存中清除", interfaceName);
            }
        } else {
            log.warn("未发现服务{}中的地址{}，删除失败", interfaceName, address);
        }
    }

    public boolean checkRetry(InetSocketAddress serviceAddress, String methodSignature) {
        
    }



}
