/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author metelko
 */
import java.awt.*;
import javax.swing.*;
public class Pawn extends Piece

{
    int piececolor;
    int xpawnpos;
    int ypawnpos;
    ImageIcon icon = new ImageIcon("wpawn.gif");


    public Pawn (int x, int y)
    {
        xpawnpos=x;
        ypawnpos=y;
    }


    public void drawpiece(Graphics g)
    {
        Image img = Toolkit.getDefaultToolkit().getImage("wpawn.gif");
        g.drawImage(icon.getImage(), xpawnpos, ypawnpos, null);
    }
}