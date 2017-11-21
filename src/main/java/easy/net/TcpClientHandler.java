package easy.net;

import io.netty.buffer.ByteBuf;

public interface TcpClientHandler {
    int getMsgSizeFieldByteCount();
    int getMsgSize(ByteBuf buffer);
    void handleMsg(TcpServerMsgParam param);
}