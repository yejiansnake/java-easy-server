package client;

import easy.net.TcpClient;
import easy.net.TcpClientConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class TestClient {

    public static void main(String args[]) throws Exception {

        TcpClientConfig config = new TcpClientConfig();
        config.host = "127.0.0.1";
        config.port = 65002;
        config.handler = new MyTcpClientHandler();
        TcpClient netInstance = new TcpClient();
        netInstance.start(config);

        System.out.printf("tcp client start");

        int count = 1;
        while (true) {
            ByteBuf buffer = Unpooled.buffer();
            buffer.writeInt(12);
            buffer.writeInt(0);
            buffer.writeInt(count);
            count++;
            netInstance.send(buffer);

            if (count == 1000) {
                netInstance.close();
                count = 1;
            }

            Thread.sleep(1);
        }
    }
}