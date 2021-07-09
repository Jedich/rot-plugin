package tld.sofugames.dao.impl;

import tld.sofugames.dao.AbstractWarDao;
import tld.sofugames.dao.Dao;
import tld.sofugames.data.Data;
import tld.sofugames.models.King;
import tld.sofugames.models.War;
import tld.sofugames.rot.WarType;

import java.sql.*;
import java.util.Map;
import java.util.Optional;

public class WarDao extends PersistentData implements Dao<War>, AbstractWarDao {

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
				KingDao kings= new DaoFactory().getKings();
				while(results.next()) {
					PersistentData.getInstance().wars.put(results.getString("name"), new War(
							results.getInt("id"),
							WarType.types[results.getInt("type")],
							kings.get(results.getString("king1")).orElse(null),
							kings.get(results.getString("king2")).orElse(null),
							results.getFloat("score"),
							results.getFloat("kingdom_name"),
							results.getLong("start_time")
					));
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
					"INSERT INTO wars VALUES(?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			pstmt.setInt(1, war.getWarType().getId());
			pstmt.setString(2, war.getAtk().getUuid().toString());
			pstmt.setString(3, war.getDef().getUuid().toString());
			pstmt.setFloat(4, war.getScore());
			pstmt.setFloat(5, war.getExhaustion());
			pstmt.setLong(6, war.getStartTime());
			pstmt.executeUpdate();
			getAll().put(war.getAtk().getUuid().toString(), war);
			ResultSet retrievedId = pstmt.getGeneratedKeys();
			if(retrievedId.next()){
				war.setId(retrievedId.getInt(1));
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(War war, Map<String, Object> params) {

	}

	@Override
	public void delete(War war) {
		deleteSoftly(war);
		try {
			PreparedStatement pstmt = Data.getInstance().getConnection().prepareStatement("DELETE FROM wars WHERE id = ?");
			pstmt.setInt(1, war.getId());
			pstmt.executeUpdate();
			getAll().remove(war.getAtk().getUuid().toString());
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void deleteSoftly(War war) {
		war.getAtk().setAtWar(false);
		war.getDef().setAtWar(false);
		war.getAtk().setCurrentWar(null);
		war.getDef().setCurrentWar(null);
		for(King ally : war.atkAllies) {
			ally.setCurrentWar(null);
			ally.setAtWar(false);
		}
		for(King ally : war.defAllies) {
			ally.setCurrentWar(null);
			ally.setAtWar(false);
			ally.setWarAlly(false);
		}
	}
}
