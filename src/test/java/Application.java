import easy.net.TcpServer;
import easy.net.TcpServerConfig;
import easy.thread.WorkerGroup;
import easy.thread.WorkerGroupConfig;
public class Application {

    public static void main(String args[]) throws Exception {

        //工作线程
        WorkerGroupConfig workerConfig = new WorkerGroupConfig();
        workerConfig.handlerClass = MyWorkerGroupHandler.class;
        workerConfig.queueCapacity = 1000;
        workerConfig.workerCount = 5;
        WorkerGroup workerGroup = new WorkerGroup();
        workerGroup.run(workerConfig);

        //Net 服务
        TcpServerConfig serverConfig = new TcpServerConfig();
        serverConfig.port = 65002;
        serverConfig.refObj = workerGroup;
        serverConfig.handler = new MyNetServerHandler();
        TcpServer netServer = new TcpServer();
        netServer.run(serverConfig);

        System.out.printf("tcp server run");

        while (true) {
            Thread.sleep(1);
        }
    }
}

