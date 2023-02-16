package tld.sofugames.models;

import org.bukkit.entity.Player;
import tld.sofugames.data.Data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class Advisor extends RotPlayer {
	private final King parentKing;

	public Advisor(Player player, ClaimedChunk homeChunk, King parentKing) {
		super(player, homeChunk);
		this.parentKing = parentKing;
	}

	public King getKing() {
		return parentKing;
	}

	public boolean pushToDb(Connection connection) throws SQLException {
		PreparedStatement pstmt = (PreparedStatement) connection.prepareStatement(
				"INSERT INTO kingdom_helpers VALUES(?, ?)", Statement.RETURN_GENERATED_KEYS);
		pstmt.setString(1, getUuid().toString());
		pstmt.setString(2, parentKing.getUuid().toString());
		pstmt.executeUpdate();
		return true;
	}
}
