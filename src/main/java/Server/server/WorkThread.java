package Server.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

import Server.provider.ServiceProvider;
import common.Message.RpcRequest;
import common.Message.RpcResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WorkThread implements Runnable {

    private Socket socket;

    private ServiceProvider serviceProvider;

    @Override
    public void run() {
        try {
            // ​​ObjectOutputStream在初始化时会立即写入一个16字节的协议头，而​​ObjectInputStream在初始化时会阻塞等待读取这个协议头​​
            // 如果双方都先初始化ois，就会相互等待导致死锁
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            
            RpcRequest rpcRequest = (RpcRequest) ois.readObject();  // 读取客户端传过来的request
            RpcResponse rpcResponse = getResponse(rpcRequest);      // 反射调用服务方法获取返回值
            oos.writeObject(rpcResponse);
            oos.flush();
        } catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private RpcResponse getResponse(RpcRequest rpcRequest) {
        String interfaceName = rpcRequest.getInterfaceName();   // 得到服务名
        Object service = serviceProvider.getService(interfaceName);     // 得到服务端相应的实现类
        Method method = null;
        try {
            // 服务器又不用隐藏调用细节，为什么还要用代理呢？
            // 因为从传来点调用字符串，得到所要调用的方法，一看就得是代理
            method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamsType());
            Object result = method.invoke(service, rpcRequest.getParams());
            return RpcResponse.success(result);
        } catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            System.out.println("[] 方法执行过程中出错");
            return RpcResponse.fail();
        }
    }
    
}
