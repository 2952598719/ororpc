package top.orosirian.client.servicecenter.balance;

import java.util.List;

public interface LoadBalance {

    String selectAddr(List<String> addrList);
    
}
