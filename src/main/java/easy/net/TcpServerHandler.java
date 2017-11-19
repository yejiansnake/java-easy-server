package easy.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public interface TcpServerHandler {
    int getMsgSizeFieldByteCount();
    int getMsgSize(ByteBuf buffer);
    void handleMsg(ChannelHandlerContext ctx, ByteBuf buffer, Object refObj);
}
