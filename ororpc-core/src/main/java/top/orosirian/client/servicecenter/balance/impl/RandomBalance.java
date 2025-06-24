package top.orosirian.client.servicecenter.balance.impl;

import lombok.extern.slf4j.Slf4j;
import top.orosirian.client.servicecenter.balance.LoadBalance;

import java.util.List;
import java.util.Random;

@Slf4j
public class RandomBalance implements LoadBalance {

    Random random = new Random();

    @Override
    public String selectAddr(List<String> addrList) {
        if (addrList == null || addrList.isEmpty()) {
            throw new IllegalArgumentException("地址列表不能为空");
        }
        int currentIndex = random.nextInt(addrList.size());
        log.info("随机负载均衡选择了: " + currentIndex + "号服务器，地址为: " + addrList.get(currentIndex));
        return addrList.get(currentIndex);
    }
    
}
