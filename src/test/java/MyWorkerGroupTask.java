import easy.thread.WorkerGroupHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledHeapByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class MyWorkerGroupTask {
    public ChannelHandlerContext ctx;
    public ByteBuf buffer;
}