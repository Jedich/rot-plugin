package tld.sofugames.rot;

import com.mysql.jdbc.*;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.command.*;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Main extends JavaPlugin {

	final String username="root"; // Enter in your db username
	final String password=""; // Enter your password for the db
	final String url = "jdbc:mysql://localhost:3306/rotr"; // Enter URL with db name

	//Connection vars
	static Connection connection; //This is the variable we will use to connect to database
	HashMap<String, String> claims = new HashMap<>();

	@Override
	public void onEnable() {

		try { // try catch to get any SQL errors (for example connections errors)
			connection = (Connection) DriverManager.getConnection(url, username, password);
			// with the method getConnection() from DriverManager, we're trying to set
			// the connection's url, username, password to the variables we made earlier and
			// trying to get a connection at the same time. JDBC allows us to do this.
		} catch (SQLException e) { // catching errors
			e.printStackTrace(); // prints out SQLException errors to the console (if any)
		}
		String sql = "SELECT * FROM user_claims"; // Note the question mark as placeholders for input values
		PreparedStatement stmt = null;
		ResultSet results;
		try {
			stmt = (PreparedStatement) connection.prepareStatement(sql);
			results = stmt.executeQuery();
			while (results.next()) {
				claims.put(results.getString("chunk"), results.getString("name"));
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("claim")) {
			Player c = getServer().getPlayer(sender.getName());
			String a = c.getLocation().getChunk().toString();
			sender.sendMessage("AYYE fuck off " + sender.getName() + " whose chunk is " + a);
			claims.put(c.getLocation().getChunk().toString(), sender.getName());
			try {
				sendToDatabase();
			}
			catch (SQLException e) {
				sender.sendMessage("lmao loser " + e.getMessage());
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}

	public void sendToDatabase() throws SQLException {
		connection.setAutoCommit(false);
		PreparedStatement pstmt = null;
		pstmt = (PreparedStatement) connection.prepareStatement(
				"INSERT INTO user_claims VALUES(?, ?)");
		for(Map.Entry<String, String> entry : claims.entrySet()) {
			pstmt.setString(1, entry.getKey());
			pstmt.setString(2, entry.getValue());
			pstmt.addBatch();
		}
		pstmt.executeBatch();
		connection.commit();
		connection.setAutoCommit(true);
	}

	@Override
	public void onDisable() {
		try {
			sendToDatabase();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		// invoke on disable.
		try { // using a try catch to catch connection errors (like wrong sql password...)
			if (connection!=null && !connection.isClosed()){ // checking if connection isn't null to
				// avoid receiving a nullpointer
				connection.close(); // closing the connection field variable.
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
