package easy.net;

import easy.net.factory.ChannelFactory;
import easy.net.factory.FactoryCreator;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

public class TcpServer {

    private TcpServerConfig _config;
    private ServerBootstrap _server;
    private ChannelFuture _channelFuture;
    private EventLoopGroup _workerGroup;
    private EventLoopGroup _acceptGroup;
    private int _clientCount = 0;
    private static final long MAX_CHANNEL_INDEX = 1;
    private long _channelIndex = MAX_CHANNEL_INDEX;
    private Map<Long, Channel> _channelMap = new HashMap<>();

    public TcpServer() {

    }

    //启动
    public void start(TcpServerConfig config) throws Exception {
        this.checkConfig(config);
        _config = config;

        ChannelFactory factory = this.getChannelFactory();

        //工作线程
        _acceptGroup = factory.createEventLoopGroup(_config.acceptThreadCount);
        _workerGroup = factory.createEventLoopGroup(_config.recvThreadCount);

        //配置server 并启动
        try {
            _server = new ServerBootstrap();
            _server.group(_acceptGroup, _workerGroup);

            _server.channel(factory.getServerSocketChannelClass());

            _server.childHandler(new ServerChannelHandler(this))
                .option(ChannelOption.SO_BACKLOG, _config.backLog)
                .childOption(ChannelOption.SO_SNDBUF, 8 * 1024)     //发送和接收缓冲区默认8K
                .childOption(ChannelOption.SO_RCVBUF, 8 * 1024);

            //系统级别的keepalive 不一定对方也打开，如果对方不开，就没用了
            if (_config.enabledSysKeepAlive) {
                _server.childOption(ChannelOption.SO_KEEPALIVE, true);
            }

            if (_config.keepAliveSecond > 0) {
                _server.childOption(ChannelOption.SO_TIMEOUT, _config.keepAliveSecond);
            }

            // 绑定端口并启动接收
            _channelFuture = _server.bind(_config.port).sync();

        } catch (Exception ex){
            this.stop();
            throw ex;
        }
    }

    public void stop() throws Exception {
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

        _channelFuture = null;
        _server = null;
        _workerGroup = null;
        _acceptGroup = null;
        _config = null;

        _channelMap.clear();
        _channelIndex = MAX_CHANNEL_INDEX;
        _clientCount = 0;
    }

    public int clientCount() {
        synchronized (this) {
            return _clientCount;
        }
    }

    public void sendChannel(long channelID, Object msg) {
        Channel channel = this.getSocketChannel(channelID);
        if (channel != null) {
            channel.writeAndFlush(msg);
        }
    }

    public void closeChannel(long channelID) {
        Channel channel = this.getSocketChannel(channelID);
        if (channel != null) {
            channel.close();
        }
    }

    private Channel getSocketChannel(long channelID) {
        Channel channel = null;
        synchronized (this) {
            if (_channelMap.containsKey(channelID)) {
                channel = _channelMap.get(channelID);
            }
        }

        return channel;
    }

    private void checkConfig(TcpServerConfig config) throws Exception {
        if (config == null) {
            throw new InvalidParameterException("config invalid");
        }

        if (config.port < 0 || config.port > 65535) {
            throw new InvalidParameterException("config.port invalid");
        }

        if (!config.core.equals("nio") && !config.core.equals("epoll")) {
            throw new InvalidParameterException("config.core invalid");
        }

        if (config.recvBufferSize <= 0) {
            throw new InvalidParameterException("config.recvBufferSize invalid");
        }

        if (config.acceptThreadCount <= 0) {
            throw new InvalidParameterException("config.acceptThreadCount invalid");
        }

        if (config.recvThreadCount <= 0) {
            throw new InvalidParameterException("config.recvThreadCount invalid");
        }

        if (config.clientLimit <= 0) {
            throw new InvalidParameterException("config.clientLimit invalid");
        }

        if (config.backLog <= 0) {
            throw new InvalidParameterException("config.backLog invalid");
        }

        if (config.handler == null) {
            throw new InvalidParameterException("config.handler invalid");
        }
    }

    public ChannelFactory getChannelFactory() throws Exception {
        if (_config.core.equals("nio")) {
            return FactoryCreator.getInstance().getNioChannelFactory();
        } else if (_config.core.equals("epoll")) {
            return FactoryCreator.getInstance().getEpollChannelFactory();
        }

        throw new InvalidParameterException(String.format("core:%s invalid",  _config.core));
    }

    //SOCKET相关对象初始化
    private class ServerChannelHandler extends ChannelInitializer<SocketChannel> {

        private TcpServer _server;

        ServerChannelHandler(TcpServer server) {
            _server = server;
        }

        @Override
        public void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast(new ChannelHandler(_server));
        }
    }

    //数据收发处理回调
    private class ChannelHandler extends ChannelInboundHandlerAdapter {

        private TcpServer _server;
        private ByteBuf _buffer;
        private long _channelID;

        ChannelHandler(TcpServer server) {
            _server = server;
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) {
            synchronized (_server) {
                _server._clientCount++;

                if (_server._clientCount > _server._config.clientLimit) {
                    ctx.close();
                    return;
                }

                if (_server._channelIndex == Long.MAX_VALUE) {
                    _server._channelIndex = TcpServer.MAX_CHANNEL_INDEX;
                }

                _channelID = _server._channelIndex++;
                _server._channelMap.put(_channelID, ctx.channel());
            }

            //System.out.printf("handlerAdded channel name:%s", channel.name());
            _buffer = ctx.alloc().buffer(_server._config.recvBufferSize);
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) {
            synchronized (_server) {
                _server._clientCount--;
                if (_channelID > 0) {
                    _server._channelMap.remove(_channelID);
                }
            }

            if (_buffer != null) {
                _buffer.release();
                _buffer = null;
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf tmpBuf = (ByteBuf) msg;
            _buffer.writeBytes(tmpBuf);
            tmpBuf.release();

            boolean againCheck = false;

            do {
                againCheck = false;
                int byteSize = _buffer.readableBytes();

                //先判断是否能获取到消息头部的包整体信息
                if (byteSize >= _server._config.handler.getMsgSizeFieldByteCount()) {

                    //再判断获取到的数据否到达消息包体总大小
                    int msgSize = _server._config.handler.getMsgSize(_buffer);

                    if (byteSize >= msgSize) {
                        TcpServerMsgParam msgParam = new TcpServerMsgParam();
                        msgParam.id = _channelID;
                        msgParam.channel = ctx.channel();
                        msgParam.buffer = _buffer.slice(0, msgSize);
                        msgParam.refObj = _config.refObj;

                        //处理消息包
                        _server._config.handler.handleMsg(msgParam);

                        //移动未完整的消息数据到包头
                        int tmpSize = byteSize - msgSize;
                        if (tmpSize > 0) {
                            ByteBuf buf = _buffer.slice(msgSize, tmpSize);
                            _buffer.readerIndex(0).writerIndex(0).writeBytes(buf);
                            againCheck = true;
                        } else {
                            _buffer.clear();
                        }
                    } else if (msgSize > _server._config.recvBufferSize) {
                        ctx.close();
                    }
                }
            } while (againCheck);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
