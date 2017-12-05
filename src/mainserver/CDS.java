package mainserver;

public class CDS {
  public static int[] clients_status = { 0, 0 };
  public static String[] connected_clients_names = new String[2];
  public static Main_Panel GUI_editor;
  public static Connection_Manager conn;
  public static long[] last_ping_clients = { 0, 0 };
  public static String[] client1_map = { "", "", "", "" };
  public static String[] client2_map = { "", "", "", "" };

  public static boolean is_map_full(int client_num) {
    if (client_num == 0) {
      for (int i = 0; i < client1_map.length; i++) {
        if (client1_map[i].equals("")) {
          return false;
        }
      }
    } else if (client_num == 1) {
      for (int i = 0; i < client2_map.length; i++) {
        if (client2_map[i].equals("")) {
          return false;
        }
      }
    }
    return true;
  }

  public static boolean check_map_status() {
    boolean client1 = false;
    try {
      int row_count = 0;
      while (!CDS.client1_map[row_count].equals("")) {
        row_count++;
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      client1 = true;
      System.out.println("yay we be good");
    }
    boolean client2 = false;
    try {
      int row_count = 0;
      while (!CDS.client2_map[row_count].equals("")) {
        row_count++;
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      client2 = true;
    }

    return client1 && client2;
  }

  public static void setup_map() {

  }
}
