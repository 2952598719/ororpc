package common.serialize.mySerializer;

import common.serialize.mySerializer.impl.JsonSerializer;
import common.serialize.mySerializer.impl.ObjectSerializer;

public interface Serializer {

    byte[] serialize(Object obj);   // 对象序列化成字节数组

    Object deserialize(byte[] bytes, int messageType);  // 字节数组反序列化成对象

    int getType();  // 得到所用序列化器，0-java自带，1-alibaba.fastjson

    static Serializer getSerializerByType(int type) {
        switch (type) {
            case 0:
                return new ObjectSerializer();
            case 1:
                return new JsonSerializer();
            default:
                return null;
        }
    }
    
}
