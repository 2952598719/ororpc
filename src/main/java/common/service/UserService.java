package common.service;

import common.pojo.User;

// 客户端通过这个公开接口调用服务端的实现类
public interface UserService {
    
    User getUserByUserId(Integer id);

    Integer insertUserId(User user);
    
}
