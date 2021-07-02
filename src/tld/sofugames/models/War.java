package tld.sofugames.models;

import org.bukkit.Bukkit;
import tld.sofugames.data.Data;
import tld.sofugames.rot.WarType;

public class War {
	private WarType warType;
	private final King atk, def;
	private float score;
	private float battleNum;
	private long startTime;

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
}
