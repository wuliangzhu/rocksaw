package com.savarese.rocksaw.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * 可以控制台输入的方式进行交互
 * 1  command + \r\n;
 *    ack: \r\n
 * 2  end check: 输入，触发输入read，endCheck，进入等待，防止你的输入无效，并没有触发ack，所以不能进行锁机制，最好是循环机制，目前测试如果输入不能处理，是不会进行ack的
 * 3  connect sendCommand close
 */
public class ConsoleSocket {
  public static final int TIMEOUT = 3000;
  private static Logger logger = LoggerFactory.getLogger(ConsoleSocket.class);
  private String host;
  private int port;

  private Socket socket;
  // 进行命令输出
  private OutputStream outputStream;
  private BufferedReader inputReader;
  private Predicate<String> endCheck;
  private Predicate<String> errorCheck;

  public ConsoleSocket(String host, int port) {
    this.host = host;
    this.port = port;
  }


  public void setEndCheck(Predicate<String> check) {
    this.endCheck = check;
  }

  public void setErrorCheck(Predicate<String> check) {
    this.errorCheck = check;
  }

  /**
   * 如果连接成功返回true，否则返回false
   *
   * @return
   */
  public synchronized boolean connect() {
    logger.info("start connect {}:{}", host, port);


    try {
      socket = new Socket();
      socket.setReuseAddress(true);
      socket.setSoLinger(true, 1);
      socket.setKeepAlive(true);
      socket.setSoTimeout(TIMEOUT);

      InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);

      socket.connect(inetSocketAddress, TIMEOUT);

      this.outputStream = socket.getOutputStream();

      InputStream inputStream = socket.getInputStream();
      this.inputReader = new BufferedReader(new InputStreamReader(inputStream));

      // 如果endcheck 为null，则用默认的
      if (this.endCheck == null) {
        this.endCheck = new DefaultEndPredict();
      }
      if (this.errorCheck == null) {
        this.errorCheck = new DefaultErrorPredict();
      }


      logger.info("connect {}:{} success", host, port);
    } catch (IOException e) {
      logger.error("connect {}:{} error:{}", host, port, e.getMessage());

      return false;
    }

    return true;
  }

  /**
   * 进行命令下发
   * @param command
   * @throws IOException
   */
  public synchronized Response sendCommand(String command, boolean autoConnect) throws IOException {
    logger.info("send command {} to {}", command, host);

    if (socket == null || socket.isClosed()) {
      if (autoConnect) {
        if (!this.connect()) throw new RuntimeException("the host " + host + ":" + port + " refuse connect");
      }else {
        throw new RuntimeException("please connect the server first");
      }
    }

    outputStream.write((command + "\r\n").getBytes());

    Response response = readResponse();
    response.req = command;

    logger.info("req {} ack {}", command, response.ack);

    return response;
  }

  public Response sendCommand(String command) throws IOException {
    return sendCommand(command, true);
  }

  /**
   * 进行资源释放
   */
  public void close() {
    try {
      logger.info("the socket will close");
      this.outputStream.close();
      this.inputReader.close();
      this.socket.close();

      logger.info("the socket is closed disconnect from {}:{}", host,port);
    } catch (IOException e) {
      e.printStackTrace();
      logger.error("close the socket error:{}", e.getMessage());
    }
  }

  private Response readResponse() {
    Response response = new Response();
    try {
      String line = this.inputReader.readLine();
      while (line != null) {
        if (endCheck.test(line)) break;
        if (errorCheck.test(line)) {
          response.status = 1;
          response.message = "the command handle error. check the command and the server";
          break;
        }

        // 判断空值
        line = line.trim();
        if (line.length() > 0) {
          response.ack.add(line);
        }

        line = this.inputReader.readLine();
      }
    }catch (Exception e) {
      response.status = 1;
      response.message = e.getMessage();
    }

    return response;
  }


  /**
   * 对返回的数据进行封装，便于逻辑根据req进行处理
   */
  static public class Response{
    public Response() {
      ack = new LinkedList<>();
      status = 0; // success
      message = "success";
    }

    String req; // 请求的字符串
    public List<String> ack; // 响应的数据列表
    public int status; // 处理结果
    public String message; // 错误信息
  }

  static class DefaultEndPredict implements Predicate<String> {
    @Override
    public boolean test(String s) {
      return "! end".equalsIgnoreCase(s);
    }
  }

  static class DefaultErrorPredict implements Predicate<String> {
    @Override
    public boolean test(String s) {
      return "! error".equalsIgnoreCase(s);
    }
  }
}
