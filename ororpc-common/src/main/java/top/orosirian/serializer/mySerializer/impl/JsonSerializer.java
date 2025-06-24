package top.orosirian.serializer.mySerializer.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import common.Message.RpcRequest;
import common.Message.RpcResponse;
import common.serialize.mySerializer.Serializer;

public class JsonSerializer implements Serializer {

    @Override
    public byte[] serialize(Object obj) {
        byte[] bytes = JSONObject.toJSONBytes(obj);     // 用第三方库很方便
        return bytes;
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        Object obj = null;
        switch (messageType) {
            case 0:         // Request
                RpcRequest request = JSON.parseObject(bytes, RpcRequest.class);
                Object[] params = new Object[request.getParams().length];
                for(int i = 0; i <= params.length - 1; i++) {
                    Class<?> paramsType = request.getParamsType()[i];   // 这里是client预先组装放入的，param可能经历一些处理，从而和paramType不对应
                    Object param = request.getParams()[i];
                    if(paramsType.isAssignableFrom(param.getClass())) {
                        params[i] = request.getParams()[i];
                    } else {
                        if(param instanceof JSONObject) {   // 还可能是由json格式存储的，所以可能要强转一下
                            params[i] = JSONObject.toJavaObject((JSONObject) param, paramsType);
                        } else {
                            throw new IllegalArgumentException("Unsupported parameter type");
                        }
                    }
                }
                request.setParams(params);
                obj = request;
                break;
            case 1:         // Response
                RpcResponse response = JSON.parseObject(bytes, RpcResponse.class);
                Class<?> dataType = response.getDataType();
                Object data = response.getData();
                if(dataType.isAssignableFrom(data.getClass())) {
                    
                } else {
                    if(data instanceof JSONObject) {
                        response.setData(JSONObject.toJavaObject((JSONObject)data, dataType));
                    } else {
                        throw new IllegalArgumentException("Unsupported data type");
                    }
                }
                obj = response;
                break;
            default:
                System.out.println("[] 暂时不支持此类数据");
                throw new RuntimeException();
        }
        return obj;
    }

    @Override
    public int getType() {
        return 1;
    }
    
}
