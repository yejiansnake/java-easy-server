package easy.net;

import io.netty.buffer.ByteBuf;

public interface TcpServerHandler {
    int getMsgSizeFieldByteCount();
    int getMsgSize(ByteBuf buffer);
    void handleMsg(TcpServerMsgParam param);
}