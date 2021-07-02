package tld.sofugames.rot;

import com.mysql.jdbc.Statement;
import tld.sofugames.data.Data;
import tld.sofugames.models.ClaimedChunk;
import tld.sofugames.models.King;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.BiFunction;

public class WarType {
	private final String name;
	private final float targetScore;
	private WarCondition condition;
	private WarCondition gains;

	public WarType(String name, float targetScore, WarCondition condition, WarCondition gains) {
		this.name = name;
		this.targetScore = targetScore;
		this.condition = condition;
		this.gains = gains;
	}

	public WarCondition getCondition() {
		return condition;
	}

	public WarCondition getGains() {
		return gains;
	}

	public String getName() {
		return name;
	}

	public float getTargetScore() {
		return targetScore;
	}

	public static WarType[] types = new WarType[] {
		new WarType("Humiliation", 0.5f, WarType::raidCond, WarType::humiGain),
		new WarType("Raid", 0.4f, WarType::raidCond, WarType::raidGain),
		new WarType("Expansion", 0.6f, WarType::expCond, WarType::expGain),
		new WarType("Vassalisation", 1f, WarType::vassalCond, WarType::humiGain),
		new WarType("Conquest", 1f, WarType::expCond, WarType::humiGain)
	};

	public static boolean raidCond(King atk, King def) {
		return true;
	}

	public static boolean expCond(King atk, King def) {
		return atk.hasClaimsOn.contains(def.getUuid());
	}

	public static boolean vassalCond(King atk, King def) {
		return atk.hasClaimsOn.contains(def.getUuid()) && atk.getChunkNumber() >= def.getChunkNumber() * 1.2f;
	}

	public static boolean humiGain(King atk, King def) {
		return true;
	}

	public static boolean raidGain(King atk, King def) {
		def.setGoldBalance(-200);
		atk.setGoldBalance(200);
		return true;
	}

	public static boolean expGain(King atk, King def) {
		try {
			PreparedStatement pstmt = Data.getInstance().getConnection().prepareStatement("UPDATE user_claims SET owner = ? WHERE id = ?");
			//PreparedStatement pstmt = Data.getInstance().getConnection().prepareStatement("UPDATE user_claims SET owner = ? WHERE id = ?");
			for(ClaimedChunk claim : atk.warClaims) {
				if(claim.owner.equals(def.getUuid())) {
					pstmt.setString(1, atk.getUuid().toString());
					pstmt.setInt(2, claim.id);
					pstmt.addBatch();
				}
			}
			pstmt.executeBatch();
			atk.setChunkNumber(atk.getChunkNumber() + 1);
			def.setChunkNumber(def.getChunkNumber() - 1);
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return true;
	}
}


