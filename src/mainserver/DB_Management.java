package mainserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

public class DB_Management {
  private PreparedStatement prepared_sql;
  private String db_host = Property_File.get_property("db_host");
  private String db_port = Property_File.get_property("db_port");
  private String db_name = Property_File.get_property("db_name");
  private String db_uname = Property_File.get_property("db_uname");
  private String db_passwd = Property_File.get_property("db_passwd");
  private Connection db_connection = null;
  public DB_Management() {
    try {
      Class.forName("org.postgresql.Driver");
      db_connection = DriverManager.getConnection("jdbc:postgresql://"+db_host+":"+db_port+"/"+db_name, db_uname, db_passwd);
    } catch (ClassNotFoundException e){
      Log.log(0, "Database driver not found.");
      System.exit(19);
    } catch (SQLException e) {
      Log.log(0, "Connection to database "+db_name+" on "+db_host+" on port "+db_port+" failed. Check host and credentials.");
      System.exit(20);
    }
    System.out.println("Opened database successfully");
    Log.log(1, "Database succesfully connected");
  }
  
  public boolean insert_clientname(String name){
	try {
	  //Statement db_statement = db_connection.createStatement();
	  String sql = "INSERT INTO client_names(name) VALUES ("+name+")";
 
	  prepared_sql = db_connection.prepareStatement(sql);
	  prepared_sql.executeUpdate();
	  db_connection.commit();
	  
	} catch (SQLException e) {
		
	  return false;
	}
	return true;
  }
  
  
  public boolean insert_score(String player_0, String player_1, int score_0, int score_1){
	Timestamp timestamp = new Timestamp(System.currentTimeMillis());
	try {
	  //Statement db_statement = db_connection.createStatement();
	  String sql = "INSERT INTO match_history(player_0, player_1, score_0, score_1, timestamp) VALUES ("+player_0+", "+player_1+","+score_0+","+score_1+","+timestamp+")";
	  Log.log(0, "timestamp is: " + timestamp);
	  prepared_sql = db_connection.prepareStatement(sql);
	  prepared_sql.executeUpdate();
	  db_connection.commit();
	  
	} catch (SQLException e) {
	  Log.log(0, "Insert query to database "+db_name+" on "+db_host+" on port "+db_port+" failed."
	  		+ "\nVar dump:"
	  		+ "\nplayer_0: "+player_0
	  		+ "\nplayer_1: "+player_1
	  		+ "\nscore_0: "+score_0
	  		+ "\nscore_1: "+score_1
	  		+ "\ntimestamp: "+timestamp.getTime()+" (parsed: "+timestamp.toString()+")");
	  return false;
	}
	return true;
  }
   
  //method insert the movement on each client for each turn
  public boolean insert_action(int client_id, int pos_x, int pos_y, int turn){
	try {
	  //Statement db_statement = db_connection.createStatement();
	  String sql = "INSERT INTO match_moves(client_id, pos_x, pos_y, turn) VALUES ("+client_id+", "+pos_x+","+pos_y+","+turn+")";
	  prepared_sql = db_connection.prepareStatement(sql);
	  prepared_sql.executeUpdate();
	  db_connection.commit();
	  
	} catch (SQLException e) {
	  Log.log(0, "Insert query to database "+db_name+" on "+db_host+" on port "+db_port+" failed."
	  		+ "\nVar dump:"
	  		+ "\nclient_id: "+ client_id
	  		+ "\npos_x: "+ pos_x
	  		+ "\npos_y: "+pos_y
	  		+ "\nturn: "+turn);
	  return false;
	}
	return true;
  }
  
  
  public ArrayList<DB_Result> read_scores(){
	ArrayList<DB_Result> result = new ArrayList<DB_Result>();
	try {
	  Statement db_statement = db_connection.createStatement();
	  String sql = "SELECT player_0, player_1, score_0, score_1, timestamp FROM match_history";
	  ResultSet db_result = db_statement.executeQuery(sql);
	  while (db_result.next()){
	    result.add(new DB_Result(db_result.getString("player_0"), db_result.getString("player_1"), db_result.getInt("score_0"), db_result.getInt("score_1"), db_result.getTimestamp("timestamp")));
	  }
	} catch (SQLException e) {
	  Log.log(0, "Reading scores from DB failed");
	}
	return result;
  }
  
/*  public DB_Result get_high_score(){
	DB_Result result = null;
	try {
	  Statement db_statement = db_connection.createStatement();
	  String sql = "SELECT player_0, player_1, score_0, score_1, timestamp FROM match_history ORDER BY abs(score_0 - score_1) DESC LIMIT 1;";
	  ResultSet db_result = db_statement.executeQuery(sql);
	  result = new DB_Result(db_result.getString("player_0"), db_result.getString("player_1"), db_result.getInt("score_0"), db_result.getInt("score_1"), db_result.getTimestamp("timestamp"));
	} catch (SQLException e) {
	  Log.log(0, "Reading high score from DB failed");
	}
	return result;
  }*/
}