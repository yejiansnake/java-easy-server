import easy.thread.WorkerGroupHandler;

public class MyWorkerGroupHandler implements WorkerGroupHandler {

    public void handleTask(Object taskObj) {
        MyWorkerGroupTask task = (MyWorkerGroupTask)taskObj;
        int value = task.buffer.getIntLE(8);
        //System.out.printf("MyWorkerGroupHandler channel name:%s, value:%d \n", task.channel.name(), value);

        task.buffer.setIntLE(8, value + 1);
        task.channel.writeAndFlush(task.buffer);
    }

    public void handleException(Exception ex, Object taskObj) {
        ex.printStackTrace();
    }
}