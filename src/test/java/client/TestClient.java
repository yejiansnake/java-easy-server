package client;

import easy.net.TcpClient;
import easy.net.TcpClientConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class TestClient {

    public static void main(String args[]) throws Exception {

        final int clientCount = 100;

        TcpClient[] clients = new TcpClient[clientCount];

        for (int index = 0; index < clientCount; index++) {
            clients[index] = TestClient.runTest();
        }

        int count = 1;
        while (true) {
            for (int index = 0; index < clientCount; index++) {
                ByteBuf buffer = Unpooled.buffer();
                buffer.writeInt(12);
                buffer.writeInt(0);
                buffer.writeInt(count);
                clients[index].send(buffer);
                Thread.sleep(1);
            }

            count++;

//            if (count == 1000) {
//                for (int index = 0; index < clientCount; index++) {
//                    clients[index].close();
//                }
//                count = 1;
//            }
        }
    }

    private static TcpClient runTest() throws Exception {
        TcpClientConfig config = new TcpClientConfig();
        config.host = "127.0.0.1";
        config.port = 65002;
        config.handler = new MyTcpClientHandler();
        TcpClient netInstance = new TcpClient();
        netInstance.start(config);
        return netInstance;
    }
}