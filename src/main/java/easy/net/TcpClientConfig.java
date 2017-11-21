package easy.net;

public class TcpClientConfig {
    //地址或者IP
    public String host = "";
    //端口号（0 - 65535）
    public int port = 0;
    //I/O 核心 (nio epoll)
    public int recvBufferSize = 20 * 1024;
    //是否启用核心上的心跳机制（client也需要打开，否则强制 client 断开）
    public boolean enabledSysKeepAlive = false;
    //TCP 心跳包发送时间间隔（秒）
    public int keepAliveSecond = 30;
    //处理步骤的回调
    public TcpClientHandler handler = null;
    //传递对象（在回调的时候可以引用）
    public Object refObj = null;
}
