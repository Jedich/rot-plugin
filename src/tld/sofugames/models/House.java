package tld.sofugames.models;

import tld.sofugames.data.Data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.UUID;

public class House implements Model {

	public int id;
	public UUID owner;
	public String bedBlock;
	private int level = 1;
	public int area;
	public int benefits;
	public int income;

	public House(int id, UUID owner, String bedBlock) {
		this.id = id;
		this.owner = owner;
		this.bedBlock = bedBlock;
	}

	public House(int id, UUID owner, String bedBlock, int area, int benefits, int income) {
		this.id = id;
		this.owner = owner;
		this.bedBlock = bedBlock;
		this.area = area;
		this.benefits = benefits;
		this.income = income;
	}

	private void calculateIncome(Connection connection) throws SQLException {
		float a = 1 + benefits * 0.025f;
		income = (int) Math.ceil(4 * a * Math.sin(0.024 * area - 7.9) + 4 * a);
		Data.getInstance().kingData.get(owner.toString()).changeIncome(income, connection);
	}

	@Override
	public boolean pushToDb(Connection connection) throws SQLException {
		calculateIncome(connection);
		PreparedStatement pstmt = (PreparedStatement) connection.prepareStatement(
				"INSERT INTO houses VALUES(?, ?, ?, ?, ?, ?, ?)");
		pstmt.setInt(1, id);
		pstmt.setString(2, owner.toString());
		pstmt.setString(3, bedBlock);
		pstmt.setInt(4, level);
		pstmt.setInt(5, area);
		pstmt.setInt(6, benefits);
		pstmt.setInt(7, income);
		pstmt.executeUpdate();
		return true;
	}

	public void delete(Connection connection) throws SQLException {
		PreparedStatement pstmt = connection.prepareStatement("DELETE FROM houses WHERE id = ?");
		pstmt.setInt(1, id);
		System.out.println("delete id " + id);
		pstmt.executeUpdate();
		Data.getInstance().houseData.remove(bedBlock);
		Data.getInstance().kingData.get(owner.toString()).changeIncome(-income, connection);
	}
}
