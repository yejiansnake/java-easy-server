package easy.net.factory;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.DatagramChannel;

public class EpollChannelFactory implements ChannelFactory {

    public EventLoopGroup createEventLoopGroup(int threadCount) {
        return new EpollEventLoopGroup(threadCount);
    }

    public Class<? extends ServerChannel> getServerSocketChannelClass() {
        return EpollServerSocketChannel.class;
    }

    public Class<? extends DatagramChannel> getDatagramChannelClass() {
        return EpollDatagramChannel.class;
    }
}
