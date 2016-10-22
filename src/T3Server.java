import java.util.Arrays;



public class T3Server implements Runnable
{
	private static final int BOARD_SIZE = 4;
	private static final int BOARD_DIM = 2;
	private static final char EMPTY_SPACE = '-';
	private static final int MAX_PLAYERS = 10;
	private static final int SPAWN_CNT = 2;
	
	private static final String ADMIN_PASSW = "password";
	private static final String WIN_MESSAGE = "Congratulations! You win!";
	private static final String LOSS_MESSAGE = "You lose";
	private static final String TIE_MESSAGE = "Game over -- Tie";
	private static final int SYSTEM = -1;		// used to indicate non-player messages
	
	
	private T3Client[] clients;
	private int clientCount = 0;
	
	private char[] board;
	
	private boolean gameOver = false;
	
	private int tokenIterator;
	
	
	/**
	 * Constructor -- contains ALL connection/networking logic
	 */
	public T3Server() 
	{	
		final char A = 'A';
		
		clients = new T3Client[MAX_PLAYERS];
		for (int i = 0; i < SPAWN_CNT; i++) {
			clients[i] = new T3Client(i, (char)(A+i));
			clientCount++;
		}
		
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
			if ( clients[cl] != null && 
				clients[cl].promptInt("Would you like to play again?") == 0) {
				removePlayer(clients[cl], false);
			}
		
		if (clientCount > 1)
			run();
	}
	
	
	
	/**
	 * This method represents one turn cycle. It is looped during regular gameplay.
	 */
	private void turn()
	{
		for (int cl = 0; cl < clients.length; cl++)
		{	
			if ( gameOver ) return;		// Needed if game ends before cycling through players
			if (clients[cl] != null)
			{
			
				char icon = clients[cl].icon;
				
				/* If human player, show board */
				clients[cl].print(drawBoard());
				
				
				/* Get player's move */
				boolean validMove = false;
				while (!validMove) 
				{
					tokenIterator = 0;
					String[] tokens = clients[cl].prompt("Enter a move.").split(" ");
					
					/* Test: display tokenized command string */
					if (tokens.length == 0)
						System.out.println("Error tokenizing input string: zero tokens!");
					else {
						System.out.print("\nTokenized input string: " + tokens[0]);
						for (int i = 1; i < tokens.length; i++)
							System.out.print(" + " + tokens[i]);
						System.out.println();
					}
					
					/* Parse and execute any user commands */
					validMove = parseCommand(clients[cl], tokens);
				}
				
				
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
					messageAll(SYSTEM, TIE_MESSAGE);
					return;
				}
			}
		}
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
	 * Gets a move from the player.
	 * 
	 * REGULAR:
	 * enter move
	 * quit game
	 * display board
	 * display commands/help
	 * query game stats
	 * message all players
	 * message one player
	 * request admin priviledge
	 * 
	 * ADMIN:
	 * boot one player
	 * force game over
	 * query player stats
	 * 
	 * @return board index corresponding to the player's move
	 */
	boolean parseCommand(T3Client player, String[] commandList)
	{
		if (tokenIterator >= commandList.length)
			return false;
		
		
		
		/* Case: request admin priviledge */
		if (commandList[tokenIterator].matches("sudo|admin")) 
		{
			tokenIterator++;
			if (player.prompt("[sudo] enter admin password:").matches(ADMIN_PASSW)) {
				player.setAdmin(true);
				player.print("You now have sudo access.");
			}
			return false;
		}
		
		
		
		/* Case: force game over */
		if (commandList[tokenIterator].matches("kill|gameover"))
		{
			tokenIterator++;
			if (!player.isAdmin) {
				if (player.prompt("[kill] enter admin password:").matches(ADMIN_PASSW))
					player.setAdmin(true);
			}
			
			if (tokenIterator < commandList.length
					&& commandList[tokenIterator++].matches("-" + "[Kk]")) { 	// end of command list
				messageAll(SYSTEM, "[gameover: kill] Program terminated");
				System.exit(0);
			}
			
			gameOver = true;
			return true;
		}
		
		
		
		
		/* Case: boot player */
		if (commandList[tokenIterator].matches("boot"))
		{
			tokenIterator++;
			if (tokenIterator >= commandList.length) { 	// end of command list
				player.print("[boot] error: no player specified");
				return false;
			}
			
			if (!player.isAdmin) {
				if (player.prompt("[boot] enter admin password:").matches(ADMIN_PASSW))
					player.setAdmin(true);
			}
			String targetName = commandList[tokenIterator++];
			int target;
			try {
				target = Integer.parseInt(targetName);
			} catch (NumberFormatException e) {
				player.print("[boot] Error: illegal boot target \"" + targetName + "\"");
				return false;
			}
			if (player.promptInt("Booting player: " + clients[target].name() + "\nContinue? (1/0)") == 1) {
				removePlayer(target, true);
				return (player.pid == target || clientCount < 2);
			}
			return false;
		}
		
		
		
		/* Case: player leaves game */
		if (commandList[tokenIterator].matches("quit"))
		{
			tokenIterator++;
			if ( (tokenIterator < commandList.length && commandList[tokenIterator++].matches("-" + "[Ss]") )
					|| player.promptInt("Quit game? (1/0)") == 1)
			{
				removePlayer(player, false);
				return true;
			}
		}
		
		
		/* Case: Player requests a board re-draw */
		if (commandList[tokenIterator].matches("board"))
		{
			tokenIterator++;
			player.print(drawBoard());
			return false;
		}
		
		
		/* Case: Player requests game stats */
		if (commandList[tokenIterator].matches("gamestats"))
		{
			tokenIterator++;
			player.print(gameStats());
			return false;
		}
		
		
		/* Case: Player requests player stats */
		if (commandList[tokenIterator].matches("playerstats"))
		{
			tokenIterator++;
			player.print(playerStats());
			return false;
		}
		
		
		/* Case: Player requests 'help' menu */
		if (commandList[tokenIterator].matches("help|commands"))
		{
			tokenIterator++;
			player.print(helpScreen());
			return false;
		}
		
		
		/* Case: Player sends message */
		if (commandList[tokenIterator].matches("-m|message"))
		{
			tokenIterator++;
			if (commandList.length < 2) {
				player.print("[message] Error: not enough arguments provided");
				return false;
			}
			
			String target = commandList[tokenIterator++];
			if (target.matches("all")) {
				messageAll(player.pid, Arrays.toString(commandList));
				return false;
			}
			else {
				int targetID = Integer.parseInt(target);	// Error here means player target wasn't entered as int
				message(player.pid, targetID, Arrays.toString(commandList));
				return false;
			}
		}
		
		/* Case: player places token */
		if (commandList[tokenIterator].matches("[0-9]+"))
		{
			int target = Integer.parseInt(commandList[tokenIterator++]);
			return place(target, player.icon) == 0;
		}
		
		player.print("Invalid input -- for a list of commands, type \'help\'.");
		return false;
	}
	
	
	/**
	 * Remove a player from the game.
	 * @param player
	 */
	void removePlayer(T3Client player, boolean boot)
	{
		int playerID = player.pid;
		
		clientCount--;
		clients[playerID] = null;
		if (boot)
		{
			player.print("You have been booted.");
			messageAll(SYSTEM, player.name() + " has been removed from the game.");
		}
		else 
		{
			player.print("Thanks for playing!");
			messageAll(SYSTEM, player.name() + " has left the game.");
		}
		
		
		if (clientCount < 2)
			gameOver = true;
	}
	
	
	void removePlayer(int playerID, boolean boot)
	{
		T3Client player = clients[playerID];
		clientCount--;
		clients[playerID] = null;
		if (boot)
		{
			player.print("You have been booted.");
			messageAll(SYSTEM, player.name() + " has been removed from the game.");
		}
		else 
		{
			player.print("Thanks for playing!");
			messageAll(SYSTEM, player.name() + " has left the game.");
		}
		
		
		if (clientCount < 2)
			gameOver = true;
	}
	
	
	
	
	/**
	 * Send a message to one player.
	 */
	void message(T3Client sender, T3Client player, String message)
	{
		String nameStamp = "[" + sender.name + "(private)]: ";
		player.print(nameStamp + message);
	}
	
	
	void message(int senderID, int targetID, String message)
	{
		String nameStamp = "[" + clients[senderID].name + "(private)]: ";
		clients[targetID].print(nameStamp + message);
	}
	
	
	/**
	 * Sends a message to all players.
	 * @param message
	 */
	void messageAll(int senderID, String message)
	{
		if (0 <= senderID && senderID <= clients.length) {
			String nameStamp = "[" + clients[senderID] + "]: ";
			message = nameStamp + message;
		}
		for (int i = 0; i < clients.length; i++)
			if (clients[i] != null)
				clients[i].print(message);
	}
	
	
	void addPlayer()
	{
		int target = -1;
		for (int i = 0; target != -1 || i < clients.length; i++)
			if (clients[i] != null)
				target = i;
		
		if (target == -1) {
			; // error message: maximum number of players already in game
			return;
		}
		
		clients[target] = new T3Client(target, (char) ('A'+target));
		messageAll(SYSTEM, clients[target].name + " has joined the game.");
		clientCount++;
	}
	
	
	/**
	 * Create a string containing information about input commands.
	 */
	String helpScreen()
	{
		return "\n[helpScreen] Displaying commands";
	}
	
	/**
	 * Create a string containing information about the current game.
	 * @return
	 */
	String gameStats()
	{
		return "\n[gameStats] Displaying game stats";
	}
	
	/**
	 * Create a string containing information about the connected players.
	 */
	String playerStats()
	{
		return "\n[playerStats] Displaying player stats";
	}
	
	/**
	 * Create a string representation of this game's gameboard for display.
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
			if (i % BOARD_SIZE == 0) 
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
