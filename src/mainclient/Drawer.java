package mainclient;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Drawer extends Component {
  public static int square = 13;

  public void paint(Graphics g) {
    g.setColor(Color.BLACK);
    g.setFont(new Font("Courier New", Font.PLAIN, 15));
    if (Client_Run.status == 2) {
      g.drawString("Waiting to Play", 10, 10);
    } else {
      g.drawString("Playing", 10, 10);

    }

    BufferedImage grass = null;
    BufferedImage water = null;
    BufferedImage mountain = null;
    try {
      grass = ImageIO.read(new File("icons//grass.png"));
      water = ImageIO.read(new File("icons//water.png"));
      mountain = ImageIO.read(new File("icons//mountain.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    for (int i = 0; i < 4; i++) {
      char[] allchar = Client_Run.client_map[i].toCharArray();
      for (int j = 0; j < 4; j++) {
        int x = j * square + 10;
        int y = i * square + 10;
        switch (allchar[j]) {
        case 'g':
          g.drawImage(grass, x, y - square, null);
          break;
        case 'w':
          g.drawImage(water, x, y - square, null);
          break;
        case 'm':
          g.drawImage(mountain, x, y - square, null);
          break;
        default:
          break;
        }
      }
    }
  }
}