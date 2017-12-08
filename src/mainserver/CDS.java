package mainserver;

public class CDS {
  public static int[] clients_status = { 0, 0 };
  public static String[] connected_clients_names = new String[2];
  public static Main_Panel GUI_editor;
  public static Connection_Manager conn;
  public static long[] last_ping_clients = { 0, 0 };
  public static String[] client1_map = { "", "", "", "" };
  public static String[] client2_map = { "", "", "", "" };
  public static char[][] map;
  public static char[][] map_secret;
  public static DB_Management dbCon = new DB_Management(); 
  public static int[] curr_x; //[0] - client1 || [1] - client2
  public static int[] curr_y;

  // ************************************************

  public static int[] random_different_numbers(int range) {
    int rand1 = 0;
    int rand2 = 0;
    while (rand1 == rand2) {
      rand1 = (int) (Math.random() * range);
      rand2 = (int) (Math.random() * range);
    }
    int[] retval = { rand1, rand2 };
    return retval;
  }

  public static String merge_sent_maps_secret() {
    String client1_str = client1_map[0] + client1_map[1] + client1_map[2] + client1_map[3];
    client1_str = reverse_string(client1_str);
    int med_count1 = 0;
    for (int i = 0; i < client1_str.length(); i++) {
      if (client1_str.charAt(i) == 'G') {
        med_count1++;
      }
    }
    int[] randoms = random_different_numbers(med_count1);
    int pass_count = 0;
    String secret_map = "";
    for (int i = 0; i < client1_str.length(); i++) {
      if (client1_str.charAt(i) == 'G' && pass_count == randoms[0]) {
        secret_map += "1"; // client 1 castle
        pass_count++;
      } else if (client1_str.charAt(i) == 'G' && pass_count == randoms[1]) {
        secret_map += "7"; // client 1 treasure
        pass_count++;
      } else {
        secret_map += client1_str.charAt(i);
        pass_count++;
      }
    }

    String client2_str = client2_map[0] + client2_map[1] + client2_map[2] + client2_map[3];
    int med_count2 = 0;
    for (int i = 0; i < client2_str.length(); i++) {
      if (client2_str.charAt(i) == 'G') {
        med_count2++;
      }
    }
    randoms = random_different_numbers(med_count2);
    pass_count = 0;
    for (int i = 0; i < client2_str.length(); i++) {
      if (client2_str.charAt(i) == 'G' && pass_count == randoms[0]) {
        secret_map += "2";// client 2 castle
        pass_count++;
      } else if (client2_str.charAt(i) == 'G' && pass_count == randoms[1]) {
        secret_map += "8";// client 2 treasure
        pass_count++;
      } else {
        secret_map += client2_str.charAt(i);
        pass_count++;
      }
    }
    Log.log(1, "Map succesfully merged");
    return secret_map;
  }

  public static String merge_sent_maps() {
    String client1_str = client1_map[0] + client1_map[1] + client1_map[2] + client1_map[3];
    client1_str = reverse_string(client1_str);
    String client2_str = client2_map[0] + client2_map[1] + client2_map[2] + client2_map[3];
    return client1_str + client2_str;
  }
  
  public static String reverse_string(String input) {
    return new StringBuilder(input).reverse().toString();
  }

  public static boolean is_map_full(int client_num) {
    if (client_num == 0) {
      for (int i = 0; i < client1_map.length; i++) {
        if (client1_map[i].equals("")) {
        	Log.log(1,"The map for client " + client_num + " is not full.");
        	updateDbScore(0);
          return false;
        }
      }
    } else if (client_num == 1) {
      for (int i = 0; i < client2_map.length; i++) {
        if (client2_map[i].equals("")) {
        	Log.log(1,"The map for client " + client_num + " is not full.");
          return false;
        }
      }
    }
    return true;
  }

  public static boolean check_sent_map_status() {
    boolean client1 = false;
    try {
      int row_count = 0;
      while (!CDS.client1_map[row_count].equals("")) {
        row_count++;
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      client1 = true;
      Log.log(1,"The map for client1 is complete.");
      System.out.println("yay we be good");
    }
    boolean client2 = false;
    try {
      int row_count = 0;
      while (!CDS.client2_map[row_count].equals("")) {
        row_count++;
      }
    } catch (ArrayIndexOutOfBoundsException e) {
    	Log.log(1,"The map for client2 is completed.");
      client2 = true;
    }

    return client1 && client2;
  }

  public static void setup_map() {
    String map_str = merge_sent_maps();
    String map_secret_str = merge_sent_maps_secret();

    // transform map_str to map char
    map = new char[8][8];
    int count = 0;
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        map[i][j] = map_str.charAt(count);
        count++;
      }
    }
    Log.log(1, "map transformed into char double array");
    // transform map_secret_str to map_secret char
    map_secret = new char[8][8];
    count = 0;
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        map_secret[i][j] = map_secret_str.charAt(count);
        count++;
      }
    }
    Log.log(1, "map transformed into char double array");
  }

  
  private static void updateDbScore(int loser_client) {
	  if(loser_client==0)
		  dbCon.insert_score(connected_clients_names[0], connected_clients_names[1], 0, 1);
	  if(loser_client==1)
		  dbCon.insert_score(connected_clients_names[0], connected_clients_names[1], 1, 0);
  }
  
  public static int check_move(int client_num, int x_new_pos, int y_new_pos) {
	  int status = -1; 
	  if((x_new_pos >= 8 || x_new_pos < 0) || (y_new_pos >=8 || y_new_pos < 0)) {
		  updateDbScore(client_num);
		  status=-1;
	  } //if the player goes overboard, we return failure state (-1)
	
	  if(client_num ==0 )
	  switch(map_secret[x_new_pos][y_new_pos])
	{
	 case 'G':
		 status = 2; //  is for valid movement
		 break;
	 case 'W':
		 status = -1; //  is for you lose
		 updateDbScore(0);
		 break;
	 case 'M':
		 status = 2;// valid but have to wait a turn, this should be implemented on the client side
		 break;
	 case '2':
		 status = 1; //found the castle, you win
		 updateDbScore(1);
		 break;
	 case '8':
		 status = 0; // found the treasure, valid movement
		 break; 		 
	}
	  
	  if(client_num == 1)
	  switch(map_secret[x_new_pos][y_new_pos])
	{
	 case 'G':
		 status = 2; //  is for valid movement
		 break;
	 case 'W':
		 status = -1; //  is for you lose
		 updateDbScore(1);
		 break;
	 case 'M':
		 status = 2;// valid but have to wait a turn, this should be implemented on the client side
		 break;
	 case '1':
		 status = 1; //found the castle, you win
		 updateDbScore(0);
		 break;
	 case '7':
		 status = 0; // found the treasure, valid movement
		 break; 		 
	}
	 return status;
    
  }
 
}
