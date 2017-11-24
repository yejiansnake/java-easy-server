package easy.net;

import easy.util.ChannelQueue;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.security.InvalidParameterException;
import java.util.concurrent.TimeUnit;

public class TcpClient {
    private TcpClientConfig _config;
    private Bootstrap _boot;
    private Channel _channel;
    private EventLoopGroup _workerGroup;
    private ChannelQueue<Object> _msgQueue;
    private boolean _stop = true;
    private Thread _activeThread = null;

    public TcpClient() {

    }

    public void start(TcpClientConfig config) throws Exception {
        synchronized (this) {
            if (!_stop) {
                throw new Exception("client has start!");
            }
        }

        this.checkConfig(config);
        _config = config;

        try {
            _msgQueue = new ChannelQueue<>(_config.sendMsgCount);
            _boot = new Bootstrap();
            _workerGroup = new NioEventLoopGroup();
            _boot.group(_workerGroup);

            _boot.channel(NioSocketChannel.class);

            _boot.handler(new TcpChannelHandler(this))
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_SNDBUF, 8 * 1024)     //发送和接收缓冲区默认8K
                    .option(ChannelOption.SO_RCVBUF, 8 * 1024);

            //系统级别的keepalive 不一定对方也打开，如果对方不开，就没用了
            if (_config.enabledSysKeepAlive) {
                _boot.option(ChannelOption.SO_KEEPALIVE, true);
            }

            _stop = false;

            _activeThread = new Thread(new ActiveThread(this));
            _activeThread.start();

        } catch (Exception ex){
            this.stop();
            throw ex;
        }
    }

    public void stop() throws Exception {
        synchronized (this) {
            if (_stop) {
                return;
            }
            _stop = true;
        }

        if (_activeThread != null) {
            _activeThread.join(3000);
            if (!_activeThread.isInterrupted()) {
                _activeThread.interrupt();
            }
        }

        if (_channel != null) {
            _channel.close().sync();
        }

        if (_workerGroup != null) {
            _workerGroup.shutdownGracefully().sync();
        }

        _activeThread = null;
        _workerGroup = null;
        _channel = null;
        _msgQueue.clear();
        _boot = null;
        _config = null;
    }

    public void send(Object msg) throws Exception {
        synchronized (this) {
            if (_stop) {
                return;
            }
        }
        _msgQueue.add(msg);
    }

    public void close() {
        _channel.close();
    }

    private void checkConfig(TcpClientConfig config) throws Exception {
        if (config == null) {
            throw new InvalidParameterException("config invalid");
        }

        if (config.host == null || config.host.trim().isEmpty()) {
            throw new InvalidParameterException("config.host invalid");
        }

        if (config.port <= 0 || config.port > 65535) {
            throw new InvalidParameterException("config.port invalid");
        }

        if (config.sendMsgCount <= 0) {
            throw new InvalidParameterException("config.sendMsgCount invalid");
        }

        if (config.recvBufferSize <= 0) {
            throw new InvalidParameterException("config.recvBufferSize invalid");
        }

        if (config.keepAliveSecond <= 0) {
            throw new InvalidParameterException("config.keepAliveSecond invalid");
        }

        if (config.handler == null) {
            throw new InvalidParameterException("config.handler invalid");
        }
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
        private ByteBuf _buffer;

        ChannelHandler(TcpClient client) {
            _client = client;
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) {
            _buffer = ctx.alloc().buffer(_client._config.recvBufferSize);
            //System.out.printf("client handlerAdded");
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) {
            if (_buffer != null) {
                _buffer.release();
                _buffer = null;
            }
            //System.out.printf("client handlerRemoved");
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            //连接成功
            //System.out.printf("client channelActive");
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
                if (byteSize >= _client._config.handler.getMsgSizeFieldByteCount()) {

                    //再判断获取到的数据否到达消息包体总大小
                    int msgSize = _client._config.handler.getMsgSize(_buffer);

                    if (byteSize >= msgSize) {
                        TcpClientMsgParam msgParam = new TcpClientMsgParam();
                        msgParam.buffer = _buffer.slice(0, msgSize);
                        msgParam.refObj = _config.refObj;

                        //处理消息包
                        _client._config.handler.handleMsg(msgParam);

                        //移动未完整的消息数据到包头
                        int tmpSize = byteSize - msgSize;
                        if (tmpSize > 0) {
                            ByteBuf buf = _buffer.slice(msgSize, tmpSize);
                            _buffer.readerIndex(0).writerIndex(0).writeBytes(buf);
                            againCheck = true;
                        } else {
                            _buffer.clear();
                        }
                    } else if (msgSize > _client._config.recvBufferSize) {
                        ctx.close();
                    }
                }
            } while (againCheck);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            // Close the connection when an exception is raised.
            cause.printStackTrace();
            ctx.close();
        }
    }

    private class ActiveThread implements Runnable {

        private TcpClient _client;

        ActiveThread(TcpClient client) {
            _client = client;
        }

        public void run() {
            while (!_client._stop) {
                try {
                    this.checkChannel();

                    //获取发送消息
                    Object msg = _client._msgQueue.poll(1, TimeUnit.SECONDS);

                    if (msg == null) {
                        continue;
                    }

                    _client._channel.writeAndFlush(msg);

                } catch (Exception ex) {
                    this.sleep();
                }
            }
        }

        private void checkChannel() throws Exception {
            try {
                if (_client._channel == null || !_client._channel.isOpen()) {
                    _client._channel = _client._boot.connect(_client._config.host,
                            _client._config.port).sync().channel();
                }

            } catch (Exception ex) {
                throw ex;
            }
        }

        private void sleep() {
            try {
                Thread.sleep(1);
            } catch (Exception ex) {
                System.out.printf("tcp client active thread %s", ex.getMessage());
            }
        }
    }
}
