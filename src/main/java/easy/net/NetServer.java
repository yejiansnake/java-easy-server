package easy.net;

import easy.net.factory.ChannelFactory;
import easy.net.factory.FactoryCreator;
import easy.net.factory.FactoryType;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.security.InvalidParameterException;

public class NetServer {

    private NetServerConfig _config;
    private ServerBootstrap _server;
    private ChannelFuture _channelFuture;
    private EventLoopGroup _acceptGroup;
    private EventLoopGroup _workerGroup;

    public NetServer() {

    }

    //启动
    public void start(NetServerConfig config) throws Exception {
        this.checkConfig(config);
        ChannelFactory factory = this.getChannelFactory(config);

        _config = config;

        //工作线程
        _acceptGroup = factory.createEventLoopGroup(_config.acceptThreadCount);
        _workerGroup = factory.createEventLoopGroup(_config.recvThreadCount);

        //配置server 并启动
        try {
            _server = new ServerBootstrap();
            _server.group(_acceptGroup, _workerGroup);

            _server.channel(factory.getServerSocketChannelClass());

            _server.childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ChannelHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, _config.backLog)
                    .childOption(ChannelOption.SO_SNDBUF, 8 * 1024)     //发送和接收缓冲区默认8K
                    .childOption(ChannelOption.SO_RCVBUF, 8 * 1024);

            //系统级别的keepalive 不一定对方也打开，如果对方不开，就没用了
            if (_config.enabledSysKeepAlive) {
                _server.childOption(ChannelOption.SO_KEEPALIVE, true);
            }

            // 绑定端口并启动接收
            _channelFuture = _server.bind(_config.port).sync(); // (7)

        } finally {
            this.stop();
        }
    }

    public void stop() throws Exception {
        // Wait until the server socket is closed.
        // In this example, this does not happen, but you can do that to gracefully
        // shut down your server.

        if (_channelFuture != null) {
            _channelFuture.channel().closeFuture().sync();
        }

        //释放线程
        if (_workerGroup != null) {
            _workerGroup.shutdownGracefully();
        }

        if (_acceptGroup != null) {
            _acceptGroup.shutdownGracefully();
        }
    }

    private void checkConfig(NetServerConfig config) throws Exception {

    }

    public ChannelFactory getChannelFactory(NetServerConfig config) throws Exception {
        if (_config.core.equals("nio")) {
            return FactoryCreator.getInstance().getNioChannelFactory();
        } else if (_config.core.equals("epoll")) {
            return FactoryCreator.getInstance().getEpollChannelFactory();
        }

        throw new InvalidParameterException(String.format("core:%s invalid",  _config.core));
    }
}
