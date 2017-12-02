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
  private String[] client_status_list = { "disconnected", "connecting", "connected", "playing" };
  Color[] client_status_colors = { Color.GRAY, Color.BLUE, Color.GREEN, Color.RED };
  private JPanel inner_panel;

  public Main_Panel() {
    inner_panel = new JPanel();
    inner_panel.setLayout(new BorderLayout());

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

    inner_panel.add(client1_panel, BorderLayout.NORTH);
    inner_panel.add(client2_panel, BorderLayout.SOUTH);
    inner_panel.setBorder(new EmptyBorder(10, 10, 10, 10));
    add(inner_panel);
  }

  public boolean set_status(int client_num, int status) {
    // System.out.println("Setting something " + status);
    String action = client_status_list[status];
    Color bg = client_status_colors[status];
    if (client_num == 0) {
      client1_status = status;
      client1_status_label.setText(action);
      client1_status_label.setForeground(bg);
    } else if (client_num == 1) {
      client2_status = status;
      client2_status_label.setText(action);
      client2_status_label.setForeground(bg);
    } else {
      return false;
    }
    inner_panel.repaint();
    inner_panel.revalidate();
    return true;
  }
}
