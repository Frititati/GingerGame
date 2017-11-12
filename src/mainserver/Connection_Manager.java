package mainserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

public class Connection_Manager {
  private int[] clients_status = { 0, 0 };
  private InetAddress[] clients_IPs = new InetAddress[2];
  private UUID[] clients_UUID = new UUID[2];
  private String[] connected_clients_names = new String[2];
  private DatagramSocket server_socket;

  public Connection_Manager() throws IOException {
    server_socket = new DatagramSocket(9876);
    byte[] receiveData = new byte[512];
    while (true) {
      // check incoming requests
      DatagramPacket receive_packet = new DatagramPacket(receiveData, receiveData.length);
      server_socket.receive(receive_packet);
      InetAddress request_IP = receive_packet.getAddress();
      // block : as inputs
      String[] variables_dirty = new String(receive_packet.getData()).split(":");
      Map map_vars = parse_request_map(receive_packet);
      switch (variables_dirty[0]) {
      case "connect":
        connection_request(map_vars, request_IP);
        break;
      case "ping":
        ping_check(map_vars, request_IP);
        break;
      }

      // if (!active_connections.contains(request_IP)) {
      // active_connections.add(request_IP);
      // }

    }
  }

  public void connection_request(Map vars, InetAddress client_ip) {
    // check if we have enought clients
    if (IntStream.of(clients_status).anyMatch(x -> x == 0)) {
      String client_name = (String) vars.get("name");
      // assign it a client space
      int client_num;
      if (clients_status[0] == 0) {
        client_num = 0;
      } else {
        client_num = 1;
      }
      UUID new_client_UUID = UUID.randomUUID();
      clients_UUID[client_num] = new_client_UUID;
      connected_clients_names[client_num] = client_name;
      clients_IPs[client_num] = client_ip;
      clients_status[client_num] = 1;
      String acceptance_response = "accept:id=" + client_num + ":name=" + client_name + ":UUID=" + new_client_UUID.toString() + ":status=1";
      send_packets(acceptance_response, client_ip);
    } else {
      // already too many users connected
    }
  }

  public boolean send_packets(String packet_content, InetAddress ip_to) {
    byte[] send_data = packet_content.getBytes();
    DatagramPacket send_packet = new DatagramPacket(send_data, send_data.length, ip_to, 6789);
    try {
      server_socket.send(send_packet);
    } catch (IOException e) {
      return false;
    }
    return true;
  }

  public Map<String, String> parse_request_map(DatagramPacket incoming) {
    // block : as inputs
    String[] vars_dirty = new String(incoming.getData()).split(":");
    String[] vars = Arrays.copyOfRange(vars_dirty, 1, vars_dirty.length);
    Map<String, String> map_vars = new HashMap<String, String>();
    for (String var : vars) {
      // block = as inputs
      String[] temp = var.split("=");
      map_vars.put(temp[0], temp[1]);
      System.out.println(var);// debug
    }
    return map_vars;
  }

  public void ping_check(Map vars, InetAddress client_ip) {
    String UUID_str = (String) vars.get("UUID");
    UUID UUID_temp = UUID.fromString(UUID_str);
    int client_num = -1;
    boolean error = false;
    if (UUID_temp.equals(clients_UUID[0])) {
      client_num = 0;
    } else if (UUID_temp.equals(clients_UUID[1])) {
      client_num = 1;
    } else {
      error = true;
    }

    String message = "";
    if (!error) {
      if (clients_status[client_num] == (int) vars.get("status")) {
        message = "ping:UUID=" + UUID_str + ":status=" + clients_status[client_num];
      } else {
        message = "error:message=The ping request status is wrong";
      }
    } else {
      message = "error:message=The ping request UUID is wrong";
    }
    send_packets(message, client_ip);
  }
}
