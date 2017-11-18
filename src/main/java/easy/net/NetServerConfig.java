package easy.net;

public class NetServerConfig {
    //端口号（0 - 65535）
    public int port = 0;
    //I/O 核心 (nio epoll)
    public String core = "nio";
    //I/O 核心模型(epoll 有 EPOLLET 与 EPOLLET 两种模型)
    public String mode = "";
    //消息体限制（TCP 默认20KB, UDP 默认 1KB）
    public int recvBufferSize = 20 * 1024;
    //TCP accept 线程数
    public int acceptThreadCount = 1;
    //接收数据 - 工作线程数
    public int recvThreadCount = 5;
    //最大连接数
    public int clientLimit = 1024;
    //是否启用核心上的心跳机制（client也需要打开，否则强制 client 断开）
    public boolean enabledSysKeepAlive = false;
    //TCP 心跳超时检测时间（秒）
    public int keepAliveSecond = 30;
    //TCP socket accept 接收队列中能保存多少未来得及处理的连接
    public int backLog = 128;
    //处理步骤的回调
    public NetServerHandler handler = null;
    //传递对象（在回调的时候可以引用）
    public Object refObj = null;
}