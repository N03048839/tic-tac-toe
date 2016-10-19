package tictactoe;

public class T3Server {
	
	private T3Client client;
	
	private char[] board;
	
	private int turn()
	{
		/* If human player, show board */
		client.drawBoard(this.board);
		
		/* Get player's move */
		int move = client.getMove();
		
		/* Check if player won */
		
		
		/* Check for tie */
		
		return 0;
	}
}
