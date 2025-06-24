package top.orosirian.serializer.mycoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.orosirian.message.MessageType;
import top.orosirian.message.RpcRequest;
import top.orosirian.message.RpcResponse;
import top.orosirian.serializer.myserializer.Serializer;

@Slf4j
@AllArgsConstructor
public class MyEncoder extends MessageToByteEncoder {

    private Serializer serializer;

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
            log.debug("正在编码消息类型: {}", msg.getClass());
        // 0.得到数据
        byte[] serializeBytes = serializer.serialize(msg);
        if(serializeBytes == null || serializeBytes.length == 0) {
            throw new IllegalArgumentException("序列化消息为空");
        }
        // 1.写入消息类型，2字节
        if(msg instanceof RpcRequest) {
            out.writeShort(MessageType.REQUEST.getCode());
        } else if(msg instanceof RpcResponse) {
            out.writeShort(MessageType.RESPONSE.getCode());
        } else {
            log.error("未知消息类型: {}", msg.getClass());
            throw new IllegalArgumentException("未知消息类型: " + msg.getClass());
        }
        // 2.写入序列化方式，2字节
        out.writeShort(serializer.getType());  
        // 3.写入长度，4字节
        out.writeInt(serializeBytes.length);
        // 4.写入数据
        out.writeBytes(serializeBytes);
    }

}
