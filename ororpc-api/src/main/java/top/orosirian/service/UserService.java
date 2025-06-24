package top.orosirian.service;

import top.orosirian.annotation.Retryable;
import top.orosirian.pojo.User;

// 客户端通过这个公开接口调用服务端的实现类
public interface UserService {

    @Retryable
    User getUserByUserId(Integer id);

    @Retryable
    Integer insertUserId(User user);
    
}
