package easy.net.factory;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NioChannelFactory implements ChannelFactory {

    public EventLoopGroup createEventLoopGroup(int threadCount) {
        return new NioEventLoopGroup(threadCount);
    }

    public Class<? extends ServerChannel> getServerSocketChannelClass() {
        return NioServerSocketChannel.class;
    }

    public Class<? extends DatagramChannel> getDatagramChannelClass() {
        return NioDatagramChannel.class;
    }
}
