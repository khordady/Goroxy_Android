package arteh.world.goroxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ReadFromProxy extends Thread {
    Socket client_to_proxy, browser_to_client;

    public ReadFromProxy(Socket client_to_proxy, Socket browser_to_client) {
        this.client_to_proxy = client_to_proxy;
        this.browser_to_client = browser_to_client;
    }

    public void run() {
        byte[] data = new byte[32*1024];
        int length;

        try {
            InputStream inputStream = client_to_proxy.getInputStream();
            OutputStream outputStream = browser_to_client.getOutputStream();

            while ((length = inputStream.read(data)) != -1) {
                outputStream.write(data, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
