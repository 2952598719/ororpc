package Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import common.Message.RpcRequest;
import common.Message.RpcResponse;

public class IOClient {

    public static RpcResponse sendRequest(String host, int port, RpcRequest request) {
        try {
            Socket socket = new Socket(host, port);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            oos.writeObject(request);
            oos.flush();
            RpcResponse response = (RpcResponse)ois.readObject();
            return response;
        } catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
    
}
