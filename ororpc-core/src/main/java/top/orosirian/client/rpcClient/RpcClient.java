package top.orosirian.client.rpcClient;

import top.orosirian.message.RpcRequest;
import top.orosirian.message.RpcResponse;

public interface RpcClient {

    RpcResponse sendRequest(RpcRequest request);
    
}
