import easy.net.NetServer;
import easy.net.NetServerConfig;
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
        NetServerConfig serverConfig = new NetServerConfig();
        serverConfig.port = 65002;
        serverConfig.refObj = workerGroup;
        serverConfig.handler = new MyNetServerHandler();
        NetServer netServer = new NetServer();
        netServer.run(serverConfig);

        System.out.printf("tcp server run");

        while (true) {
            Thread.sleep(1);
        }
    }
}

