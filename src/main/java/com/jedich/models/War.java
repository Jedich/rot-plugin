package com.jedich.models;

import com.jedich.rot.WarType;
import org.bukkit.Bukkit;
import com.jedich.dao.impl.DaoFactory;

import java.util.HashSet;

public class War {
	private int id;
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

	public War(int id, WarType warType, King atk, King def, float score, float exhaustion, long startTime) {
		this.id = id;
		this.warType = warType;
		this.atk = atk;
		this.def = def;
		this.score = score;
		this.battleNum = (int)exhaustion;
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

	public float getExhaustion() {
		return battleNum;
	}

	public long getStartTime() {
		return startTime;
	}

	public boolean signPeace() {
		if(getScore() >= warType.getTargetScore()) {
			warType.getGains().apply(getAtk(), getDef());
			new DaoFactory().getWars().delete(this);
			return true;
		}
		return false;
	}

	public boolean signWhitePeace(boolean isAgreement) {
		if(isAgreement || battleNum >= 10 || score <= -warType.getTargetScore()) {
			new DaoFactory().getWars().delete(this);
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

	public void updateWarState(boolean isOn) {
		War ongoingWar = isOn ? this : null;
		getAtk().setAtWar(isOn);
		getDef().setAtWar(isOn);
		getAtk().setCurrentWar(ongoingWar);
		getDef().setCurrentWar(ongoingWar);
		for(King ally : atkAllies) {
			ally.setCurrentWar(ongoingWar);
			ally.setAtWar(isOn);
			ally.setWarAlly(isOn);
		}
		for(King ally : defAllies) {
			ally.setCurrentWar(ongoingWar);
			ally.setAtWar(isOn);
			ally.setWarAlly(isOn);
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return warType.getName() + " war: " + getAtk().assignedPlayer.getName() + " vs. " +  getDef().assignedPlayer.getName();
	}
}
