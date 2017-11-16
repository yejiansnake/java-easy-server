package easy.net.factory;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;

public class EpollChannelFactory implements ChannelFactory {

    @Override
    public EventLoopGroup createEventLoopGroup(int threadCount) {
        return new EpollEventLoopGroup(threadCount);
    }

    @Override
    public Class getServerSocketChannelClass() {
        return EpollServerSocketChannel.class;
    }

    @Override
    public Class getDatagramChannelClass() {
        return EpollDatagramChannel.class;
    }
}
