package Client.serviceCenter.balance.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import Client.serviceCenter.balance.LoadBalance;

public class ConsistencyBalance implements LoadBalance {

    private static final int VIRTUAL_NUM = 5;

    private List<String> lastAddrList = Collections.emptyList();            // 真实节点列表

    private SortedMap<Integer, String> virtualNodes = new TreeMap<>();    // 虚拟节点分配，key是节点哈希值，value是其所在服务器名字

    @Override
    public String selectAddr(List<String> addrList) {
        updateRing(addrList);
        String requestKey = UUID.randomUUID().toString();
        int hash = getHash(requestKey);
        SortedMap<Integer, String> tailMap = virtualNodes.tailMap(hash);    // hash值大于等于所请求hash值的虚拟节点
        String virtualNode = tailMap.isEmpty() ? virtualNodes.get(virtualNodes.firstKey()) : tailMap.get(tailMap.firstKey());   // 如果没找到，代表大于所有节点，从已有选个最大的就行
        return virtualNode.split("&&")[0];
    }

    private void updateRing(List<String> addrList) {
        if(addrList.equals(lastAddrList)) return;

        virtualNodes.clear();
        for(String addr : addrList) {
            for(int i = 0; i <= VIRTUAL_NUM - 1; i++) {
                String virtualNode = addr + "&&VN" + i;
                virtualNodes.put(getHash(virtualNode), virtualNode);
            }
        }
        lastAddrList = new ArrayList<>(addrList);
    }

    // FNV1_32_HASH算法
    private static int getHash(String str) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < str.length(); i++) {
            hash = (hash ^ str.charAt(i)) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        // 如果算出来的值为负数则取其绝对值
        if (hash < 0) {
            hash = Math.abs(hash);
        }
        return hash;
    }
    
}
