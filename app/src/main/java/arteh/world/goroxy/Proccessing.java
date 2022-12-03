package arteh.world.goroxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Proccessing extends Thread {
    Socket browser_to_client;

    public Proccessing(Socket browser_to_client) {
        this.browser_to_client = browser_to_client;
    }

    public void run() {
        try {
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

            byte[] finali = message.getBytes(StandardCharsets.UTF_8);
            if (Config.encryption == 1)
                finali = Encryptor.encryptAES(finali, finali.length);

            InetAddress serverAddr = InetAddress.getByName(Config.server);
            SocketAddress socketaddres = new InetSocketAddress(serverAddr, Config.port);
            Socket client_to_proxy = new Socket();
            client_to_proxy.setReuseAddress(true);
            client_to_proxy.setSoTimeout(0);
            client_to_proxy.connect(socketaddres, 5000);

            if (Config.write_server) {
                InputStream input = client_to_proxy.getInputStream();

                byte[] size = new byte[4];
                byte[] btlength = new byte[4];
                int total, readed = 0;

                length = input.read(size);
                if (length == -1)
                    return;
                System.arraycopy(size, 0, btlength, 0, 4);
                total = ByteBuffer.wrap(btlength).getInt();

                while ((length = input.read(data, readed, total - readed)) != -1) {
                    readed = readed + length;
                    if (readed >= total)
                        break;
                }
            }

            OutputStream outputStream = client_to_proxy.getOutputStream();
            outputStream.write(Encryptor.intToArray(finali.length));
            outputStream.write(finali);
            outputStream.flush();

            new ReadFromProxy(client_to_proxy, browser_to_client).start();
            new WriteToProxy(client_to_proxy, browser_to_client).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
//                    System.out.println("LENGTH FROM BROWSER " + length);
                    processed = Encryptor.processToServer(data, length);
//                    System.out.println("AFTER PROCESS FROM BROWSER " + processed.length);
                    outputStream.write(Encryptor.intToArray(processed.length));
                    outputStream.write(processed, 0, processed.length);
                    outputStream.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
//                System.out.println("FINALLY FROM BROWSER FINISHED");
                try {
                    browser_to_client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    client_to_proxy.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

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

            int length;
            int total, readed;

            try {
                InputStream inputStream = client_to_proxy.getInputStream();
                OutputStream outputStream = browser_to_client.getOutputStream();

                while (true) {
                    readed = 0;

                    length = inputStream.read(size, 0, 4);
                    if (length == -1)
                        return;
                    total = ByteBuffer.wrap(size).getInt();
//                    System.out.println("TOTAL IS " + total);
                    while ((length = inputStream.read(data, readed, total - readed)) != -1) {
                        readed = readed + length;
                        System.out.println("READED IS " + readed);

                        if (readed >= total)
                            break;
                    }

//                    System.out.println("DATA BEFORE PROCEES TO BROWSER " + total);

                    processed = Encryptor.processToBrowsder(data, total);
//                    System.out.println("DATA AFTER PROCEES TO BROWSER " + processed.length);
                    outputStream.write(processed, 0, processed.length);
                    outputStream.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
//                System.out.println("FINALLY TO BROWSER FINISHED");
                try {
                    browser_to_client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    client_to_proxy.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}