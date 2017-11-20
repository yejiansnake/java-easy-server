package easy.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;

public class UdpServerMsgParam {
    Channel channel;
    ByteBuf buffer;
    InetSocketAddress address;
    Object refObj;
}