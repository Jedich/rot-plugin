package tld.sofugames.dao.impl;

import tld.sofugames.dao.Dao;
import tld.sofugames.data.Data;
import tld.sofugames.models.King;
import tld.sofugames.models.War;
import tld.sofugames.rot.WarSide;
import tld.sofugames.rot.WarType;

import java.sql.*;
import java.util.Map;

public class WarDao extends PersistentData implements Dao<War> {

	Connection connection;

	public WarDao() {
		connection = Data.getInstance().getConnection();
	}

	@Override
	public Map<String, War> getAll() {
		try {
			if(PersistentData.getInstance().wars.size() == 0) {
				PreparedStatement stmt = (PreparedStatement) connection.prepareStatement("SELECT * FROM wars");
				ResultSet results = stmt.executeQuery();
				KingDao kings = new DaoFactory().getKings();
				while(results.next()) {
					King king1 = kings.get(results.getString("king1")).orElse(null);
					King king2 = kings.get(results.getString("king2")).orElse(null);
					War newWar = new War(
							results.getInt("id"),
							WarType.types[results.getInt("type")],
							king1,
							king2,
							results.getFloat("score"),
							results.getFloat("kingdom_name"),
							results.getLong("start_time")
					);
					PersistentData.getInstance().wars.put(results.getString("name"), newWar);
					stmt = connection.prepareStatement("SELECT * FROM war_helpers WHERE atk=?");
					results = stmt.executeQuery();
					while(results.next()) {
						newWar.addAlly(WarSide.valueOf(results.getString("side")) == WarSide.Atk ? king1 : king2,
								kings.get(results.getString("helper")).orElse(null));
					}
					newWar.updateWarState(true);
				}
			}
			return PersistentData.getInstance().wars;
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void save(War war) {
		try {
			PreparedStatement pstmt = (PreparedStatement) connection.prepareStatement(
					"INSERT INTO wars(type, atk, def, score, exhaustion, start_time) VALUES(?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			setParams(war, pstmt);
			pstmt.executeUpdate();
			PersistentData.getInstance().wars.put(war.getAtk().getUuid().toString(), war);
			ResultSet retrievedId = pstmt.getGeneratedKeys();
			if(retrievedId.next()) {
				war.setId(retrievedId.getInt(1));
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(War war, Map<String, Object> params) {

	}

	public void addWarHelper(War war, King helper, WarSide side) {
		try {
			PreparedStatement pstmt = (PreparedStatement) connection.prepareStatement(
					"INSERT INTO war_helpers(atk, helper, side) VALUES(?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, war.getAtk().getUuid().toString());
			pstmt.setString(2, helper.getUuid().toString());
			pstmt.setString(3, side.toString());
			pstmt.executeUpdate();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	private void setParams(War war, PreparedStatement pstmt) throws SQLException {
		pstmt.setInt(1, war.getWarType().getId());
		pstmt.setString(2, war.getAtk().getUuid().toString());
		pstmt.setString(3, war.getDef().getUuid().toString());
		pstmt.setFloat(4, war.getScore());
		pstmt.setFloat(5, war.getExhaustion());
		pstmt.setLong(6, war.getStartTime());
	}

	public void updateAll() {
		try {
			for(War war : PersistentData.getInstance().wars.values()) {
				PreparedStatement pstmt = (PreparedStatement) connection.prepareStatement(
						"UPDATE wars SET type=?, atk=?, def=?, score=?, exhaustion=?, start_time=? WHERE id = ?");
				setParams(war, pstmt);
				pstmt.setInt(7, war.getId());
				pstmt.executeUpdate();
				war.updateWarState(true);
				System.out.println(war.toString() + " was saved.");
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void delete(War war) {
		war.updateWarState(false);
		try {
			PreparedStatement pstmt = Data.getInstance().getConnection().prepareStatement("DELETE FROM wars WHERE id = ?");
			pstmt.setInt(1, war.getId());
			pstmt.executeUpdate();
			getAll().remove(war.getAtk().getUuid().toString());
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}


	public void saveSoftly(War war) {
		PersistentData.getInstance().wars.put(war.getAtk().getUuid().toString(), war);
	}
}
