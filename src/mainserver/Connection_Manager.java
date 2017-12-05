package mainserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import mainclient.Client_Run;

public class Connection_Manager extends Thread {
  public InetAddress[] clients_IPs = new InetAddress[2];
  private UUID[] clients_UUID = new UUID[2];
  private DatagramSocket server_socket;

  public Connection_Manager() {
    this.start();
  }

  public void run() {
    try {
      server_socket = new DatagramSocket(9876);
      byte[] receiveData = new byte[512];
      while (true) {
        // check incoming requests
        DatagramPacket receive_packet = new DatagramPacket(receiveData, receiveData.length);
        server_socket.receive(receive_packet);
        InetAddress request_IP = receive_packet.getAddress();
        // block : as inputs
        // String[] variables_dirty = new
        // String(receive_packet.getData()).split(":");
        Map map_vars = parse_request_map(receive_packet);
        String command_type = command_parse(receive_packet);
        switch (command_type) {
        case "connect":
          connection_request(map_vars, request_IP);
          break;
        case "ping":
          ping_check(map_vars, request_IP);
          break;
        case "playakk":
          ask_map(map_vars, request_IP);
          break;
        case "mapakk":
          add_map(map_vars, request_IP);
          break;
        default:
          System.out.println("filtered wrong request");
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void add_map(Map vars, InetAddress request_IP) {
    UUID UUID_temp = UUID.fromString((String) vars.get("UUID"));
    int row_sent = Integer.parseInt((String) vars.get("row"));
    String row_data = (String) vars.get("data");
    int client_num = -1;
    boolean error = false;
    if (UUID_temp.equals(clients_UUID[0])) {
      client_num = 0;
    } else if (UUID_temp.equals(clients_UUID[1])) {
      client_num = 1;
    } else {
      error = true;
    }

    int row_count = 0;
    if (client_num == 0) {
      CDS.client1_map[row_sent] = row_data;
    } else if (client_num == 1) {
      CDS.client2_map[row_sent] = row_data;
    }
    CDS.last_ping_clients[client_num] = System.currentTimeMillis();
    if (!CDS.is_map_full(client_num)) {
      ask_map(vars, request_IP);
    }
  }

  private void ask_map(Map vars, InetAddress request_IP) {
    UUID UUID_temp = UUID.fromString((String) vars.get("UUID"));
    int client_num = -1;
    boolean error = false;
    if (UUID_temp.equals(clients_UUID[0])) {
      client_num = 0;
    } else if (UUID_temp.equals(clients_UUID[1])) {
      client_num = 1;
    } else {
      error = true;
    }

    int row_count = 0;
    try {
      if (client_num == 0) {
        while (!CDS.client1_map[row_count].equals("")) {
          row_count++;
        }
      } else if (client_num == 1) {
        while (!CDS.client2_map[row_count].equals("")) {
          row_count++;
        }
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      if (CDS.check_map_status()) {
        CDS.setup_map();
      }
    }

    if (!error) {
      String[] keys = { "UUID", "row" };
      String[] values = { clients_UUID[client_num].toString(), row_count + "" };
      byte[] ask_map_byte = create_packet_bytes("mapreq", keys, values);
      send_packets(ask_map_byte, request_IP);
    } else {
      send_error(client_num, "The UUID is not correct and you are suspicious");
    }
  }

  public void connection_request(Map vars, InetAddress client_ip) {
    // check if we have enought clients
    if (IntStream.of(CDS.clients_status).anyMatch(x -> x == 0)) {
      String client_name = (String) vars.get("name");
      // assign it a client space
      int client_num;
      if (CDS.clients_status[0] == 0) {
        client_num = 0;
      } else {
        client_num = 1;
      }
      UUID new_client_UUID = UUID.randomUUID();
      clients_UUID[client_num] = new_client_UUID;
      CDS.connected_clients_names[client_num] = client_name;
      clients_IPs[client_num] = client_ip;
      CDS.clients_status[client_num] = 1;
      String[] keys = { "id", "name", "UUID", "status" };
      String[] values = { Integer.toString(client_num), client_name, new_client_UUID.toString(), "1" };
      byte[] send_packet = create_packet_bytes("accept", keys, values);
      send_packets(send_packet, client_ip);
      Log.log(1, "client named: " + client_name + " has connected");
    } else {
      Log.log(0, "Too many users tried to play");
      // already too many users connected
    }
  }

  public boolean send_packets(byte[] packet_content, InetAddress ip_to) {
    byte[] send_data = packet_content;
    DatagramPacket send_packet = new DatagramPacket(send_data, send_data.length, ip_to, 9875);
    try {
      server_socket.send(send_packet);
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

  private void ping_check(Map vars, InetAddress client_ip) {
    UUID UUID_temp = UUID.fromString((String) vars.get("UUID"));
    int client_num = -1;
    boolean error = false;
    if (UUID_temp.equals(clients_UUID[0])) {
      client_num = 0;
    } else if (UUID_temp.equals(clients_UUID[1])) {
      client_num = 1;
    } else {
      error = true;
    }

    if (!error) {
      if (CDS.clients_status[client_num] == Integer.parseInt((String) vars.get("status"))) {
        if (Integer.parseInt((String) vars.get("status")) == 1) {
          CDS.clients_status[client_num] = 2;
        }
        send_ping(client_num);
      } else {
        send_error(client_num, "The ping request status is wrong");
      }
    } else {
      send_error(client_num, "The ping request UUID is wrong");
    }
    CDS.last_ping_clients[client_num] = System.currentTimeMillis();
  }

  private void send_error(int client_num, String error_message) {
    String command_word = "error";
    String[] keys = { "message" };
    String[] values = { error_message };
    byte[] send_error_data = create_packet_bytes(command_word, keys, values);
    send_packets(send_error_data, clients_IPs[client_num]);
  }

  private void send_ping(int client_num) {
    String command_word = "pingback";
    String[] keys = { "UUID", "status" };
    String[] values = { clients_UUID[client_num].toString(), Integer.toString(CDS.clients_status[client_num]) };
    byte[] send_ping_data = create_packet_bytes(command_word, keys, values);
    send_packets(send_ping_data, clients_IPs[client_num]);
  }

  public byte[] create_packet_bytes(String command_word, String[] keys, String[] values) {
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

  public String command_parse(DatagramPacket incoming) {
    String[] vars_dirty = new String(incoming.getData()).split(":");
    return vars_dirty[0];
  }

  public void start_game() {
    String[] keys = { "UUID", "status" };
    String[] values = { clients_UUID[0].toString(), "3" };
    byte[] packets_start = create_packet_bytes("playstart", keys, values);
    send_packets(packets_start, clients_IPs[0]);
    Log.log(1, "Sent packets to start the game");
    // for (int i = 0; i < 2; i++) {
    // String[] keys = { "UUID", "status" };
    // String[] values = { clients_UUID[i].toString(), "3" };
    // byte[] packets_start = create_packet_bytes("playstart", keys, values);
    // send_packets(packets_start, clients_IPs[i]);
    // Log.log(1, "Sent packets to start the game");
    // }
  }
}
