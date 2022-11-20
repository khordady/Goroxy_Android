package arteh.world.goroxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

public class ListenSocket extends Thread {
    ServerSocket serverSocket;
    Listener listener;

    public ListenSocket(Listener listener) {
        this.listener = listener;
        Encryptor.initialize();
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
            while (true) {
                Socket browser_to_client = serverSocket.accept();
                listener.Connected();
                browser_to_client.setReuseAddress(true);
                browser_to_client.setSoTimeout(0);

                byte[] data = new byte[9 * 1024];

                StringBuilder builder = new StringBuilder(300);
                InputStream inputStream = browser_to_client.getInputStream();
                int length;

                while ((length = inputStream.read(data)) != -1) {
                    String a = new String(data, 0, length, StandardCharsets.UTF_8);
                    builder = builder.append(a);
                    if (builder.toString().endsWith("\r\n\r\n")) break;
                }
                String message = "";
                if (Config.authentication)
                    message = Config.username + "," + Config.password + "\r\n";
                message = message + builder;

                System.out.println("MESSAGE ISSSSSSSSSS: " + message);

                byte[] finali = message.getBytes(StandardCharsets.UTF_8);
                if (Config.encryption == 1)
                    finali = Encryptor.encryptAES(finali, finali.length);

                InetAddress serverAddr = InetAddress.getByName(Config.server);
                SocketAddress socketaddres = new InetSocketAddress(serverAddr, Config.port);
                Socket client_to_proxy = new Socket();
                client_to_proxy.setReuseAddress(true);
                client_to_proxy.setSoTimeout(0);
                client_to_proxy.connect(socketaddres, 5000);

                OutputStream outputStream = client_to_proxy.getOutputStream();
                outputStream.write(Encryptor.intToArray(finali.length));
                outputStream.write(finali);
                outputStream.flush();

                new ReadFromProxy(client_to_proxy, browser_to_client).start();
                new WriteToProxy(client_to_proxy, browser_to_client).start();
            }
        } catch (Exception e) {
            listener.failed(e.getMessage());
            e.printStackTrace();
        }
    }

}