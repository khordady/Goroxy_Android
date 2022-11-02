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

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class ListenSocket extends Thread {
    ServerSocket serverSocket;
    Listener listener;
    Cipher cipher;

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
            while (true) {
                Socket browser_to_client = serverSocket.accept();
                browser_to_client.setReuseAddress(true);
                browser_to_client.setSoTimeout(0);

                byte[] data = new byte[512];

                StringBuilder builder = new StringBuilder(100);
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

                byte[] finali = message.getBytes(StandardCharsets.UTF_8);

                if (Config.encryption == 1)
                    finali = encryptMethod(message.substring(0, message.length() - (message.length() % Config.encryption_key.length())).getBytes(StandardCharsets.UTF_8));

                InetAddress serverAddr = InetAddress.getByName(Config.server);
                SocketAddress socketaddres = new InetSocketAddress(serverAddr, Config.port);
                Socket client_to_proxy = new Socket();
                client_to_proxy.setReuseAddress(true);
                client_to_proxy.setSoTimeout(0);
                client_to_proxy.connect(socketaddres, 5000);

                OutputStream outputStream = client_to_proxy.getOutputStream();
                outputStream.write(finali);
                outputStream.flush();

                new ReadFromProxy(client_to_proxy, browser_to_client).start();
                new WriteToProxy(client_to_proxy, browser_to_client).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] encryptMethod(byte[] message) {
        try {
            if (cipher == null) {
                SecretKeySpec keySpec = new SecretKeySpec(Config.encryption_key.getBytes(StandardCharsets.UTF_8), "AES");
                cipher = Cipher.getInstance("AES/ECB/NoPadding");
                cipher.init(1, keySpec);
            }
            return cipher.doFinal(message);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}