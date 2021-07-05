package tld.sofugames.models;

import org.bukkit.Bukkit;
import tld.sofugames.data.Data;
import tld.sofugames.rot.WarType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;

public class War implements Model {
	private WarType warType;
	private final King atk, def;
	private float score;
	private int battleNum;
	private long startTime;
	public HashSet<King> atkAllies = new HashSet<>(), defAllies = new HashSet<>();

	public War(King atk, King def) {
		this.atk = atk;
		this.def = def;
		score = 0;
		battleNum = 0;
		atk.setAtWar(true);
		def.setAtWar(true);
		atk.setCurrentWar(this);
		def.setCurrentWar(this);
		Bukkit.getWorlds().get(0).getFullTime();
	}

	public War(WarType warType, King atk, King def, float score, int exhaustion, long startTime) {
		this.warType = warType;
		this.atk = atk;
		this.def = def;
		this.score = score;
		this.battleNum = exhaustion;
		this.startTime = startTime;
	}

	public void addAlly(King king, King ally) {
		if(getDef().equals(king)) {
			defAllies.add(ally);
		} else {
			atkAllies.add(ally);
		}
		ally.setAtWar(true);
		ally.setCurrentWar(this);
		ally.setWarAlly(true);
	}

	public void changeWarScore(boolean hasAtkWon) {
		int sign = hasAtkWon ? 1 : -1;
		float value;
		King victim = hasAtkWon ? getDef() : getAtk();
		if(victim.getChunkNumber() < 4) {
			value = 1/4f;
		} else {
			value = 1f/victim.getChunkNumber();
			if(battleNum > 1 && victim.getChunkNumber() > 10) {
				value *= battleNum /2f;
			}
		}
		addScore(value*sign);
	}

	public WarType getWarType() {
		return warType;
	}

	public void setWarType(WarType warType) {
		this.warType = warType;
	}

	public King getAtk() {
		return atk;
	}

	public King getDef() {
		return def;
	}

	public float getScore() {
		return score;
	}

	public boolean signPeace() {
		if(getScore() >= warType.getTargetScore()) {
			warType.getGains().apply(getAtk(), getDef());
			Data.getInstance().destroyWar(getAtk().getUuid().toString());
			return true;
		}
		return false;
	}

	public boolean signWhitePeace(boolean isAgreement) {
		if(isAgreement || battleNum >= 10 || score <= -warType.getTargetScore()) {
			Data.getInstance().destroyWar(getAtk().getUuid().toString());
		}
		return isAgreement || battleNum >= 10 || score <= -warType.getTargetScore();
	}

	public void addScore(float score) {
		this.score += score;
		battleNum++;
		if(this.score > 1) this.score = 1;
		if(this.score < -1) this.score = -1;
		if(battleNum >= 10) {
			getAtk().assignedPlayer.sendMessage("This war is too exhausting. Any side can sign a white peace now.");
			getDef().assignedPlayer.sendMessage("This war is too exhausting. Any side can sign a white peace now.");
		}
		if(getScore() >= warType.getTargetScore()) {
			getAtk().assignedPlayer.sendMessage("§aThe war goal is reached! You can finish it with /war end.");
		} else if(score <= -warType.getTargetScore()) {
			getDef().assignedPlayer.sendMessage("Our enemies totally lost! You can sign a white peace now.");
		}
	}

	@Override
	public boolean pushToDb(Connection connection) throws SQLException {
		PreparedStatement pstmt = (PreparedStatement) connection.prepareStatement(
				"INSERT INTO wars VALUES(?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
		pstmt.setString(1, atk.getUuid().toString());
		pstmt.setString(2, def.getUuid().toString());
		pstmt.setFloat(3, score);
		pstmt.setInt(4, battleNum);
		pstmt.setLong(5, startTime);
		pstmt.executeUpdate();
		return true;
	}

	@Override
	public boolean readFromDb(Connection connection) throws SQLException {
		return true;
	}
}
