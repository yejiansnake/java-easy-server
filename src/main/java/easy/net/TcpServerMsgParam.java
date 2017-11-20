package easy.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public class TcpServerMsgParam {
    public long id;
    public Channel channel;
    public ByteBuf buffer;
    public Object refObj;
}
