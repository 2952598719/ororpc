package Client.serviceCenter.balance.impl;

import java.util.List;

import Client.serviceCenter.balance.LoadBalance;

public class RoundBalance implements LoadBalance {

    private int index = -1;

    @Override
    public String selectAddr(List<String> addrList) {
        index++;
        return addrList.get(index % addrList.size());
    }
    
}
