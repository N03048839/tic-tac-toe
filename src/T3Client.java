import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;


public class T3Client 
{
	private final int BOARD_SIZE;
	private final int BOARD_DIM;
	
	final char icon;
	
	protected PrintStream screen = null;
	protected Scanner kb = null;
	
	
	public T3Client(final int boardSize, final int boardDim, final char icon) 
	{
		this.BOARD_SIZE = boardSize;
		this.BOARD_DIM = boardDim;
		this.icon = icon;
		
		screen = System.out;
		kb = new Scanner( System.in );
	}
	
	
	/**
	 * Gets a move from the player.
	 * @return board index corresponding to the player's move
	 */
	int prompt(String message)
	{
		/* Get player's move */
		screen.print("\n" + message + "\n> ");
		int s = kb.nextInt();
		
		/* Parse player input */
		
		return s;
	}
	
	
	void print(String s)
	{
		screen.print(s);
	}
}
