package Client.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceCache {

    private Map<String, List<String>> cache = new HashMap<>();

    public void addServiceToCache(String serviceName, String address) {
        List<String> addrList = cache.getOrDefault(serviceName, new ArrayList<>());
        addrList.add(address);
        cache.put(serviceName, addrList);
    }

    public void replaceServiceAddress(String serviveName, String oldAddr, String newAddr) {
        if(cache.containsKey(serviveName)) {
            List<String> addrList = cache.get(serviveName);
            addrList.remove(oldAddr);   // 服务数量导致的查询时间相比于其他时间一般不会太多，因此O(n)可以容忍
            addrList.add(newAddr);
        } else {
            System.out.println("服务不存在，修改失败");
        }
    }

    public List<String> getServiceAddrFromCache(String serviceName) {
        return cache.getOrDefault(serviceName, null);
    }
    
    public void delete(String serviceName, String addr) {
        if(cache.containsKey(addr)) {
            List<String> addrList = cache.get(serviceName);
            addrList.remove(addr);
        } else {
            System.out.println("服务不存在，删除失败");
        }
    }
}
