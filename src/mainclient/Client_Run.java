package mainclient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Random;
import javax.swing.JApplet;
import javax.swing.JFrame;

public class Client_Run {
  static int status = 0;
  private InetAddress server_ip;
  private int server_port = 9876;
  private DatagramSocket client_socket;
  private int id;
  private String name = "Test_Client";
  private UUID uuid;
  public static String[] client_map = { "0000", "0000", "0000", "0000" };
  public static boolean gen_map = false;

  public Client_Run() {
    try {
      server_ip = InetAddress.getByName("127.0.0.1");
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
    while (true) {
      if (status < 1) {
        request_to_connect();
        response_to_connect();
        if (status == 1) {
          ping_send();
          ping_receive();
        }
      } else if (status == 3) {
        wait_command_play();
      } else {
        ping_send();
        ping_receive();
        wait_command_idle();
      }
    }
  }

  private void wait_command_play() {
    byte[] receiveData = new byte[512];
    DatagramPacket receive_packet = new DatagramPacket(receiveData, receiveData.length);
    try {
      client_socket.setSoTimeout(10000); // 10 seconds
      client_socket.receive(receive_packet);
      String command_type = command_parse(receive_packet);
      Map variables = parse_request_map(receive_packet);
      switch (command_type) {
      case "error":
        Log.log(0, "Received error from server: " + variables.get("message"));
        break;
      case "mapreq":
        if (!gen_map) {
          split_map();
          gen_map = true;
        }
        send_map_response(variables);
        break;
      default:
        Log.log(0, "Sent Request was malformed");
      }
    } catch (SocketTimeoutException e1) {
      // nothing happened
      Log.log(0, "Sent Request and didn't get any response while waiting to play");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void send_map_response(Map variables) {
    if (((String) variables.get("UUID")).equals(uuid.toString())) {
      int which_row = Integer.parseInt((String) variables.get("row"));
      String[] keys = { "UUID", "row", "data" };
      String[] values = { uuid.toString(), which_row + "", client_map[which_row] };
      byte[] map_response_byte = create_packet_string("mapakk", keys, values);
      send_packets(map_response_byte);
    } else {
      Log.log(0, "Server didn't send correct UUID, intruder in the system");
    }
  }

  // water w
  // meddow g
  // mountain m
  private char random_char(ArrayList<Character> r) {
    int list_len = r.size();
    Random rand = new Random();
    int rand_char = rand.nextInt(list_len);
    return r.get(rand_char);
  }

  // water w
  // meddow g
  // mountain m
  private String generate_map() {
    String retval = "";
    int available_w = 6;
    int available_g = 6;
    int available_m = 4;
    ArrayList<Character> round_char = new ArrayList<Character>();
    for (int i = 0; i < 16; i++) {
      round_char = new ArrayList<Character>();
      if (available_w != 0) {
        round_char.add('w');
      }
      if (available_g != 0) {
        round_char.add('g');
      }
      if (available_m != 0) {
        round_char.add('m');
      }
      char char_selected = random_char(round_char);
      retval += char_selected;
      switch (char_selected) {
      case 'w':
        available_w--;
        break;
      case 'g':
        available_g--;
        break;
      case 'm':
        available_m--;
      }
    }
    return retval;
  }

  private String[] split_map() {
    String to_be_split = generate_map();
    String[] map_split = new String[4];
    map_split[0] = to_be_split.substring(0, 4);
    map_split[1] = to_be_split.substring(3, 7);
    map_split[2] = to_be_split.substring(7, 11);
    map_split[3] = to_be_split.substring(11, 15);
    client_map = map_split;
    return client_map;
  }

  private void wait_command_idle() {
    byte[] receiveData = new byte[512];
    DatagramPacket receive_packet = new DatagramPacket(receiveData, receiveData.length);
    try {
      client_socket.setSoTimeout(10000); // 10 seconds
      client_socket.receive(receive_packet);
      String command_type = command_parse(receive_packet);
      Map variables = parse_request_map(receive_packet);
      switch (command_type) {
      case "error":
        Log.log(0, "Received error from server: " + variables.get("message"));
        break;
      case "playstart":
        check_play_status(variables);
        break;
      default:
        Log.log(0, "Sent Request was malformed");
      }
    } catch (SocketTimeoutException e1) {
      // nothing happened
      Log.log(0, "Sent Request and didn't get any response while waiting");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void response_to_connect() {
    byte[] receiveData = new byte[512];
    DatagramPacket receive_packet = new DatagramPacket(receiveData, receiveData.length);
    try {
      client_socket.setSoTimeout(5000);
      client_socket.receive(receive_packet);
      String command_type = command_parse(receive_packet);
      Map variables = parse_request_map(receive_packet);
      switch (command_type) {
      case "error":
        Log.log(0, "Received error from server: " + variables.get("message"));
        break;
      case "accept":
        if (((String) variables.get("name")).equals(name)) {
          id = Integer.parseInt((String) variables.get("id"));
          uuid = UUID.fromString((String) variables.get("UUID"));
          status = Integer.parseInt((String) variables.get("status"));
        } else {
          Log.log(1, "Responce has the wrong username, restart connect");
        }
        break;
      default:
        Log.log(0, "Incorrect message from server received");
      }
    } catch (SocketTimeoutException e1) {
      Log.log(0, "Sent Request and didn't get any response, while waiting for connection");
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
    send_packets(send_packet);
  }

  private boolean send_packets(byte[] packet_content) {
    byte[] send_data = packet_content;
    DatagramPacket send_packet = new DatagramPacket(send_data, send_data.length, server_ip, server_port);
    try {
      client_socket.send(send_packet);
    } catch (IOException e) {
      return false;
    }
    return true;
  }

  private void check_play_status(Map variables) {
    if (((String) variables.get("UUID")).equals(uuid.toString())) {
      status = Integer.parseInt((String) variables.get("status"));
      String[] keys = { "UUID", "status" };
      String[] values = { uuid.toString(), Integer.toString(status) };
      byte[] send_play_akk = create_packet_string("playakk", keys, values);
      send_packets(send_play_akk);
      status = 3;
      // start map making and the AI
    } else {
      Log.log(0, "Server didn't send correct UUID, intruder in the system");
    }
  }

  private Map<String, String> parse_request_map(DatagramPacket incoming) {
    String[] vars_dirty = new String(incoming.getData()).split(":");
    Map<String, String> map_vars = new HashMap<String, String>();
    for (int i = 1; i < (vars_dirty.length - 1); i++) {
      String[] temp = vars_dirty[i].split("=");
      map_vars.put(temp[0], temp[1]);
    }
    return map_vars;
  }

  private String command_parse(DatagramPacket incoming) {
    String[] vars_dirty = new String(incoming.getData()).split(":");
    return vars_dirty[0];
  }

  private byte[] create_packet_string(String command_word, String[] keys, String[] values) {
    if (keys.length != values.length) {
      Log.log(1, "The the command: " + command_word + " the keys and values don't match");
      throw new ArrayIndexOutOfBoundsException();
    } else {
      String packet_string = command_word + ":";
      for (int j = 0; j < keys.length; j++) {
        packet_string += keys[j] + "=" + values[j] + ":";
      }
      Log.log(1, "Sending: " + packet_string);

      return packet_string.getBytes();
    }
  }

  private void ping_send() {
    try {
      String command_word = "ping";
      String[] keys = { "UUID", "status" };
      String[] values = { uuid.toString(), Integer.toString(status) };
      byte[] send_ping_data = create_packet_string(command_word, keys, values);
      send_packets(send_ping_data);
      Log.log(1, "Sent ping");
    } catch (NullPointerException e) {
      Log.log(0, "Sent ping request malformed");
    }

  }

  private void ping_receive() {
    byte[] receiveData = new byte[512];
    DatagramPacket receive_packet = new DatagramPacket(receiveData, receiveData.length);
    try {
      client_socket.setSoTimeout(5000); // 5 seconds
      client_socket.receive(receive_packet);
      String command_type = command_parse(receive_packet);
      Map variables = parse_request_map(receive_packet);
      switch (command_type) {
      case "error":
        Log.log(0, "Error from server: " + variables.get("message"));
        break;
      case "pingback":
        status = Integer.parseInt((String) variables.get("status"));
        Log.log(1, "Received ping-back");
        break;
      default:
        Log.log(0, "Incorrect message from server received");
      }
    } catch (SocketTimeoutException e1) {
      // nothing happened
      Log.log(0, "Sent Request and didn't get any response, while waiting for connection");
      status = 0;
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame("Client Game");
    JApplet applet = new JAppletControl();
    frame.add(applet);
    frame.pack();
    frame.setVisible(true);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    Client_Run client = new Client_Run();
  }
}
