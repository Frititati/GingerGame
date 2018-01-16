package mainclient;

import javax.swing.JApplet;

public class JAppletControl extends JApplet {
  Drawer squaresitem;

  public JAppletControl() {
    squaresitem = new Drawer();
    add(squaresitem);
  }
}