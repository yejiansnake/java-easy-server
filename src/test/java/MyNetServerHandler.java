import easy.net.TcpServerHandler;
import easy.thread.WorkerGroup;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class MyNetServerHandler implements TcpServerHandler {
    public int getMsgSizeFieldByteCount() {
        return Integer.BYTES;
    }

    public int getMsgSize(ByteBuf buffer) {
        int tmp = buffer.getIntLE(0);
        //System.out.printf("MyNetServerHandler getMsgSize buffer size: %d \n", tmp);
        return tmp;
    }

    public void handleMsg(ChannelHandlerContext ctx, ByteBuf buffer, Object refObj) {
        //System.out.printf("MyNetServerHandler ctx name:%s, buffer size: %d \n", ctx.name(), buffer.readableBytes());
        WorkerGroup workerGroup = (WorkerGroup)refObj;

        try {
            MyWorkerGroupTask task = new MyWorkerGroupTask();
            task.ctx = ctx;
            task.buffer = buffer.copy();
            workerGroup.addTask(task);
        } catch (Exception ex){

        }
    }
}