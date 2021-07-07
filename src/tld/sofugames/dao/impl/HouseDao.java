package tld.sofugames.dao.impl;

import org.bukkit.Bukkit;
import tld.sofugames.dao.Dao;
import tld.sofugames.data.Data;
import tld.sofugames.models.House;
import tld.sofugames.models.King;

import java.sql.*;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class HouseDao extends PersistentData implements Dao<House> {
	Connection connection;

	public HouseDao() {
		connection = Data.getInstance().getConnection();
	}

	@Override
	public Optional<House> get(String query) {
		return Optional.of(getAll().get(query));
	}

	@Override
	public Map<String, House> getAll() {
		try {
			if(PersistentData.getInstance().kingData.size() == 0) {
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
					PersistentData.getInstance().houseData.put(results.getString("bed_block"), newHouse);
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
			return PersistentData.getInstance().houseData;
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void save(House house) {
		try {
			PreparedStatement pstmt = (PreparedStatement) connection.prepareStatement(
					"INSERT INTO houses VALUES(?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, house.owner.toString());
			pstmt.setString(2, house.bedBlockId);
			pstmt.setInt(3, house.level);
			pstmt.setInt(4, house.area);
			pstmt.setInt(5, house.benefits);
			pstmt.setFloat(6, house.income);
			pstmt.executeUpdate();
			ResultSet retrievedId = pstmt.getGeneratedKeys();
			if(retrievedId.next()){
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
		getAll().put(house.bedBlockId, house);
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
			PersistentData.getInstance().houseData.remove(house.bedBlockId);
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
}
