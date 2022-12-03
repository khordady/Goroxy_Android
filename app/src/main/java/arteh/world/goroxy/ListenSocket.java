package arteh.world.goroxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ListenSocket extends Thread {
    ServerSocket serverSocket;
    Listener listener;

    public ListenSocket(Listener listener) {
        this.listener = listener;
    }

    public void close() {
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(2022);
            serverSocket.setReuseAddress(true);
            serverSocket.setSoTimeout(0);
            listener.Connected();

            while (true) {
                Socket browser_to_client = serverSocket.accept();
                new Proccessing(browser_to_client).start();
            }
        } catch (Exception e) {
            listener.failed(e.getMessage());
            e.printStackTrace();
        }
    }
}