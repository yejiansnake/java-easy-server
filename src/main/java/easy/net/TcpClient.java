package easy.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class TcpClient {
    private TcpClientConfig _config;
    private Bootstrap _client;
    private Channel _channel;
    private ChannelFuture _channelFuture;
    private EventLoopGroup _workerGroup;

    public TcpClient() {

    }

    public void start(TcpClientConfig config) throws Exception {
        this.checkConfig(config);
        _config = config;

        try {
            _client = new Bootstrap();
            _workerGroup = new NioEventLoopGroup();
            _client.group(_workerGroup);

            _client.channel(NioSocketChannel.class);

            _client.handler(new TcpChannelHandler(this))
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_SNDBUF, 8 * 1024)     //发送和接收缓冲区默认8K
                    .option(ChannelOption.SO_RCVBUF, 8 * 1024);

            //系统级别的keepalive 不一定对方也打开，如果对方不开，就没用了
            if (_config.enabledSysKeepAlive) {
                _client.option(ChannelOption.SO_KEEPALIVE, true);
            }

            // 绑定端口并启动接收
            _channelFuture = _client.connect(_config.host, _config.port).sync();
            _channel = _channelFuture.channel();

        } catch (Exception ex){
            this.stop();
            throw ex;
        }
    }

    private void createChanncel() {
        if (_client != null) {

        }

    }

    public void stop() throws Exception {

    }

    public void send(Object msg) {

    }

    public void close() {

    }

    private void checkConfig(TcpClientConfig config) throws Exception {

    }

    //SOCKET相关对象初始化
    private class TcpChannelHandler extends ChannelInitializer<SocketChannel> {

        private TcpClient _client;

        TcpChannelHandler(TcpClient client) {
            _client = client;
        }

        @Override
        public void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast(new ChannelHandler(_client));
        }
    }

    private class ChannelHandler extends ChannelInboundHandlerAdapter {

        private TcpClient _client;

        public ChannelHandler(TcpClient client) {
            _client = client;
        }
    }
}
