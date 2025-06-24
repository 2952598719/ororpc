package Client.serviceCenter.balance.impl;

import java.util.List;
import java.util.Random;

import Client.serviceCenter.balance.LoadBalance;

public class RandomBalance implements LoadBalance {

    Random random = new Random();

    @Override
    public String selectAddr(List<String> addrList) {
        int index = random.nextInt(addrList.size());
        return addrList.get(index);
    }
    
}
