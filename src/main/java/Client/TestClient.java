package Client;

import Client.proxy.ClientProxy;
import Client.rpcClient.RpcClient;
import Client.rpcClient.impl.NettyRpcClient;
import Client.rpcClient.impl.SimpleSocketRpcClient;
import common.pojo.User;
import common.service.UserService;

public class TestClient {

    public static void main(String[] args) throws InterruptedException {
        int type = 0;
        ClientProxy clientProxy = new ClientProxy(getClient(type));
        UserService proxy = clientProxy.getProxy(UserService.class);

        for (int i = 0; i < 120; i++) {
            if(i % 30 == 0) {
                Thread.sleep(10000);    // 每30秒休眠10秒
            }
            Integer userId = i;
            new Thread(() -> {
                try {
                    // 服务器那边现在就是个模拟，所以这边业务代码先考虑性能，不考虑合理性
                    User user = proxy.getUserByUserId(userId);
                    System.out.println("从服务端得到的用户信息为：" + user.toString());
                    Integer id = proxy.insertUserId(User.builder()
                                                        .id(userId)
                                                        .userName("User " + userId)
                                                        .sex(true)
                                                        .build());
                    System.out.println("插入id为：" + id.toString());
                } catch(Exception e) {
                    System.out.println(e.getMessage());
//                    e.printStackTrace();
                }
            }).start();
        }
    }

    private static RpcClient getClient(int type) {
        RpcClient rpcClient = null;
        try {
            switch (type) {
                case 0:
                    rpcClient = new NettyRpcClient();
                    break;
                case 1:
                    rpcClient = new SimpleSocketRpcClient("127.0.0.1", 9999);
                    break;
                default:
                    rpcClient = new NettyRpcClient();
                    break;
            }
        } catch(InterruptedException e) {
            System.out.println(e.getMessage());
        }
        return rpcClient;
    } 
    
}
