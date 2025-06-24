package top.orosirian.serializer.myCoder;

import common.Message.MessageType;
import common.Message.RpcRequest;
import common.Message.RpcResponse;
import common.serialize.mySerializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;

@SuppressWarnings("rawtypes")
@AllArgsConstructor
public class MyEncoder extends MessageToByteEncoder {

    private Serializer serializer;

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        System.out.println(msg.getClass());
        // 1.写入消息类型
        if(msg instanceof RpcRequest) {
            out.writeShort(MessageType.REQUEST.getCode());
        } else if(msg instanceof RpcResponse) {
            out.writeShort(MessageType.RESPONSE.getCode());
        }
        // 2.写入序列化方式
        out.writeShort(serializer.getType());  
        // 3.写入长度
        byte[] serializeBytes = serializer.serialize(msg);
        out.writeInt(serializeBytes.length);
        // 4.写入数据
        out.writeBytes(serializeBytes);
    }

}
