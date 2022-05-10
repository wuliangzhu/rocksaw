package rocksaw;


import com.savarese.rocksaw.net.ConsoleSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleSocketTest {

    public static void main(String[] args) {
      ConsoleSocket consoleSocket = new ConsoleSocket("10.200.10.1", 50030);
      try {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
          String line = bufferedReader.readLine();
          if (line != null) {
            ConsoleSocket.Response response = consoleSocket.sendCommand(line);

            if (line.equalsIgnoreCase("close")) {
              consoleSocket.close();
            }

            System.out.println(response.ack + " " + response.message);
          }

          System.out.println("line:" + line);
        }

      } catch (IOException e) {
        e.printStackTrace();
      }
    }
}
