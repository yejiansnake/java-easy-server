package server;

import easy.thread.WorkerGroupHandler;

public class MyWorkerGroupHandler implements WorkerGroupHandler {

    public void handleTask(Object taskObj) {
        MyWorkerGroupTask task = (MyWorkerGroupTask)taskObj;
        int value = task.buffer.getInt(8);
        System.out.printf("handle task on channel name:%s, value:%d \n", task.channel.id().asLongText(), value);

        task.buffer.setInt(8, value + 1);
        task.channel.writeAndFlush(task.buffer);
    }

    public void handleException(Exception ex, Object taskObj) {
        ex.printStackTrace();
    }
}