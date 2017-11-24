package server;

import easy.net.TcpServer;
import easy.net.TcpServerConfig;
import easy.thread.WorkerGroup;
import easy.thread.WorkerGroupConfig;
public class TestServer {

    public static void main(String args[]) throws Exception {

        //工作线程
        WorkerGroupConfig workerConfig = new WorkerGroupConfig();
        workerConfig.handlerClass = MyWorkerGroupHandler.class;
        workerConfig.queueCapacity = 1000;
        workerConfig.workerCount = 5;
        WorkerGroup workerGroup = new WorkerGroup();
        workerGroup.start(workerConfig);

        //Net 服务
        TcpServerConfig serverConfig = new TcpServerConfig();
        serverConfig.port = 65002;
        serverConfig.refObj = workerGroup;
        serverConfig.handler = new MyNetServerHandler();
        TcpServer netServer = new TcpServer();
        netServer.start(serverConfig);

        System.out.printf("tcp server start");

        while (true) {
            Thread.sleep(1);
        }
    }
}

