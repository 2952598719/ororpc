package Client;

import Client.proxy.ClientProxy;
import Client.rpcClient.RpcClient;
import Client.rpcClient.impl.NettyRpcClient;
import Client.rpcClient.impl.SimpleSocketRpcClient;
import common.pojo.User;
import common.service.UserService;

public class TestClient {

    public static void main(String[] args) {
        ClientProxy clientProxy = new ClientProxy(getClient(0));
        UserService proxy = clientProxy.getProxy(UserService.class);

        User user = proxy.getUserByUserId(1);
        System.out.println("[] 从服务端获取user=" + user.toString());

        User u = User.builder()
                        .id(100)
                        .userName("xxx")
                        .sex(true)
                        .build();
        Integer id = proxy.insertUserId(u);
        System.out.println("[] 向服务器插入id为" + id + "的user");
    }

    private static RpcClient getClient(int type) {
        RpcClient rpcClient = null;
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
        return rpcClient;
    } 
    
}
