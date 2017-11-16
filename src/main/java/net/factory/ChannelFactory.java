package easy.net.factory;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.socket.DatagramChannel;

public interface ChannelFactory {
    EventLoopGroup createEventLoopGroup(int threadCount);
    Class getServerSocketChannelClass();
    Class getDatagramChannelClass();
}
