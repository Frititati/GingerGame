package mainserver;

import javax.swing.*;

public class Server_Start {
  public static void main(String[] args) {
    Server_Start server = new Server_Start();

  }

  public Server_Start() {
    JFrame frame = new JFrame();
    Main_Panel panel = new Main_Panel();
    frame.add(panel);
    frame.pack();
    frame.setVisible(true);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }
}
