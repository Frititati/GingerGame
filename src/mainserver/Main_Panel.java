package mainserver;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Main_Panel extends JPanel {
  private JLabel client1_status_label;
  private JLabel client2_status_label;
  private int client1_status, client2_status;
  String[] client_status_list = { "disconnected", "connecting", "connected", "playing" };
  Color[] client_status_colors = { Color.GRAY, Color.BLUE, Color.GREEN, Color.RED };

  public Main_Panel() {
    setLayout(new BorderLayout());

    JPanel client1_panel = new JPanel();
    JLabel client1 = new JLabel("Client 1");
    client1_status_label = new JLabel();
    client1_panel.add(client1);
    client1_panel.add(client1_status_label);

    JPanel client2_panel = new JPanel();
    JLabel client2 = new JLabel("Client 2");
    client2_status_label = new JLabel();
    client2_panel.add(client2);
    client2_panel.add(client2_status_label);

    set_status(1, 0);
    set_status(2, 0);
    add(client1_panel, BorderLayout.NORTH);
    add(client2_panel, BorderLayout.SOUTH);
    setBorder(new EmptyBorder(10, 10, 10, 10));
  }

  public boolean set_status(int client_num, int status) {
    String action = client_status_list[status];
    Color bg = client_status_colors[status];
    if (client_num == 1) {
      client1_status = status;
      client1_status_label.setText(action);
      client1_status_label.setForeground(bg);
    } else if (client_num == 2) {
      client2_status = status;
      client2_status_label.setText(action);
      client2_status_label.setForeground(bg);
    } else {
      return false;
    }
    repaint();
    return true;
  }
}
