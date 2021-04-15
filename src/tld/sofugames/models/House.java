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
	public float income;

	public House(int id, UUID owner, String bedBlock) {
		this.id = id;
		this.owner = owner;
		this.bedBlock = bedBlock;
	}

	public House(int id, UUID owner, String bedBlock, int area, int benefits, float income) {
		this.id = id;
		this.owner = owner;
		this.bedBlock = bedBlock;
		this.area = area;
		this.benefits = benefits;
		this.income = income;
	}

	private void calculateIncome() {
		float a = 1 + benefits * 0.025f;
		income = (float)(4 * a * Math.sin(0.024 * area - 7.9) + 4 * a);
		Data.getInstance().kingData.get(owner.toString()).changeIncome(income);
	}

	@Override
	public boolean pushToDb(Connection connection) throws SQLException {
		calculateIncome();
		PreparedStatement pstmt = (PreparedStatement) connection.prepareStatement(
				"INSERT INTO houses VALUES(?, ?, ?, ?, ?, ?, ?)");
		pstmt.setInt(1, id);
		pstmt.setString(2, owner.toString());
		pstmt.setString(3, bedBlock);
		pstmt.setInt(4, level);
		pstmt.setInt(5, area);
		pstmt.setInt(6, benefits);
		pstmt.setFloat(7, income);
		pstmt.executeUpdate();
		return true;
	}

	@Override
	public boolean readFromDb(Connection connection) throws SQLException {
		return false;
	}

	public void delete(Connection connection) throws SQLException {
		PreparedStatement pstmt = connection.prepareStatement("DELETE FROM houses WHERE id = ?");
		pstmt.setInt(1, id);
		pstmt.executeUpdate();
		Data.getInstance().houseData.remove(bedBlock);
		Data.getInstance().kingData.get(owner.toString()).changeIncome(-income);
	}
}
