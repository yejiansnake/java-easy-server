package easy.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.net.InetSocketAddress;

public interface UdpServerHandler {
    int getMsgSizeFieldByteCount();
    int getMsgSize(ByteBuf buffer);
    void handleMsg(ChannelHandlerContext ctx, ByteBuf buffer, InetSocketAddress address, Object refObj);
}
