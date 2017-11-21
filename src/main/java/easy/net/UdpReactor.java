package easy.net;

import easy.net.factory.ChannelFactory;
import easy.net.factory.FactoryCreator;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;

import java.net.InetSocketAddress;
import java.security.InvalidParameterException;

public class UdpReactor {

    private UdpReactorConfig _config;
    private Bootstrap _server;
    private Channel _channel;
    private ChannelFuture _channelFuture;
    private EventLoopGroup _workerGroup;

    public UdpReactor() {

    }

    //启动
    public void start(UdpReactorConfig config) throws Exception {
        this.checkConfig(config);
        _config = config;

        easy.net.factory.ChannelFactory factory = this.getChannelFactory();

        //工作线程
        _workerGroup = factory.createEventLoopGroup(_config.recvThreadCount);

        //配置server 并启动
        try {
            _server = new Bootstrap();
            _server.group(_workerGroup);

            _server.channel(factory.getDatagramChannelClass());

            _server.handler(new ServerChannel(this))
                .option(ChannelOption.SO_SNDBUF, 8 * 1024)
                .option(ChannelOption.SO_RCVBUF, 8 * 1024);

            if (_config.broadcast) {
                _server.option(ChannelOption.SO_BROADCAST,true);
            }

            // 绑定端口并启动接收

            if (_config.port > 0) {
                _channelFuture = _server.bind(_config.port);
            } else {
                _channelFuture = _server.bind();
            }

            _channelFuture.sync();
            _channel = _channelFuture.channel();

        } catch (Exception ex) {
            this.stop();
            throw ex;
        }
    }

    public void stop() throws Exception {
        if (_channel != null) {
            _channel.closeFuture().sync();
        }

        //释放线程
        if (_workerGroup != null) {
            _workerGroup.shutdownGracefully();
        }

        _channel = null;
        _channelFuture = null;
        _server = null;
        _workerGroup = null;
        _config = null;
    }

    public void send(byte[] buffer, InetSocketAddress address) {
        this.send(Unpooled.copiedBuffer(buffer), address);
    }

    public void send(ByteBuf buffer, InetSocketAddress address) {
        this.send(new DatagramPacket(buffer, address));
    }

    public void send(DatagramPacket udpPacket) {
        if (_channel != null) {
            _channel.writeAndFlush(udpPacket);
        }
    }

    private void checkConfig(UdpReactorConfig config) throws Exception {
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

        if (config.recvThreadCount <= 0) {
            throw new InvalidParameterException("config.recvThreadCount invalid");
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
    private class ServerChannel extends ChannelInitializer<SocketChannel> {

        private UdpReactor _server;

        ServerChannel(UdpReactor server) {
            _server = server;
        }

        @Override
        public void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast(new ChannelHandler(_server));
        }
    }

    //数据收发处理回调
    private class ChannelHandler extends SimpleChannelInboundHandler<DatagramPacket> {

        private UdpReactor _server;

        ChannelHandler(UdpReactor server) {
            _server = server;
        }

        @Override
        public void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
            ByteBuf buffer = msg.content();

            int startIndex = 0;
            boolean againCheck = false;
            do {
                againCheck = false;
                int byteSize = buffer.readableBytes();

                //先判断是否能获取到消息头部的包整体信息
                if (byteSize >= _server._config.handler.getMsgSizeFieldByteCount()) {

                    //再判断获取到的数据否到达消息包体总大小
                    int msgSize = _server._config.handler.getMsgSize(buffer);

                    if (byteSize >= msgSize) {
                        UdpReactorMsgParam msgParam = new UdpReactorMsgParam();
                        msgParam.channel = ctx.channel();
                        msgParam.buffer = buffer.slice(startIndex, msgSize);
                        msgParam.address = msg.sender();
                        msgParam.refObj = _config.refObj;

                        //处理消息包
                        _server._config.handler.handleMsg(msgParam);
                        buffer.readerIndex(startIndex + msgSize);
                        startIndex += msgSize;

                        //移动未完整的消息数据到包头
                        int tmpSize = byteSize - msgSize;
                        if (tmpSize >= msgSize) {
                            againCheck = true;
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
