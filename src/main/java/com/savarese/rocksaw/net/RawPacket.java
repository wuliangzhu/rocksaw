package com.savarese.rocksaw.net;

import org.savarese.vserv.tcpip.TCPPacket;

import java.net.InetAddress;

public class RawPacket extends TCPPacket {
  /**
   * Creates a new IPPacket of a given size.
   * check 需要 src dest 地址 协议  ip packet length，外加 tcp header 所以所有这些都是为了计算出src
   *
   * ip 数据包，和 一般的socket一样，差别只在于没有端口，所以他能收到所有到这个IP的数据包，协议就是要字节定义，但这里如果定义了tcp，那就只能用tcp来处理
   * @param size The number of bytes in the packet.
   */
  public RawPacket(String src, int srcPort, String dest, int destPort, byte[] data) {
    super(data.length);
    this._data_ = new byte[data.length + 40]; // ip header tcp header

    byte[] sendBuffer = this._data_;

    try {
      InetAddress srcAddrObj = InetAddress.getByName(src);

      byte[] srcAddrbytes = srcAddrObj.getAddress();
      System.arraycopy(srcAddrbytes, 0, sendBuffer, 12, 4);

      InetAddress destAddrObj = InetAddress.getByName(dest);
      byte[] destAddrbytes = destAddrObj.getAddress();
      System.arraycopy(destAddrbytes, 0, sendBuffer, 16, 4);

      this.setProtocol(6);
      this.setIPHeaderLength(5);

      this.setTCPHeaderLength(5); // 这里的header 都是 *4 得到真实长度
      this.setSourcePort(srcPort);
      this.setDestinationPort(destPort);
      this.setTCPDataByteLength(data.length);

      System.arraycopy(data, 0, sendBuffer, 40, data.length);

      this.computeTCPChecksum();
    }catch (Exception e) {
      e.printStackTrace();
    }
  }
}
