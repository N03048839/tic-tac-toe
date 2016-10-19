package tictactoe;

public class T3Client 
{
	private static final int BOARD_SIZE = 2;
	private static final int BOARD_DIM = 6;
	

	public T3Client () {
		char[] b = new char[ (int) Math.pow(BOARD_SIZE, BOARD_DIM)];
		for (int i = 0; i < b.length; i++)
			b[i] = '-';
		
		drawBoard(b);
	}
	
	/**
	 * Gets a move from the player.
	 * @return board index corresponding to the player's move
	 */
	int getMove() 
	{
		
		/* Get player's move */
		
		return 0;
	}
	
	
	
	
	
	void drawBoard(char[] board) 
	{
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
				if (i > 0) System.out.print(" | ");	// Trailing board edge
				System.out.print("\n");
				for (int j = 0; j < spacingOffset; j++)
					System.out.print(" ");

				System.out.print("|");				// Leading board edge
			}
			System.out.print(" " + board[i]);
		}
		System.out.print(" |\n");
	}
	
	
	private int NDimSpacingOffset(int dim)
	{
		if (dim < 2)
			return 0;
		if (dim == 2)
			return 4 + (2 * BOARD_SIZE);
		if (dim % 2 == 0)
			return NDimSpacingOffset(dim-2) - NDimSpacingOffset(dim-1);
		else
			return -4 - (BOARD_SIZE * NDimSpacingOffset(dim-1));
	}
	
	
	public static void main(String[] args)
	{
		
		
		new T3Client();
	}
}
