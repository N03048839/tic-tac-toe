import java.io.PrintStream;



public class T3Server implements Runnable
{
	private static final int BOARD_SIZE = 4;
	private static final int BOARD_DIM = 2;
	private static final char EMPTY_SPACE = '-';
	
	private static final String WIN_MESSAGE = "Congratulations! You win!";
	private static final String LOSS_MESSAGE = "You lose";
	private static final String TIE_MESSAGE = "Game over -- Tie";
	
	private T3Client[] clients;
	private int clientCount = 1;
	
	private char[] board;
	
	private boolean gameOver = false;
	
	
	
	/**
	 * Constructor -- contains ALL connection/networking logic
	 */
	public T3Server() 
	{	
		final char A = 'A';
		
		clients = new T3Client[clientCount];
		for (int i = 0; i < clients.length; i++)
			clients[i] = new T3Client(BOARD_SIZE, BOARD_DIM, (char)(A+i));
		
	}
	
	
	
	/**
	 * This method initializes the game/board components.
	 * It also can be called to clear an existing game.
	 */
	void reset() {
		gameOver = false;
		
		char[] b = new char[ (int) Math.pow(BOARD_SIZE, BOARD_DIM)];
		for (int i = 0; i < b.length; i++)
			b[i] = EMPTY_SPACE;
		
		board = b;
	}
	
	
	
	/**
	 * This method represents the main program execution.
	 */
	public void run() 
	{
		/* Initialize game variables */
		reset();
		
		/* Gameplay loop */
		while (!gameOver)
			turn();
		
		/* End-of-game actions */
		for (int cl = 0; cl < clients.length; cl++)
			if ( clients[cl].prompt("Would you like to play again?") == 0) {
				clients[cl].print("Thanks for playing!");
				clientCount--;
				clients[cl] = null;
			}
		
		if (clientCount > 0)
			run();
	}
	
	
	
	/**
	 * This method represents one turn cycle. It is looped during regular gameplay.
	 */
	private void turn()
	{
		for (int cl = 0; cl < clients.length; cl++)
		{	
			char icon = clients[cl].icon;
			
			/* If human player, show board */
			clients[cl].print(drawBoard());
			
			/* Get player's move */
			int move = clients[cl].prompt("Enter a move.");
			place(move, icon);
			
			/* Check if player won */
			if ( checkforWin(icon)) {
				gameOver = true;
				clients[cl].print(drawBoard() + WIN_MESSAGE);
				for (int cl2 = 0; cl2 < clients.length; cl2++)
					if (clients[cl] != clients[cl2])
						clients[cl2].print(drawBoard() + LOSS_MESSAGE);
				return;
			}
			
			/* Check for tie */
			if ( checkforTie()) {
				gameOver = true;
				for (int cl2 = 0; cl2 < clients.length; cl2++)
					clients[cl2].print(drawBoard() + TIE_MESSAGE);
				return;
			}
		}
		return;
	}
	
	
	
	/**
	 * 
	 * @param icon
	 * @return
	 */
	boolean checkforWin(char icon)
	{
		final int L = BOARD_SIZE - 1;
		
		/* Iterate across 2d boards */
		for (int n = 0; n < board.length; n += (BOARD_SIZE * BOARD_SIZE))
		{
			boolean wincondExists = true;
			
			/* Iterate across rows */
			for (int row = 0; row < BOARD_SIZE; row += BOARD_SIZE)
			{
				/* Check current row */
				wincondExists = true;
				for (int k = 0; k < BOARD_SIZE; k++){
					if (board[n + row + k] != icon)
						wincondExists = false;
				}
				if (wincondExists) {
					System.out.println("Win condition found: row " + row);
					return true;
				}
			}
			
			/* Iterate across columns */
			for (int col = 0; col < BOARD_SIZE; col++)
			{
				/* Check current column */
				wincondExists = true;
				for (int k = 0; k < BOARD_SIZE; k++) {
					if (board[n + (BOARD_SIZE * k) + col] != icon)
						wincondExists = false;
				}
				if (wincondExists) {
					System.out.println("Win condition found: column " + col);
					return true;
				}
			}
			
			/* Diagonals */
			wincondExists = true;
			for (int k = 0; k < BOARD_SIZE; k++) {
				if (board[n + (BOARD_SIZE * k) + k] != icon)
					wincondExists = false;
			}
			if (wincondExists){
				System.out.println("Win condition found: diagonal");
				return true;
			}
			
			wincondExists = true;
			for (int k = 0; k < BOARD_SIZE; k++) {
				if (board[n + (BOARD_SIZE * k) + L - k] != icon)
					wincondExists = false;
			}
			if (wincondExists){
				System.out.println("Win condition found: diagonal");
				return true;
			}
		}
		
		return false;
	}
	
	
	
	/**
	 * 
	 * @return
	 */
	boolean checkforTie() 
	{
		return false;
	}
	
	
	
	
	/**
	 * Place a tile at the provided board space.
	 * @param move
	 * @param icon
	 * @return
	 */
	int place(final int move, final char icon)
	{
		if (move < 0 || move >= board.length)
			return 1;
		if (board[move] != EMPTY_SPACE)
			return 1;
		board[move] = icon;
		return 0;
	}
	
	
	
	
	/**
	 * Creates a string representation of this game's gameboard for display.
	 * @return
	 */
	String drawBoard() 
	{
		String out = "";
		
		int spacingOffset = 4 * board.length / (int) Math.pow(BOARD_SIZE, 3);
		
		for (int i = 0; i < board.length; i++)
		{	
			/* N-dimensional spacing offset */
			for (int n = 2; i > 0 && n < BOARD_DIM; n++)
				if (i % Math.pow(BOARD_SIZE, n) == 0)
					spacingOffset += NDimSpacingOffset(n);
			
			/* Check for new line */
			if (i % this.BOARD_SIZE == 0) 
			{
				if (i > 0) out += " | ";	// Trailing board edge
				out += "\n";
				for (int j = 0; j < spacingOffset; j++)
					out +=" ";

				out += "|";				// Leading board edge
			}
			out += " " + board[i];
		}
		out += " |\n";
		
		return out;
	}
	
	
	/**
	 * Calculates the amount of spacing offset needed by the drawBoard() method.
	 * @param dim
	 * @return
	 */
	private int NDimSpacingOffset(int dim)
	{
		if (dim < 2)
			return 0;
		if (dim == 2)
			return 3 + (2 * BOARD_SIZE);
		if (dim % 2 == 0)
			return NDimSpacingOffset(dim-2) - NDimSpacingOffset(dim-1);
		else
			return -4 - (BOARD_SIZE * NDimSpacingOffset(dim-1));
	}
	
	
	public static void main(String[] args)
	{
		T3Server s = new T3Server();
		s.run();
	}
}
