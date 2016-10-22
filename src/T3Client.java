import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;


public class T3Client 
{	
	final char icon;
	final int pid;
	
	boolean isAdmin = false;
	String name;
	
	
	protected PrintStream screen = null;
	protected Scanner kb = null;
	
	
	public T3Client(final int playerID, final char icon) 
	{
		this.pid = playerID;
		this.icon = icon;
		
		screen = System.out;
		kb = new Scanner( System.in );
		
		this.name = prompt("Welcome to tic-tac-toe! Enter your name.");
	}
	
	
	/**
	 * Gets a move from the player.
	 * @return board index corresponding to the player's move
	 */
	String prompt(String message)
	{	
		screen.print("\n" + message + "\n> ");
		return kb.nextLine();
	}
	
	int promptInt(String message)
	{
		screen.print("\n" + message + "\n> ");
		String s = kb.nextLine();	// using nextLine() for all input prevents unpleasant problems
		return Integer.parseInt(s);
	}
	
	String name() {
		return this.name;
	}
	
	void setName(String name) {
		this.name = name;
	}
	
	void setAdmin(boolean a) {
		this.isAdmin = a;
	}
	
	boolean isAdmin() {
		return this.isAdmin;
	}
	
	void print(String s)
	{
		screen.print(s);
	}
	
	void close()
	{
		kb.close();
	}
}
