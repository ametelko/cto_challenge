import javax.swing.*;
import java.awt.event.*;
import java.awt.*;




public class Chess extends JFrame implements MouseListener, ActionListener, ItemListener
{

		JFrame frame = new JFrame("Chess by Alan Metelko");
		JComboBox numberPlayer = new JComboBox();
		static JLabel playerTurn = new JLabel("White's Turn");
		Font turnFont = new Font("Serif", Font.ITALIC, 32);
		JButton resetBoard = new JButton("Reset Board");
		JButton speedButton = new JButton("Change Computer Speed");
		JButton reverseMove = new JButton("Back One Move");
		int delay1=1000; // number of milliseconds to delay before
		int delay2=1;    // executing computer moves in computer-only play
		Timer tslow = new Timer (delay1, this); //timer to execute one move per second
		Timer tfast = new Timer (delay2, this); //timer to execute one move per millisecond

        BoardComponent cb = new BoardComponent();

        public Chess()
    {

		frame.setSize(430,550);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(cb);
        frame.addMouseListener(this);
        frame.setLayout(null);

        cb.setBounds (0,0,430,550);
        resetBoard.setBounds(cb.xstart+195,cb.ystart+330,125,20);
        speedButton.setBounds(cb.xstart,cb.ystart+360,175,20);
        reverseMove.setBounds(cb.xstart+195,cb.ystart+360,125,20);
        playerTurn.setBounds(cb.xstart+80,cb.ystart-75,250,50);

        frame.add(resetBoard);
        frame.add(speedButton);
        frame.add(reverseMove);
        frame.add(playerTurn);


		Color darkcolor = new Color (233,174,95);
		playerTurn.setForeground(darkcolor);
		playerTurn.setFont(turnFont);

        resetBoard.addActionListener(this);
        speedButton.addActionListener(this);
        reverseMove.addActionListener(this);

        createnumberPlayerBox();

        frame.setVisible(true);

    }
		public void mouseEntered(MouseEvent event){}

        public void mouseExited(MouseEvent event){}

		public void mousePressed(MouseEvent event)
			{

				if (cb.continueGame==true)
				{
					int x=event.getX()-cb.xstart-4;
					int y=event.getY()-cb.ystart-23;/* -4 x and -23 y are adjustments for Windows XP mouse pointer.
														Vista is different*/
					int xgrid=x/cb.boxsize;
					int ygrid=y/cb.boxsize;
					/*check to see if the mouse is on the chessboard*/
					if (x>0 && y>0 && x<cb.xrows*cb.boxsize && y<cb.yrows*cb.boxsize && cb.checkempty(cb.piecepositions,xgrid,ygrid)==false)
					{

					cb.selectpiece(xgrid,ygrid);  //choose piece at xgrid, ygrid if a valid piece is there
					}
				}

			}

	    public void mouseClicked(MouseEvent event)

	       	{

			}

        public void mouseReleased(MouseEvent event)
        	{


				if (cb.continueGame==true)
				{

					int x=event.getX()-cb.xstart-4;
					int y=event.getY()-cb.ystart-23;  /* -4 x and -23 y are adjustments for Windows XP mouse pointer.
														Vista is different*/
					int xgrid=x/cb.boxsize;
					int ygrid=y/cb.boxsize;

					if (x>0 && y>0 && x<(cb.xrows*cb.boxsize) && y<(cb.yrows*cb.boxsize) ) /*check to see if the mouse
																							is on the chessboard*/
					{
						cb.releasepiece(xgrid,ygrid);
						if (cb.continueGame==true && cb.turnOver==true && cb.two_player==false)//computer player's turn
						{
							cb.computerTurn3();
						}
					}
				}
			}

		public void actionPerformed(ActionEvent event)
			{
				if ((event.getSource()==tslow || event.getSource()==tfast)) cb.computerTurn3(); //this is called every second when timer starts
				if (event.getSource()==resetBoard)  //reset pieces on board and stop computer play
				{
					tslow.stop();
					tfast.stop();
					cb.slow_delay=true;
					cb.timer_on=false;
					cb.move=0;
					cb.resetPieces();
					cb.two_player=true;
					numberPlayer.setSelectedItem("Human vs. Human");
				}
				if (event.getSource()==speedButton) //toggle between 1 second and 1 millisecond delay for computer-only play
				{




					if (cb.slow_delay==false)
					{
						cb.slow_delay=true;
						tfast.stop();
						//System.out.println("tslow should start.  slow_delay="+cb.slow_delay);

						if (cb.timer_on==true) tslow.start();
					}

					else if (cb.slow_delay==true)
					{
						cb.slow_delay=false;

						tslow.stop();
						//System.out.println("tfast should start.  slow_delay="+cb.slow_delay);
						if (cb.timer_on==true) tfast.start();
					}



				}
				 if (cb.suspend_game==true) //used to stop game when king has no valid move to avoid infinite loop
					{
					tslow.stop();
					tfast.stop();
					cb.slow_delay=true;
					cb.timer_on=false;
					cb.move=0;
					cb.resetPieces();
					cb.two_player=true;
					numberPlayer.setSelectedItem("Human vs. Human");
					cb.suspend_game=false;
					}
				if (cb.move==1 && event.getSource()==reverseMove)
				{
					tslow.stop();
					tfast.stop();
					cb.slow_delay=true;
					cb.timer_on=false;
					cb.move=0;
					cb.resetPieces();
					cb.two_player=true;
					numberPlayer.setSelectedItem("Human vs. Human");
				}

				if (cb.move>1 && cb.move <2000 && event.getSource()==reverseMove) cb.reverseMove();
			}
		public void createnumberPlayerBox() //creates combo box for 2-player, 1-player on computer-only play
		{

			numberPlayer.addItem("Human vs. Human");
			numberPlayer.addItem("Human vs. Computer");
			numberPlayer.addItem("Computer vs. Computer");
			numberPlayer.setEditable(false);
			numberPlayer.addItemListener(this);
			numberPlayer.setBounds(cb.xstart,cb.ystart+330,175,20);
			frame.add(numberPlayer);
		}



		public void itemStateChanged( ItemEvent e ) //method for changing options in combo box
		{
			if( e.getSource() == numberPlayer
					&& numberPlayer.getSelectedItem() == "Computer vs. Computer" )
			{

				if (cb.timer_on==false)
				{
					if (cb.slow_delay==true) tslow.start();
					if (cb.slow_delay==false) tfast.start();  //start appropriate timer when 2 computer players are chosen
					cb.timer_on=true;
				}
			}

			if (e.getSource() == numberPlayer
					&& numberPlayer.getSelectedItem() == "Human vs. Computer" )  /*timer not needed but computer player is
																				 still needed*/
			{
				tslow.stop();
				tfast.stop();
				cb.timer_on=false;
				cb.two_player=false;
			}

			if (e.getSource() == numberPlayer
					&& numberPlayer.getSelectedItem() == "Human vs. Human" )//neither timer nor computer player needed
			{
				tslow.stop();
				tfast.stop();
				cb.timer_on=false;
				cb.two_player=true;
			}


		}


	public static void main(String[] args)
	{
		Chess chess = new Chess();
	}
}