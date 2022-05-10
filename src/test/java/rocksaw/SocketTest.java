package rocksaw;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class SocketTest {
public static final int TIMEOUT = 30000;

  public static void main(String[] args) throws Exception {
    String host = "10.200.10.1";
    int port = 50030;

    SocketAddress socketAddress = new InetSocketAddress(host, port);

    Socket socket = new Socket();

    try {
      socket.connect(socketAddress);
      InputStream inputStream = socket.getInputStream();
      OutputStream outputStream = socket.getOutputStream();
      Thread readThread = new Thread(new Runnable() {
        @Override
        public void run() {
          BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
          String line = null;
          try {
            while (true) {
              if (inputStream.available() > 0) {
                while ((line = bufferedReader.readLine()) != null) {
                  System.out.println("ack:" + line);
                }
              }else {
                Thread.sleep(100);
              }
            }
          }catch (Exception e){
            e.printStackTrace();
          }finally {
            try {
              bufferedReader.close();
            }catch ( Exception ex){
              ex.printStackTrace();
            }
          }



        }
      });
      readThread.setDaemon(true);
      readThread.start();


      Thread writerThread = new Thread(new Runnable() {
        @Override
        public void run() {
          BufferedReader bufferedReader1 = new BufferedReader(new InputStreamReader(System.in));
          while (true) {
            String line = null;
            try {
              line = bufferedReader1.readLine();
              System.out.println("your input is " + line);
              outputStream.write((line + "\r\n").getBytes() );
              outputStream.flush();

            } catch (IOException e) {
              e.printStackTrace();
            }

          }
        }
      });
      writerThread.start();

      while (true) {
        Thread.sleep(1000);
      }

//      socket.close();
    }catch (Exception e) {
      e.printStackTrace();
    }
  }
}
