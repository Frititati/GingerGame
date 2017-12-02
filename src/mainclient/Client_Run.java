package mainclient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Client_Run {
  private int status = 0;
  private InetAddress server_ip;
  private int server_port = 9876;
  private DatagramSocket client_socket;
  private boolean connected = false;
  private int id;
  private String name = "filippo";
  private UUID uuid;

  public Client_Run() {
    try {
      server_ip = InetAddress.getByName("127.0.0.1");
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
    while (true) {
      if (!connected) {
        request_to_connect();
        response_to_connect();

        send_ping();
        receive_ping();
      } else {
        wait_for_command();
        send_ping();
        receive_ping();
      }
    }
  }

  private void wait_for_command() {
    byte[] receiveData = new byte[512];
    DatagramPacket receive_packet = new DatagramPacket(receiveData, receiveData.length);
    try {
      client_socket.setSoTimeout(10000); // 30 seconds
      client_socket.receive(receive_packet);
      String command_type = command_parse(receive_packet);
      Map variables = parse_request_map(receive_packet);
      switch (command_type) {
      case "error":
        System.out.println(variables.get("message"));
        break;
      case "startplay":
        System.out.println("yay");
        break;
      default:
        System.out.println("bad request");
      }
    } catch (SocketTimeoutException e1) {
      // nothing happened
      System.out.println("didn't receive anything in the wait");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void response_to_connect() {
    byte[] receiveData = new byte[512];
    DatagramPacket receive_packet = new DatagramPacket(receiveData, receiveData.length);
    try {
      client_socket.receive(receive_packet);
      String command_type = command_parse(receive_packet);
      Map variables = parse_request_map(receive_packet);
      switch (command_type) {
      case "error":
        System.out.println(variables.get("message"));
        break;
      case "accept":
        if (((String) variables.get("name")).equals(name)) {
          id = Integer.parseInt((String) variables.get("id"));
          uuid = UUID.fromString((String) variables.get("UUID"));
          status = Integer.parseInt((String) variables.get("status"));
          connected = true;
        } else {
          System.out.println("Wrong username back, restart connect");
        }
        break;
      default:
        System.out.println("not error or accept");
      }
    } catch (SocketTimeoutException e1) {
      System.out.println("timed out");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void request_to_connect() {
    try {
      client_socket = new DatagramSocket(9875);
    } catch (SocketException e) {
    }
    String[] keys = { "name" };
    String[] values = { name };
    byte[] send_packet = create_packet_string("connect", keys, values);
    send_packets(send_packet, server_ip);
  }

  private boolean send_packets(byte[] packet_content, InetAddress ip_to) {
    byte[] send_data = packet_content;
    DatagramPacket send_packet = new DatagramPacket(send_data, send_data.length, ip_to, server_port);
    try {
      client_socket.send(send_packet);
    } catch (IOException e) {
      return false;
    }
    return true;
  }

  private Map<String, String> parse_request_map(DatagramPacket incoming) {
    // block : as inputs
    String[] vars_dirty = new String(incoming.getData()).split(":");
    // String[] vars = Arrays.copyOfRange(vars_dirty, 1, vars_dirty.length);
    Map<String, String> map_vars = new HashMap<String, String>();
    for (int i = 1; i < (vars_dirty.length - 1); i++) {
      // block = as inputs
      String[] temp = vars_dirty[i].split("=");
      map_vars.put(temp[0], temp[1]);
      // System.out.println("Packet Content " + temp[1]);
    }
    return map_vars;
  }

  private String command_parse(DatagramPacket incoming) {
    String[] vars_dirty = new String(incoming.getData()).split(":");
    return vars_dirty[0];
  }

  private byte[] create_packet_string(String command_word, String[] keys, String[] values) {
    if (keys.length != values.length) {
      System.out.println("The the command: " + command_word + " the keys and values don't match");
      throw new ArrayIndexOutOfBoundsException();
    } else {
      String packet_string = command_word + ":";
      for (int j = 0; j < keys.length; j++) {
        packet_string += keys[j] + "=" + values[j] + ":";
      }
      // System.out.println(packet_string);

      return packet_string.getBytes();
    }
  }

  private void send_ping() {
    String command_word = "ping";
    String[] keys = { "UUID", "status" };
    String[] values = { uuid.toString(), Integer.toString(status) };
    byte[] send_ping_data = create_packet_string(command_word, keys, values);
    send_packets(send_ping_data, server_ip);
    System.out.println("sent ping");
  }

  private void receive_ping() {
    byte[] receiveData = new byte[512];
    DatagramPacket receive_packet = new DatagramPacket(receiveData, receiveData.length);
    try {
      client_socket.setSoTimeout(5000); // 5 seconds
      client_socket.receive(receive_packet);
      String command_type = command_parse(receive_packet);
      Map variables = parse_request_map(receive_packet);
      switch (command_type) {
      case "error":
        System.out.println(variables.get("message"));
        break;
      case "pingback":
        status = Integer.parseInt((String) variables.get("status"));
        System.out.println("received ping");
        break;
      default:
        System.out.println("not error or ping");
      }
    } catch (SocketTimeoutException e1) {
      // nothing happened
      System.out.println("didn't receive anything back");
      status = 0;
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    Client_Run client = new Client_Run();
  }

}
