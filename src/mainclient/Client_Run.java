package mainclient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Client_Run {
  private int status = 0;
  private InetAddress server_ip;
  private int server_port;
  private DatagramSocket client_socket;
  private boolean connected = false;
  private int id;
  private String name;
  private UUID uuid;

  public Client_Run() {
    try {
      server_ip = InetAddress.getByName("192.168.221.1");
    } catch (UnknownHostException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    while (true) {
      if (!connected) {
        request_to_connect();
        response_to_connect();
      }
    }

  }

  private void response_to_connect() {
    byte[] receiveData = new byte[512];
    DatagramPacket receive_packet = new DatagramPacket(receiveData, receiveData.length);
    try {
      client_socket.setSoTimeout(60000);
      client_socket.receive(receive_packet);
      String message_type = "";
      Map variables = parse_request_map(message_type, receive_packet);
      switch (message_type) {
      case "error":
        System.out.println(variables.get("message"));
        break;
      case "accept":
        id = (int) variables.get(id);
        uuid = (UUID) variables.get("UUID");
        status = (int) variables.get("status");
        connected = true;
        System.out.println("somehow it worked"); // need to add test cases
        break;
      default:
        System.out.println("broken");
      }
    } catch (SocketTimeoutException e1) {
      System.out.println("timed out");
    } catch (Exception e) {
      System.out.println("Couldn't receive");
      e.printStackTrace();
    }
  }

  public void request_to_connect() {
    try {
      client_socket = new DatagramSocket(server_port);
    } catch (SocketException e) {
    }
    String connection_str = "connect:name=filippo";
    send_packets(connection_str, server_ip);
  }

  private boolean send_packets(String packet_content, InetAddress ip_to) {
    byte[] send_data = packet_content.getBytes();
    DatagramPacket send_packet = new DatagramPacket(send_data, send_data.length, ip_to, 6789);
    try {
      client_socket.send(send_packet);
    } catch (IOException e) {
      return false;
    }
    return true;
  }

  private Map<String, String> parse_request_map(String message_type, DatagramPacket incoming) {
    // block : as inputs
    String[] vars_dirty = new String(incoming.getData()).split(":");
    String[] vars = Arrays.copyOfRange(vars_dirty, 1, vars_dirty.length);
    message_type = vars_dirty[0];
    Map<String, String> map_vars = new HashMap<String, String>();
    for (String var : vars) {
      // block = as inputs
      String[] temp = var.split("=");
      map_vars.put(temp[0], temp[1]);
      System.out.println(var);// debug
    }
    return map_vars;
  }

  public static void main(String[] args) {

  }
}
