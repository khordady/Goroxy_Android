package arteh.world.goroxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ReadFromProxy extends Thread {
    Socket client_to_proxy, browser_to_client;

    public ReadFromProxy(Socket client_to_proxy, Socket browser_to_client) {
        this.client_to_proxy = client_to_proxy;
        this.browser_to_client = browser_to_client;
    }

    public void run() {
        byte[] data = new byte[32 * 1024];
        byte[] processed;
        byte[] size = new byte[4];
        byte[] btlength = new byte[4];

        int length;
        int total, readed = 0;

        try {
            InputStream inputStream = client_to_proxy.getInputStream();
            OutputStream outputStream = browser_to_client.getOutputStream();

            while (true) {
                length = inputStream.read(size);
                if (length == -1)
                    return;
                System.arraycopy(size, 0, btlength, 0, 4);
                total = ByteBuffer.wrap(btlength).getInt();

                while ((length = inputStream.read(data, readed, total - readed)) != -1) {
                    readed = readed + length;
                    if (readed >= total)
                        break;
                }

                processed = Encryptor.processToBrowsder(data, total);
                outputStream.write(processed, 0, processed.length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
