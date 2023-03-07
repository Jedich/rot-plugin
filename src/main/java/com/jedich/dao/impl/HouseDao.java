package com.jedich.dao.impl;

import com.jedich.models.King;
import org.bukkit.Bukkit;
import com.jedich.dao.Dao;
import com.jedich.data.Data;
import com.jedich.models.House;

import java.sql.*;
import java.util.Map;
import java.util.UUID;

public class HouseDao extends PersistentData implements Dao<House> {
	Connection connection;

	public HouseDao() {
		connection = Data.getInstance().getConnection();
	}

	@Override
	public Map<String, House> getAll() {
		try {
			if(getInstance().houseData.size() == 0) {
				PreparedStatement stmt = (PreparedStatement) connection.prepareStatement("SELECT * FROM houses");
				ResultSet results = stmt.executeQuery();
				while(results.next()) {
					House newHouse = new House(results.getInt("id"),
							UUID.fromString(results.getString("owner")),
							results.getString("bed_block"),
							null,
							results.getInt("area"),
							results.getInt("benefits"),
							results.getFloat("income")
					);
					getInstance().houseData.put(results.getString("bed_block"), newHouse);
					new DaoFactory().getKings().get(results.getString("owner"))
							.orElse(new King()).changeIncome(results.getFloat("income"));
				}
				stmt = (PreparedStatement) connection.prepareStatement("SELECT * FROM house_blocks");
				results = stmt.executeQuery();
				while(results.next()) {
					get(results.getString("name")).orElse(new House()).bedBlock =
							Bukkit.getServer().getWorlds().get(0).getBlockAt(results.getInt("x"),
									results.getInt("y"), results.getInt("z"));
				}
			}
			return getInstance().houseData;
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public int getHouseNumber(UUID owner) {
		int houseNumber = 0;
		try {
			PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) AS 'count' FROM houses WHERE owner = ?");
			stmt.setString(1, owner.toString());
			ResultSet results = stmt.executeQuery();
			while(results.next()) {
				houseNumber = results.getInt("count");
			}
			return houseNumber;
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public void save(House house) {
		try {
			PreparedStatement pstmt = (PreparedStatement) connection.prepareStatement(
					"INSERT INTO houses(owner, bed_block, level, area, benefits, income) VALUES(?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, house.owner.toString());
			pstmt.setString(2, house.bedBlockId);
			pstmt.setInt(3, house.level);
			pstmt.setInt(4, house.area);
			pstmt.setInt(5, house.benefits);
			pstmt.setFloat(6, house.income);
			pstmt.executeUpdate();
			ResultSet retrievedId = pstmt.getGeneratedKeys();
			if(retrievedId.next()) {
				house.setId(retrievedId.getInt(1));
			}
			System.out.println(pstmt.toString());

			pstmt = (PreparedStatement) connection.prepareStatement(
					"INSERT INTO house_blocks(name, x, y, z) VALUES(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, house.bedBlockId);
			pstmt.setInt(2, house.bedBlock.getX());
			pstmt.setInt(3, house.bedBlock.getY());
			pstmt.setInt(4, house.bedBlock.getZ());
			pstmt.executeUpdate();
		} catch(SQLException e) {
			e.printStackTrace();
		}
		getInstance().houseData.put(house.bedBlockId, house);
	}

	@Override
	public void update(House house, Map<String, Object> params) {

	}

	@Override
	public void delete(House house) {
		try {
			PreparedStatement pstmt = connection.prepareStatement("DELETE FROM houses WHERE id = ?");
			pstmt.setInt(1, house.getId());
			pstmt.executeUpdate();
			pstmt = connection.prepareStatement("DELETE FROM house_blocks WHERE name = ?");
			pstmt.setString(1, house.bedBlockId);
			pstmt.executeUpdate();
			new DaoFactory().getKings().get(house.owner.toString()).orElse(new King()).changeIncome(-house.income);
			getInstance().houseData.remove(house.bedBlockId);
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
}
