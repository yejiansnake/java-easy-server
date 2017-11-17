package easy.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public interface NetServerHandler {
    int getMsgSizeByteCount();
    int getMsgSize(ByteBuf byteBuf);
    int handleMsg(ChannelHandlerContext ctx, ByteBuf byteBuf);
}
