package server;

import easy.net.TcpServerHandler;
import easy.net.TcpServerMsgParam;
import easy.thread.WorkerGroup;
import io.netty.buffer.ByteBuf;

public class MyNetServerHandler implements TcpServerHandler {
    public int getMsgSizeFieldByteCount() {
        return Integer.BYTES;
    }

    public int getMsgSize(ByteBuf buffer) {
        int tmp = buffer.getInt(0);
        //System.out.printf("server.MyNetServerHandler getMsgSize buffer size: %d \n", tmp);
        return tmp;
    }

    public void handleMsg(TcpServerMsgParam param) {
        //System.out.printf("server.MyNetServerHandler channel name:%s, buffer size: %d \n", channel.name(), buffer.readableBytes());
        WorkerGroup workerGroup = (WorkerGroup)param.refObj;

        try {
            MyWorkerGroupTask task = new MyWorkerGroupTask();
            task.channel = param.channel;
            task.buffer = param.buffer.copy();
            workerGroup.addTask(task);
        } catch (Exception ex){

        }
    }
}