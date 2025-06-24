package top.orosirian.serializer.myserializer;

import top.orosirian.exception.SerializeException;
import top.orosirian.pojo.User;

public class SerializerUtil {

    public static Class<?> getClassForMessageType(int messageType) {
        switch (messageType) {
            case 1:
                return User.class;
            default:
                throw new SerializeException("未知的消息类型");
        }
    }

}
