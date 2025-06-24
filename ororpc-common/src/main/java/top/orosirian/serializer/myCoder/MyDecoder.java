package top.orosirian.serializer.myCoder;

import java.util.List;

import common.Message.MessageType;
import common.serialize.mySerializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class MyDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 1.读取消息类型
        short messageType = in.readShort();
        if(messageType != MessageType.REQUEST.getCode() && messageType != MessageType.RESPONSE.getCode()) {
            System.out.println("[] 暂不支持该类型数据");
            return;
        }
        // 2.读取序列化方式，并构造相应的序列化器
        short serializerType = in.readShort();
        Serializer serializer = Serializer.getSerializerByType(serializerType);
        if(serializer == null) {
            throw new RuntimeException("不存在对应的序列化器");
        }
        // 3.读取序列化数组长度
        int length = in.readInt();
        // 4.读取数据
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        Object desirialize = serializer.deserialize(bytes, messageType);
        out.add(desirialize);
    }

    
    
}
