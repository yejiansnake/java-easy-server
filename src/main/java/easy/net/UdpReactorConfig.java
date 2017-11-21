package easy.net;

public class UdpReactorConfig {
    //端口号（0 - 65535）
    public int port = 0;
    //I/O 核心 (nio epoll)
    public String core = "nio";
    //I/O 核心模型(epoll 有 EPOLLET 与 EPOLLET 两种模型)
    //public String mode = "";
    //消息体限制（默认 1KB）
    public boolean broadcast = false;
    public int recvBufferSize = 1024;
    //接收数据 - 工作线程数
    public int recvThreadCount = 1;
    public UdpReactorHandler handler = null;
    //传递对象（在回调的时候可以引用）
    public Object refObj = null;
}