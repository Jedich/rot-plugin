package tld.sofugames.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class House implements Model {

	public int id;
	public UUID owner;
	private int level = 1;
	public int area;
	public int benefits;
	public int income;

	public House(UUID owner) {
		this.owner = owner;
	}

	private int calculateIncome() {
		return (int) Math.ceil(5.0);
	}

	@Override
	public boolean pushToDb(Connection connection) throws SQLException {
		income = calculateIncome();
		PreparedStatement pstmt = (PreparedStatement) connection.prepareStatement(
				"INSERT INTO houses VALUES(?, ?, ?, ?, ?, ?)");
		pstmt.setInt(1, id);
		pstmt.setString(2, owner.toString());
		pstmt.setInt(3, level);
		pstmt.setInt(4, area);
		pstmt.setInt(5, benefits);
		pstmt.setInt(6, income);
		pstmt.executeUpdate();
		return true;
	}
}
