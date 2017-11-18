package easy.net;

import easy.net.factory.ChannelFactory;
import easy.net.factory.FactoryCreator;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;

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
    public void run(NetServerConfig config) throws Exception {
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

            _server.childHandler(new ServerChannel(this))
                .option(ChannelOption.SO_BACKLOG, _config.backLog)
                .childOption(ChannelOption.SO_SNDBUF, 8 * 1024)     //发送和接收缓冲区默认8K
                .childOption(ChannelOption.SO_RCVBUF, 8 * 1024);

            //系统级别的keepalive 不一定对方也打开，如果对方不开，就没用了
            if (_config.enabledSysKeepAlive) {
                _server.childOption(ChannelOption.SO_KEEPALIVE, true);
            }

            // 绑定端口并启动接收
            _channelFuture = _server.bind(_config.port).sync(); // (7)
        } catch (Exception ex){
            this.close();
            throw ex;
        }
    }

    public void close() throws Exception {
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

        private NetServer _server;

        ServerChannel(NetServer server) {
            _server = server;
        }

        @Override
        public void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast(new ChannelHandler(_server));
        }
    }

    //数据收发处理回调
    private class ChannelHandler extends ChannelInboundHandlerAdapter {

        private NetServer _server;
        private ByteBuf _buffer;

        ChannelHandler(NetServer server) {
            _server = server;
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) {
            //System.out.printf("handlerAdded ctx name:%s", ctx.name());
            _buffer = ctx.alloc().buffer(_server._config.recvBufferSize);
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) {
            _buffer.release();
            _buffer = null;
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

                        ByteBuf msgBuf = _buffer.slice(0, msgSize);

                        //处理消息包
                        _server._config.handler.handleMsg(ctx, msgBuf, _config.refObj);

                        //移动未完整的消息数据到包头
                        int tmpSize = byteSize - msgSize;
                        if (tmpSize > 0) {
                            ByteBuf buf = _buffer.slice(msgSize, tmpSize);
                            _buffer.readerIndex(0).writerIndex(0).writeBytes(buf);
                            againCheck = true;
                        } else {
                            _buffer.clear();
                        }
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
