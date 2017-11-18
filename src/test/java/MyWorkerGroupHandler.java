import easy.thread.WorkerGroupHandler;
import io.netty.channel.ChannelHandlerContext;

public class MyWorkerGroupHandler implements WorkerGroupHandler {

    public void handleTask(Object taskObj) {
        MyWorkerGroupTask task = (MyWorkerGroupTask)taskObj;
        int value = task.buffer.getIntLE(8);
        //System.out.printf("MyWorkerGroupHandler ctx name:%s, value:%d \n", task.ctx.name(), value);

        task.buffer.setIntLE(8, value + 1);
        task.ctx.writeAndFlush(task.buffer);
    }

    public void handleException(Exception ex, Object taskObj) {
        ex.printStackTrace();
    }
}