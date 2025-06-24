package Client.serviceCenter.balance;

import java.util.List;

public interface LoadBalance {

    String selectAddr(List<String> addrList);
    
}
