package rocksaw;

import com.savarese.rocksaw.net.RawPacket;
import com.savarese.rocksaw.net.RawSocket;

import java.net.InetSocketAddress;

public class RawTest {
public static final int TIMEOUT = 30000;

  public static void main(String[] args) throws Exception {
    RawSocket socket = new RawSocket();
    try {
    String address = "10.200.10.1:50030";
    String srcAddress = "10.200.10.2";
    String[]addrArr = address.split(":");

    byte[] readBuffer = new byte[128];
    byte[] srcAddr = new byte[4];


    socket.open(RawSocket.PF_INET, 6); // iPV4 /etc/protocols

    System.out.println("connect success");

//    int ret = socket.read(readBuffer, srcAddr);
//    System.out.println("connect ack:" + new String(readBuffer) + " from " + new String(srcAddr));

    InetSocketAddress destAddrObj = new InetSocketAddress(addrArr[0], Integer.parseInt(addrArr[1]));


      socket.setSendTimeout(TIMEOUT);
      socket.setReceiveTimeout(TIMEOUT);
      byte[] data = "echo".getBytes();
      byte[]sendBuffer = new byte[data.length + 40];
      RawPacket rawPacket = new RawPacket("10.10.43.68", 12345, "10.200.10.1", 50030, data);
//      byte[] sendBuffer = new byte[52];
//      rawPacket.setData(sendBuffer);
//
//      // src address dest address protocl(6)
//      InetAddress srcAddrObj = InetAddress.getByName("10.10.43.68");
//      System.out.println("src:" + srcAddrObj.getHostAddress());
//      byte[] srcAddrbytes = srcAddrObj.getAddress();
//      System.arraycopy(srcAddrbytes, 0, sendBuffer, 12, 4);
//
//
//      byte[] destAddrbytes = destAddrObj.getAddress().getAddress();
//      System.arraycopy(destAddrbytes, 0, sendBuffer, 16, 4);
//
//      rawPacket.setProtocol(6);
//      rawPacket.setIPHeaderLength(5);
//
//      rawPacket.setTCPHeaderLength(5); // 这里的header 都是 *4 得到真实长度
//      rawPacket.setSourcePort(12345);
//      rawPacket.setDestinationPort(destAddrObj.getPort());
//      rawPacket.setTCPDataByteLength(0);
//      System.out.println(String.format("IP packet Length: %02x%02x", sendBuffer[2], sendBuffer[3]));
//      rawPacket.setIPPacketLength(rawPacket.getIPHeaderLength() * 4 + rawPacket.getTCPHeaderLength() * 4 + rawPacket.getTCPDataByteLength());
//
//      System.out.println(String.format("IP packet Length: %02x%02x", sendBuffer[2], sendBuffer[3]));
//
//
//      int offset = rawPacket.getIPHeaderByteLength();
//      int maxOffset = offset + 16;
//      StringBuffer sb = new StringBuffer();
//      for (int i = offset; i < maxOffset; i++) {
//        sb.append(String.format("%02x", sendBuffer[i]));
//        if (i % 2 == 0)sb.append(" ");
//      }
//      System.out.println(sb);

//      rawPacket.setData(new byte[0]);

    // 这里要发送的是 ipheader之外的数据
      rawPacket.getData(sendBuffer);
      socket.write(destAddrObj.getAddress(), sendBuffer, 20, 20 + data.length);
      System.out.println("send data:");
      socket.read(readBuffer, srcAddr);
      System.out.println(new String(readBuffer) + " from " + new String(srcAddr));
    } catch (Exception se) {
      se.printStackTrace();
      socket.setUseSelectTimeout(true);
      socket.setSendTimeout(TIMEOUT);
      socket.setReceiveTimeout(TIMEOUT);
    }
  }
}
