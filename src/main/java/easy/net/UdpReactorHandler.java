package easy.net;

import io.netty.buffer.ByteBuf;

public interface UdpReactorHandler {
    int getMsgSizeFieldByteCount();
    int getMsgSize(ByteBuf buffer);
    void handleMsg(UdpReactorMsgParam param);
}
