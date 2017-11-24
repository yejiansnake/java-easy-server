package client;

import easy.net.TcpClientHandler;
import easy.net.TcpClientMsgParam;
import io.netty.buffer.ByteBuf;

public class MyTcpClientHandler implements TcpClientHandler {
    public int getMsgSizeFieldByteCount() {
        return Integer.BYTES;
    }

    public int getMsgSize(ByteBuf buffer) {
        return buffer.getInt(0);
    }

    public void handleMsg(TcpClientMsgParam param) {
        int value = param.buffer.getInt(8);
        System.out.printf("handle msg value:%d \n", value);
    }
}