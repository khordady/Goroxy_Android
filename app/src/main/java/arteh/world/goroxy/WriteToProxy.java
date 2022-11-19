package arteh.world.goroxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class WriteToProxy extends Thread {
    Socket client_to_proxy, browser_to_client;

    public WriteToProxy(Socket client_to_proxy, Socket browser_to_client) {
        this.client_to_proxy = client_to_proxy;
        this.browser_to_client = browser_to_client;
    }

    public void run() {
        byte[] data = new byte[32 * 1024];
        byte[] processed;
        int length;

        try {
            InputStream inputStream = browser_to_client.getInputStream();
            OutputStream outputStream = client_to_proxy.getOutputStream();

            while ((length = inputStream.read(data)) != -1) {
                processed = Encryptor.processToServer(data, length);
                outputStream.write(Encryptor.intToArray(processed.length));
                outputStream.write(processed, 0, processed.length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
