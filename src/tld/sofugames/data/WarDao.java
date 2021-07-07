package tld.sofugames.data;

import org.bukkit.Bukkit;
import tld.sofugames.models.ClaimedChunk;
import tld.sofugames.models.King;
import tld.sofugames.models.War;
import tld.sofugames.rot.ChunkType;
import tld.sofugames.rot.WarType;

import java.sql.*;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class WarDao extends PersistentData implements Dao<War> {

	Connection connection;

	public WarDao() {
		connection = Data.getInstance().getConnection();
	}
	@Override
	public Optional<War> get(String query) {
		return Optional.of(getAll().get(query));
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
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(War war, Map<String, Object> params) {

	}

	@Override
	public void delete(War war) {

	}
}
