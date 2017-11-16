package easy.net.factory;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NioChannelFactory implements ChannelFactory {

    @Override
    public EventLoopGroup createEventLoopGroup(int threadCount) {
        return new NioEventLoopGroup(threadCount);
    }

    @Override
    public Class getServerSocketChannelClass() {
        return NioServerSocketChannel.class;
    }

    @Override
    public Class getDatagramChannelClass() {
        return NioDatagramChannel.class;
    }
}
