import java.awt.*;
import javax.swing.*;
import java.util.Random;



public class BoardComponent extends JComponent

{

    public final int xrows=8;  //number of squares horizontally
    public final int yrows=8;  //number of squares vertically
    public final int boxsize=40;  //size of chessquare in pixels
    public final int xstart=50;   //starting x position of chessboard
    public final int ystart=100;   //starting y position of chessboard
    boolean selected; //true if piece has been selected, otherwise false
    int xselect;  //x coordinate of selected piece
    int yselect;  //y coordinate of selected piece
    Boolean turnOver;  /*set to true once white player has executed move.  This variable
    keeps white's turn from being over if it has selected a piece but not moved it.*/
    boolean whiteturn;  //true if white's turn, false if black's turn
	boolean continueGame; //play game as long as this variable is true
    boolean whiteKingcastle=false; //tracks if white has initiated king-side castle on that turn
    boolean blackKingcastle=false; //tracks if black has initiated king-side castle on that turn
    boolean whiteQueencastle=false; //tracks if white has initiated queen-side castle on that turn
    boolean blackQueencastle=false; //tracks if black has initiated queen-side castle on that turn
    /*neither of above variables prevents more than one castling of king*/
    int whitewin=0; //records who won if checkmate occurs
    boolean suspend_game=false; //used with notifyEnd() method to avoid infinite loops when king has no move
	boolean two_player=true;   //computerTurn2 not called when this is true
	boolean slow_delay=true;  //registers whether slow timer or fast timer is in use
	boolean timer_on=false;  //true if either timer is on
	int lookahead=2;




    boolean[][] whiteblack =new boolean [xrows][yrows];//array to track whether square is white or black


	String[][] piecepositions = new String[8][8]; //array tracks what piece is in each square
	String[][] testboard = new String[8][8];

	int []returnArray = new int [5];

	String[][][] moveRecord = new String[2000][8][8];//keep record of first 100 moves
	int move=0; //record move number;

public BoardComponent()
    {
        setBoard();
        resetPieces();
    }


public void paintComponent(Graphics g)/*this method draws the squares and then places the pieces
using arrays whiteblack and position*/

	{
		Color lightcolor = new Color (177,113,24);
		Color darkcolor = new Color (233,174,95);

    	if (whiteturn==true)
    	{

			Chess.playerTurn.setForeground(darkcolor);
			Chess.playerTurn.setText("White's Turn");
		}
    	if (whiteturn==false)
    	{

			Chess.playerTurn.setForeground(lightcolor);
			Chess.playerTurn.setText("Black's Turn");
		}

		if (continueGame==false)
		{
			g.setColor(Color.red);
			if (whitewin==1) g.drawString ("Checkmate! -- White wins!",xstart+100,500);
			else g.drawString ("Checkmate! -- Black wins!",xstart+100,ystart+400);
		}

		if (continueGame==true)
		{
			g.setColor(Color.red);
			String checkString = (checkEnemyAttacks(piecepositions, whiteturn)[0]<-1000) ?"Check!": "      ";
			g.drawString(checkString, xstart+155, ystart+400);
		}


		g.setColor(Color.blue);
		g.drawString("Move # "+(move+1), xstart+260,ystart-5);


		for (int i=0;i<xrows;i++)
		{
			for (int j=0;j<yrows;j++)
			{
				//draw chess squares
				if (whiteblack[i][j]==true) g.setColor(darkcolor);
				else g.setColor(lightcolor);
				g.fillRect(xstart+i*boxsize, ystart+j*boxsize, boxsize, boxsize);

				//draw piece icon
				ImageIcon icon = new ImageIcon(piecepositions[i][j]);
				if (checkempty(piecepositions,i,j)==false) g.drawImage(icon.getImage(), xstart+i*boxsize-2, ystart+j*boxsize-2,null);
			}
		}
		if (selected==true) //if mouse is clicked selected square is set to gray
		{
			g.setColor(Color.gray);
			g.fillRect(xstart+xselect*boxsize, ystart+yselect*boxsize, boxsize, boxsize);
			ImageIcon icon = new ImageIcon(piecepositions[xselect][yselect]);
			if (checkempty(piecepositions,xselect,yselect)==false) g.drawImage(icon.getImage(), xstart+xselect*boxsize-2, ystart+yselect*boxsize-2,null);
		}

    }

public void setBoard()  //set up chessboard pattern
{
    for (int i=0;i<xrows;i++)
            {
                for (int j=0;j<yrows;j++)
                {
                    if ((i+j)%2==0) whiteblack[i][j]=true;
                    else whiteblack[i][j]=false;
                }
            }
}

public void selectpiece(int x, int y) //select piece to move
	{
		if (continueGame==true)
		{
			selected=true;
			xselect=x;
			yselect=y;
			repaint();
		}
	}
public void releasepiece(int xrelease, int yrelease) //move selected piece to selected square if move is valid
	{
		turnOver=false;
		if (continueGame==true )
		{

			if (canmove(piecepositions,xselect, yselect, xrelease, yrelease, false)==false)

			{
				selected=false;
				repaint();// piece is not moved
			}
			else
			{

				if (whiteKingcastle==false && blackKingcastle==false && whiteQueencastle==false && blackQueencastle==false)
				{
				piecepositions[xrelease][yrelease]=piecepositions[xselect][yselect];
				piecepositions[xselect][yselect]="e"; //piece is moved to new position and previous position is made empty
				}

				if (whiteKingcastle==true) whiteKingcastleking();
				if (blackKingcastle==true) blackKingcastleking();
				if (whiteQueencastle==true) whiteQueencastleking();
				if (blackQueencastle==true) blackQueencastleking();
				if(piece_id(piecepositions,xrelease,yrelease).equals("pawn"))
					pawnqueen(piecepositions,xrelease, yrelease); //check to see if pawn can be set to queen

				selected=false;
				turnOver=true;
				move++;
				copyMoves();
				if (whiteturn==true) whiteturn=false; else whiteturn=true;
				checkmate();
				repaint();  //move piece to new position and erase piece from old position

			}
		}
	}
public boolean canmove(String[][]position, int startx, int starty, int endx, int endy, boolean checkmove)
//check for valid move.  Checkmove paramater is true if canmove is being called by checkmove() method
	{
		boolean white;

		if (checkwhite(position,startx,starty)==true) white=true;
			else white=false;//determine if the piece is white or black

		if (checkmove==false)
		{
			if (white!=whiteturn) return false;


			/*check to ensure that start position and end position are not inhabited by piece of the
			same color.  Also ensures that piece can't take itself*/
			if (position[startx][starty].substring(0,1).equals(position[endx][endy].substring(0,1)))
			return false;
		}

		if (piece_id(position, startx,starty).equals("pawn") &&
		canmovepawn(position,white,startx, starty, endx, endy, checkmove)==true) return true;

		if (piece_id(position,startx,starty).equals("rook") &&
		canmoverook(position,white, startx, starty, endx, endy)==true) return true;

		if (piece_id(position,startx,starty).equals("bish") &&
		canmovebishop(position,startx, starty, endx, endy)==true) return true;

		if (piece_id(position,startx,starty).equals("king") &&
		canmoveking(startx, starty, endx, endy)==true) return true;

		if (piece_id(position,startx,starty).equals("quee") &&
		canmovequeen(position,startx, starty, endx, endy)==true) return true;

		if (piece_id(position,startx,starty).equals("knig") &&
		canmoveknight(startx, starty, endx, endy)==true) return true;

		return false;
	}

	public boolean canmovepawn(String[][] position, boolean pawnwhite, int spawnx, int spawny, int epawnx, int epawny,
	boolean checkmove) //check for pawn valid move
		{

			//rules for when white pawn can move forward
			if (pawnwhite==true && checkmove==false && spawny==6 && epawny==4 && spawnx==epawnx &&
			position[epawnx][epawny+1].equals("e") && position[epawnx][epawny].equals("e")) return true;

			if (pawnwhite==true && checkmove==false && spawny<=6 && spawny==(epawny+1) && spawnx==epawnx &&
			position[epawnx][epawny].equals("e")) return true;

			//rules for when white pawn can white pawn can take black piece
			if (pawnwhite==true && ((spawnx==0 && epawnx==1)|| (spawnx==7 && epawnx==6)) && spawny==(epawny+1) &&
			(position[epawnx][epawny].substring(0,1).equals("b")||checkmove==true)) return true;

			if (pawnwhite==true && spawnx>0 && spawnx<7 && ((spawnx==(epawnx-1))||(spawnx==(epawnx+1)))
			&& spawny==(epawny+1) && (position[epawnx][epawny].substring(0,1).equals("b")||checkmove==true)) return true;


			//rules for when black pawn can move forward
			if (pawnwhite==false && checkmove==false && spawny==1 && epawny==3 && spawnx==epawnx &&
			position[epawnx][epawny-1].equals("e") && position[epawnx][epawny].equals("e")) return true;
			if (pawnwhite==false && spawny>=1 && spawny==(epawny-1) && spawnx==epawnx &&
				position[epawnx][epawny].equals("e")) return true;

			//rules for when black pawn can take white piece
			if (pawnwhite==false && ((spawnx==0 && epawnx==1) || (spawnx==7 && epawnx==6)) && spawny==(epawny-1) &&
			(checkwhite(position,epawnx,epawny)==true || checkmove==true)) return true;

			if (pawnwhite==false && spawnx>0 && spawnx<7 && ((spawnx==(epawnx-1)) || (spawnx==(epawnx+1)))
			&& spawny==(epawny-1) && (checkwhite(position,epawnx,epawny)==true || checkmove==true)) return true;


			return false;
		}

	public boolean canmoverook(String[][] position, boolean white, int srookx, int srooky, int erookx, int erooky) //check for rook valid move
		{
			/* check if white king castle was initiated by king rook*/
			if (srookx==7 && srooky==7 && white==true && erookx==5 && erooky==7
			&& piecepositions[6][7].equals("e") && piecepositions[5][7].equals("e") &&
			piecepositions[4][7].equals("wking.gif") && checkMove(piecepositions,4,7)==true && checkMove(piecepositions,5,7)==true &&
			checkMove(piecepositions,6,7)==true)
				{
					whiteKingcastle=true;
					return true;
				}

			/* check if black king castle was initiated by king rook*/
			if (srookx==7 && srooky==0 && white==false && erookx==5 && erooky==0
			&& piecepositions[6][0].equals("e") && piecepositions[5][0].equals("e") &&
			piecepositions[4][0].equals("bking.gif")&& checkMove(piecepositions,4,0)==true && checkMove(piecepositions,5,0)==true &&
			checkMove(piecepositions,6,0)==true)
				{
					blackKingcastle=true;
					return true;
				}
			/* check if white queen castle was initiated by king rook*/
			if (srookx==0 && srooky==7 && white==true && erookx==3 && erooky==7
			&& piecepositions[1][7].equals("e")&& piecepositions[2][7].equals("e") && piecepositions[3][7].equals("e") &&
			piecepositions[4][7].equals("wking.gif") && checkMove(piecepositions,4,7)==true && checkMove(piecepositions,3,7)==true &&
			checkMove(piecepositions,2,7)==true)
				{
					whiteQueencastle=true;
					return true;
				}

			/* check if black queen castle was initiated by king rook*/
			if (srookx==0 && srooky==0 && white==false && erookx==3 && erooky==0
			&& piecepositions[1][0].equals("e")&& piecepositions[2][0].equals("e") && position[3][0].equals("e") &&
			position[4][0].equals("bking.gif")&& checkMove(piecepositions,4,0)==true && checkMove(piecepositions,3,0)==true &&
			checkMove(piecepositions,2,0)==true)
				{
					blackQueencastle=true;
					return true;
	}

			if (srookx==erookx && ((erooky==srooky+1) || (erooky==srooky-1))) return true;
			if (srooky==erooky && ((erookx==srookx+1) || (erookx==srookx-1))) return true;
			if (srookx==erookx && erooky>(srooky+1))
				{
					for (int y=srooky+1;y<erooky;y++)
						{
							if (checkempty(position,erookx,y)==false) return false;
						}
					return true;
				}
			if (srookx==erookx && erooky<(srooky-1))
				{
					for (int y=srooky-1;y>erooky;y--)
						{
							if (checkempty(position, erookx,y)==false) return false;
						}
					return true;
				}
			if (srooky==erooky && erookx>(srookx+1))
				{
					for (int x=srookx+1;x<erookx;x++)
						{
							if (checkempty(position, x,erooky)==false) return false;
						}
					return true;
				}
			if (srooky==erooky && erookx<(srookx-1))
				{
					for (int x=srookx-1;x>erookx;x--)
						{
							if (checkempty(position, x,erooky)==false) return false;
						}
					return true;
				}

			return false;
		}

	public boolean canmovebishop(String[][] position, int sbishx, int sbishy, int ebishx, int ebishy) //check for bishop valid move
		{


			if (((sbishx-ebishx)==(sbishy-ebishy))||((sbishx-ebishx)==(ebishy-sbishy)))
			{
				int xdist=sbishx-ebishx;
				int ydist=sbishy-ebishy;

				if (xdist>1 && ydist>1)
				{
					for (int i=xdist-1;i>0;i--)
					{
						int checkx=sbishx-i; int checky=sbishy-i;
						if (checkempty(position, checkx, checky)==false) return false;
					}
				}

				if (xdist<1 && ydist<1)
				{
					int h=(xdist*-1)-1;
					for (int i=h;i>0;i--)
					{
						int checkx=sbishx+i; int checky=sbishy+i;
						if (checkempty(position, checkx, checky)==false) return false;
					}

				}

				if (xdist>1 && ydist<1)
				{
					for (int i=xdist-1;i>0;i--)
					{
						int checkx=sbishx-i; int checky=sbishy+i;
						if (checkempty(position, checkx, checky)==false) return false;
					}

				}

				if (xdist<1 && ydist>1)
				{
					for (int i=ydist-1;i>0;i--)
					{
						int checkx=sbishx+i; int checky=sbishy-i;
						if (checkempty(position, checkx, checky)==false) return false;
					}

				}
				return true;
			}

			return false;
		}

	public boolean canmoveking(int skingx, int skingy, int ekingx, int ekingy)//check for king valid move
		{
			if (ekingx>=(skingx-1) && ekingx<=(skingx+1) && ekingy>=(skingy-1) &&
				ekingy<=(skingy+1)) return true;
			return false;
		}

	public boolean canmovequeen(String[][] position, int squeenx, int squeeny, int equeenx, int equeeny) /* check for queen valid move
																	using methods for rook and bishop*/
		{
			if (canmovebishop(position, squeenx, squeeny, equeenx, equeeny)==true ||
			canmoverook(position, true,squeenx, squeeny, equeenx, equeeny)==true) return true;

			return false;
		}

	public boolean canmoveknight(int sknightx, int sknighty, int eknightx, int eknighty) //check for knight valid move
		{
			int xdistance=Math.abs(sknightx-eknightx);
			int ydistance=Math.abs(sknighty-eknighty);
			int distance=xdistance+ydistance;
			if (distance==3 && sknightx!=eknightx && sknighty!=eknighty) return true;
			else return false;
		}

	public void pawnqueen (String[][] position,int x, int y)  //convert pawn into queen if it reaches end of chessboard
	{
		if (checkwhite(position,x,y)==true && y==0) position[x][y]="wqueen.gif";
		if (checkwhite(position,x,y)==false && y==7) position[x][y]="bqueen.gif";
	}

	public boolean checkempty (String[][] position,int checkx, int checky) //return true if square is empty
	{
		if (position[checkx][checky].compareTo("e")!=0) return false;
		return true;
	}

	public boolean checkwhite (String[][] position,int x, int y) //return true if piece is white
	{
		if (position[x][y].substring(0,1).equals("w")) return true;
		return false;
	}
	public String piece_id (String[][] position,int x, int y)  //return four-letter chesspiece identifier
	{
		if (position[x][y].equals("e")) return "e";
		else return position[x][y].substring(1,5);
	}

	public void whiteKingcastleking()  //move pieces if white player castles
	{
		piecepositions[7][7]="e";
		piecepositions[6][7]="wking.gif";
		piecepositions[5][7]="wrook.gif";
		piecepositions[4][7]="e";
		whiteKingcastle=false;
		whiteQueencastle=false;
	}

	public void blackKingcastleking()  //move pieces if black player castles
	{
		piecepositions[7][0]="e";
		piecepositions[6][0]="bking.gif";
		piecepositions[5][0]="brook.gif";
		piecepositions[4][0]="e";
		blackKingcastle=false;
		blackQueencastle=false;
	}

		public void whiteQueencastleking()  //move pieces if white player castles
		{
			piecepositions[0][7]="e";
			piecepositions[1][7]="e";
			piecepositions[2][7]="wking.gif";
			piecepositions[3][7]="wrook.gif";
			piecepositions[4][7]="e";
			whiteQueencastle=false;
			whiteKingcastle=false;
		}

		public void blackQueencastleking()  //move pieces if black player castles
		{
			piecepositions[0][0]="e";
			piecepositions[1][0]="e";
			piecepositions[2][0]="bking.gif";
			piecepositions[3][0]="brook.gif";
			piecepositions[4][0]="e";
			blackQueencastle=false;
			blackKingcastle=false;
	}

	public void checkmate() //count number of kings on chessboard.  if <2 declare checkmate and end game
	{
		int kings=0;
		for (int i=0;i<8;i++)
			{
				for (int j=0;j<8;j++)
				{
					if (piecepositions[i][j].equals("bking.gif")) kings--;
					if (piecepositions[i][j].equals("wking.gif")) kings++;
				}
			}
		if (kings!=0)
			{
				if (kings==1) whitewin=1; else whitewin=-1;
				continueGame=false;

				repaint();
			}
	}

	public void computerTurn() //chooses a piece at random and executes a random valid move. not currently used.
	{
		boolean findpieceloop=false;
		boolean findtargetloop=false;
		boolean entireloop=false;
		boolean escapeloop=false;
		//find square at random that has a piece of the appropriate color with a valid move



		if (escapeloop==false && continueGame==true)
		entireloop=false;
		while (entireloop==false)
		{
			findpieceloop=false;
			while (findpieceloop==false)
			{

				Random g1 = new Random();
				int xselect=g1.nextInt(8);
				Random g2 = new Random();
				int yselect=g2.nextInt(8);
				if (checkwhite(piecepositions,xselect,yselect)==whiteturn && checkempty(piecepositions,xselect, yselect)==false &&
					movePossible(piecepositions,xselect, yselect)==true)
					{
					selectpiece(xselect,yselect);
					findpieceloop=true;
					}

			}

			int trymove=0; //variable to track how many attempts made to find a place for the randomly chosen piece to move
			findtargetloop=false;
			while (findtargetloop==false) //This loop attempts to find a valid and safe place for the randomly chosen piece to move to
			{
				trymove++;
				Random g3 = new Random();
				int xrelease=g3.nextInt(8);
				Random g4 = new Random();
				int yrelease=g4.nextInt(8);
				if (canmove(piecepositions,xselect, yselect, xrelease, yrelease, false)==true && checkMove(piecepositions,xrelease, yrelease)==true)
				{
					findtargetloop=true;
					entireloop=true;
					escapeloop=true;
					System.out.println("Moved "+piecepositions[xselect][yselect]+" from "+xselect+","+
					yselect+" to "+xrelease+","+yrelease);
					System.out.println("checkMove="+checkMove(piecepositions,xrelease, yrelease));
					releasepiece(xrelease, yrelease);
				}
				if (trymove>2000)  //give up after 2000 tries and go find another piece to move
				{
					if (numberFriendlyPieces()==1 && checkKingmove(piecepositions, xselect, yselect)==false)
					{
						notifyEnd();
						findtargetloop=true;
						entireloop=true;
						escapeloop=true;



					}
					else

					findtargetloop=true;
					entireloop=false;
					escapeloop=false;


				}
			}
		}
	}
	public int numberFriendlyPieces() /*check number of non-pawn pieces of current turn's
	color with valid moves that are still on the board*/
	{
		int moveable_pieces=0;
		for (int x=0;x<8;x++)
		{
			for (int y=0;y<8;y++)
			{
				if (checkwhite(piecepositions, x, y) == whiteturn && checkempty (piecepositions, x, y)==false
				&& movePossible(piecepositions, x, y)==true && getScore(piecepositions, -1,-1 ,x,y)>1) moveable_pieces++;
				{

				}

			}
		}


		return moveable_pieces;
	}


	public boolean movePossible(String[][] position,int xselect, int yselect) //check to see if piece has valid move
	{
				boolean movepossible=false;
				for (int i=0;i<8;i++)
				{
					for (int j=0;j<8;j++)
					{
						if (canmove(position,xselect, yselect, i, j, false)==true) movepossible=true;
					}
				}
				if (movepossible==true) return true; else return false;
	}

	public boolean checkKingmove(String[][] position, int xselect, int yselect)  //check to see if king has safe move
	{
		for (int i=-1;i<2;i++)
		{
			for (int j=-1;j<2;j++)
			{
				if (checkMove (position, xselect + i, yselect+j)==false) return false;
			}
		}

		return true;
	}


	public void computerTurn2()/*This method finds the move with the highest point value as determined
	by getScore or getScore2 method.  If no pieces can be taken, the computerTurn method is invoked instead. not currently used*/
	{
		checkmate();
		if (continueGame==true  && suspend_game==false)
		{
			boolean pieceSelected=false;
			int bestscore=0;
			int checkmovescore=0;
			int selectx=0;
			int selecty=0;
			int releasex=0;
			int releasey=0;
			int startx;
			int endx;
			int starty;
			int endy;
			int lookahead=1;

			for (startx=0;startx<8;startx++)

			{
				for (starty=0;starty<8;starty++)
				{
					if (checkempty(piecepositions,startx,starty)==false && checkwhite(piecepositions,startx,starty)==whiteturn)

					{

						for (endx=0;endx<8;endx++)
						{
							for (endy=0;endy<8;endy++)
							{

								if (canmove(piecepositions,startx,starty,endx,endy, false)==true && checkwhite(piecepositions,endx,endy)!=whiteturn
									&& checkempty(piecepositions,endx,endy)==false)
								{
									System.out.println("Consider taking "+piecepositions[endx][endy]+" at "+endx+","+endy+
									" with "+piecepositions[startx][starty]+" at "+startx+","+starty+" Checkmove="+checkMove(piecepositions,endx,endy)
									+" checkEnemyAttacks2 from "+startx+","+starty+" to "+endx+","
											+endy+"="+checkEnemyAttacks2(startx, starty, endx, endy, lookahead));
									if (checkMove(piecepositions, endx, endy)==false)
									{
										if (getScore2(piecepositions,startx, starty, endx,endy)>=bestscore)
										{
											bestscore=getScore2(piecepositions,startx, starty, endx,endy);
											pieceSelected=true;
											System.out.println("bestscore="+bestscore+" checkEnemyAttacks2 from "+startx+","+starty+" to "+endx+","
											+endy+"="+checkEnemyAttacks2(startx, starty, endx, endy, lookahead));
											selectx=startx;selecty=starty;releasex=endx;releasey=endy;
										}
									}
									if (checkMove(piecepositions, endx, endy)==true)
									{
										if (getScore(piecepositions,startx, starty, endx,endy)>=bestscore)
										{
											bestscore=getScore(piecepositions,startx, starty, endx,endy);
											System.out.println("bestscore="+bestscore+" checkEnemyAttacks2 from "+startx+","+starty+" to "+endx+","
											+endy+"="+checkEnemyAttacks2(startx, starty, endx, endy, lookahead));
											pieceSelected=true;
											selectx=startx;selecty=starty;releasex=endx;releasey=endy;
										}
									}
								}
							}
						}
					}
				}
			}


			if (bestscore>=0 && pieceSelected==true)
			{
				selectpiece(selectx,selecty); releasepiece (releasex, releasey);
			}
			else computerTurn();
		}
	}
	public int getScore (String[][] position,int startx, int starty, int endx, int endy)
	//return value of target
	{
		int score=0;
		if (piece_id(position,endx,endy).equals("pawn")) score=1;
		if (piece_id(position,endx,endy).equals("bish")) score=3;
		if (piece_id(position,endx,endy).equals("knig")) score=3;
		if (piece_id(position,endx,endy).equals("rook")) score=5;
		if (piece_id(position,endx,endy).equals("quee")) score=9;
		if (piece_id(position,endx,endy).equals("king")) score=10000;
		if (piece_id(position,endx,endy).equals("e"))
		{
			if (checkwhite(position, startx, starty)==true && starty==1 && piece_id(position, startx, starty).equals("pawn")) score=8;
			else if (checkwhite(position, startx, starty)==false && starty==6 && piece_id(position, startx, starty).equals("pawn")) score=8;
			else score=0;
		}
		return score;
	}


		public int getScore2 (String[][] position,int startx, int starty, int endx, int endy)
		//return value of attacker subtracted from value of target.  not currently used
		{
			int target_score=0;
			int attack_score=0;
			int score=0;
			if (piece_id(position,endx,endy).equals("pawn")&&
			checkwhite(position, endx,endy)!=whiteturn) target_score=1;
			if (piece_id(position,endx,endy).equals("bish")&&
			checkwhite(position, endx,endy)!=whiteturn) target_score=3;
			if (piece_id(position,endx,endy).equals("knig")&&
			checkwhite(position, endx,endy)!=whiteturn) target_score=3;
			if (piece_id(position,endx,endy).equals("rook")&&
			checkwhite(position, endx,endy)!=whiteturn) target_score=5;
			if (piece_id(position,endx,endy).equals("quee")&&
			checkwhite(position, endx,endy)!=whiteturn) target_score=9;
			if (piece_id(position,endx,endy).equals("king")&&
			checkwhite(position, endx,endy)!=whiteturn) target_score=10000;
			if (piece_id(position,endx,endy).equals("e")) target_score=0;

			if (piece_id(position,startx, starty).equals("pawn") &&
			checkwhite(position, startx, starty)==whiteturn) attack_score=1;
			if (piece_id(position,startx, starty).equals("bish") &&
			checkwhite(position, startx, starty)==whiteturn) attack_score=3;
			if (piece_id(position,startx, starty).equals("knig") &&
			checkwhite(position, startx, starty)==whiteturn) attack_score=3;
			if (piece_id(position,startx, starty).equals("rook") &&
			checkwhite(position, startx, starty)==whiteturn) attack_score=5;
			if (piece_id(position,startx, starty).equals("quee") &&
			checkwhite(position, startx, starty)==whiteturn) attack_score=9;
			if (piece_id(position,startx, starty).equals("king") &&
			checkwhite(position, startx, starty)==whiteturn) attack_score=10000;
			score=target_score-attack_score;
			System.out.println ("Starting Square ="+startx+","+starty+" Ending Square ="+endx+","+endy+
			"   target_score="+target_score+"   attack_score="+attack_score+"   score="+score);


			return score;
	}

	public void resetPieces()  //set initial configuration of pieces on chessboard
	{

		String reset[][] =  {
	{"brook.gif","bpawn.gif","e","e","e","e","wpawn.gif","wrook.gif",},
	{"bknight.gif","bpawn.gif","e","e","e","e","wpawn.gif","wknight.gif"},
	{"bbishop.gif","bpawn.gif","e","e","e","e","wpawn.gif","wbishop.gif"},
	{"bqueen.gif","bpawn.gif","e","e","e","e","wpawn.gif","wqueen.gif"},
	{"bking.gif","bpawn.gif","e","e","e","e","wpawn.gif","wking.gif"},
	{"bbishop.gif","bpawn.gif","e","e","e","e","wpawn.gif","wbishop.gif"},
	{"bknight.gif","bpawn.gif","e","e","e","e","wpawn.gif","wknight.gif"},
	{"brook.gif","bpawn.gif","e","e","e","e","wpawn.gif","wrook.gif"}};
		piecepositions=reset;
		repaint();
		selected=false;
		continueGame=true;
		whiteturn=true;
		whitewin=0;
		move=0;
		slow_delay=true;
		timer_on=false;
		two_player=true;

	}
public boolean checkMove(String[][]position ,int x, int y) //returns true if queried square is safe from enemy attack
	{
		//System.out.println("Checking square: "+x+","+y);
		int i,j;
		for (i=0;i<8;i++)

		{
			for (j=0;j<8;j++)
			{
				if (x==i && y==j) continue;
				if (checkempty(position,i,j)==false && checkwhite(position,i,j)!=whiteturn &&
				canmove(position,i,j,x,y,true)==true)
				{

					//System.out.println("Attacker "+position[i][j]+" is at "+i+","+j);
					return false;
				}
			}
		}


		return true;
	}

public void copyMoves() //copy current piece positions to 3D moveRecord array
	{
		if (move<2000)
		{

			for (int i=0;i<8;i++)
			{
				for (int j=0;j<8;j++)
				moveRecord[move][i][j]=piecepositions[i][j];
			}
		}
	}
public void reverseMove() //retrieve state of chessboard at move-1
	{
		if (move==1) resetPieces();

		if (move>1 && move<2000)
		{

			move--;
			for (int i=0;i<8;i++)
			{
				for (int j=0;j<8;j++)
				piecepositions[i][j]=moveRecord[move][i][j];
			}
			repaint();
			if (whiteturn==true) whiteturn =false; else whiteturn=true;

		}

	}
public int[] checkEnemyAttacks(String[][]position, boolean piecewhite ) /*returns a negative value indicating the value of most valuable piece subject to enemy capture
								so far only used to determine if king is in check*/
	{
		int [] returnArray= new int [5];
		int i,j,k,l, score=0;
		returnArray[0]=0;
		returnArray[1]=-1;
		returnArray[2]=-1;
		returnArray[3]=-1;
		returnArray[4]=-1;

		for (i=0;i<8;i++)
		{
			for (j=0;j<8;j++)
			{
				if (checkempty (position, i,j) == false &&
				checkwhite(position, i,j) ==piecewhite)
				{
					for (k=0;k<8;k++)
					{
						for (l=0;l<8;l++)
						{
							if (checkempty (position, k,l) == false &&
							checkwhite(position, k,l) !=piecewhite)
							{
								//System.out.println("checking to see if "+piecepositions[k][l]+ " can attack "+piecepositions[i][j]);
								if (canmove (position, k,l,i,j, true)==true)
								{
									score= getScore (position,k,l,i,j)*-1;
									if (score<returnArray[0])
									{
										returnArray[0]=score;
										returnArray[1]=k;//x position of potential attacker
										returnArray[2]=l;//y position of potential attacker
										returnArray[3]=i;//x position of space being checked for vulnerability
										returnArray[4]=j;//y position of space being checked for vulnerability
									}
								}

							}

						}

					}

				}
			}
		}
		return returnArray;
	}

public int checkEnemyAttacks2(int startx, int starty, int endx, int endy, int lookahead)
/*this method copies chessboard array in order to compute the value of the
most dangerous enemy counter to a proposed move*/
{
	lookahead--;
	String[][] testboard = new String [8][8];
	int score=0;
	resetTestboard(testboard);
	testboard[endx][endy]=testboard[startx][starty];
	testboard[startx][starty]="e"; //piece is moved to new position and previous position is made empty
	boolean piecewhite=checkwhite(piecepositions, startx, starty);
	score=checkEnemyAttacks(testboard, piecewhite)[0]; //value of enemy's most dangerous move
	int newstartx=checkEnemyAttacks(testboard, piecewhite)[1]; //starting x position of enemy if attack is possible
	int newstarty=checkEnemyAttacks(testboard, piecewhite)[2]; //starting y position of enemy if attack is possible
	int newendx=checkEnemyAttacks(testboard, piecewhite)[3]; //ending x position of enemy if attack is possible
	int newendy=checkEnemyAttacks(testboard, piecewhite)[4]; //ending y position of enemy if attack is possible
	//System.out.println("If I move from "+startx+","+starty+" to "+endx+","+endy);
	//System.out.println ("Value of enemy's move from "+newstartx+","+newstarty+" to "+newendx+","+newendy+" is "+ score);

	if (lookahead>=1 && score!=0)
	{
		testboard[newendx][newendy]=testboard[newstartx][newstarty];
		testboard[newstartx][newstarty]="e";
		piecewhite=(piecewhite==true)?false:true;
		//System.out.println("The value of the enemy piece is "+getScore(testboard, newstartx, newstarty, newendx, newendy));
		int nextScore=checkEnemyAttacks(testboard, piecewhite)[0]*-1;
		int newstartx1=checkEnemyAttacks(testboard, piecewhite)[1];
		int newstarty1=checkEnemyAttacks(testboard, piecewhite)[2];
		int newendx1=checkEnemyAttacks(testboard, piecewhite)[3];
		int newendy1=checkEnemyAttacks(testboard, piecewhite)[4];
		//System.out.println("The value of my next from from "+newstartx1+","+newstarty1+" to "+newendx1+","+newendy1+" is "+nextScore);
		if (score>-9000) score=score+nextScore;
		//System.out.println("The value of the total enemy response 1 move ahead is "+score);
	}

	return score;
}

public void resetTestboard(String[][]testboard)
{
	for (int i=0;i<8;i++)
	{
		for (int j=0;j<8;j++)
		{
			testboard[i][j]=piecepositions[i][j];
		}
	}
}

public void computerTurn3()
	{
		checkmate();
		if (continueGame==true  && suspend_game==false)
		{
			int [] getMove = new int [5];

			if (move==1)
				{
					getMove[1]=3;getMove[2]=1;getMove[3]=3;getMove[4]=3;
				}
			/*else if (move==3)
				{
					getMove[1]=4;getMove[2]=1;getMove[3]=4;getMove[4]=3;
				}*/
			else getMove=generateMove();
			if (getMove[0]>-9990)
			{
				/*System.out.println ("Moving "+piecepositions[selectx][selecty]+" from "+selectx+","+selecty+" to "+releasex+","+releasey+"  bestscore="+bestscore);
				System.out.println("computerTurn3 succeeded!");*/
				selectpiece(getMove[1],getMove[2]);releasepiece(getMove[3], getMove[4]);
			}
			else

			notifyEnd();
		}

	}
public int[] generateMove()//chooses move that maximizes point value of move
{


	int[] returnArray=new int[5];
	int selectx=-1, selecty=-1, releasex=-1, releasey=-1;
	int i,j,k,l,mod1,mod2,mod3=1;int score=0, bestscore=-10000;
	Random g1 = new Random();
	int randmove1=g1.nextInt(100);
	if (randmove1>50)
	{
		mod1=0;mod2=8;mod3=1;
	}
	else
	{
		mod1=7;mod2=-1;mod3=-1;
	}
	for ( i=mod1;i!=mod2;i+=mod3)
	{
		for ( j=mod1;j!=mod2;j+=mod3)
		{
			if (checkwhite(piecepositions,i,j)==whiteturn && checkempty(piecepositions,i,j)==false &&
				movePossible(piecepositions,i,j)==true)
			{
				for ( k=mod1;k!=mod2;k+=mod3)
				{
					for (l=mod1;l!=mod2;l+=mod3)
					{
						if (canmove(piecepositions,i,j,k,l,false)==true)
						{
							int getEnemyScore=checkEnemyAttacks2(i,j,k,l,lookahead);

							score=getScore(piecepositions,i,j,k,l)+getEnemyScore;

							if (score>bestscore)
							{
								bestscore=score;
								selectx=i;selecty=j;releasex=k;releasey=l;
							}
							if (score==bestscore)
							{
								Random g2 = new Random();
								int randmove2=g2.nextInt(100);
								if (randmove2>50)
								{
									bestscore=score;
									selectx=i;selecty=j;releasex=k;releasey=l;
								}

							}

						}
					}
				}
			}
		}

	}
	returnArray[0]=bestscore;
	returnArray[1]=selectx;
	returnArray[2]=selecty;
	returnArray[3]=releasex;
	returnArray[4]=releasey;
	System.out.println();
	return returnArray;
}

public void notifyEnd()  //called to suspend game when king is only moveable piece but has no safe move
{
		String kingColor;
		kingColor = (whiteturn==true) ?"white":"black";
		String message = (timer_on==true) ?"  The board will now reset.":"  Please reset the board.";
		JOptionPane.showMessageDialog(null,
    	"The "+kingColor+" king is in checkmate."+message);
		repaint();
		selected=false;
		continueGame=true;
		whiteturn=true;
		whitewin=0;
		slow_delay=true;
		timer_on=false;
		two_player=true;
		suspend_game=true;

}


}



