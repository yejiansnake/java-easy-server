package easy.net;

import io.netty.buffer.ByteBuf;

public interface UdpServerHandler {
    int getMsgSizeFieldByteCount();
    int getMsgSize(ByteBuf buffer);
    void handleMsg(UdpServerMsgParam param);
}
