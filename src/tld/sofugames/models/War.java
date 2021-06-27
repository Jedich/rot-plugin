package tld.sofugames.models;

import tld.sofugames.rot.WarType;

public class War {
	private WarType warType;
	private King atk, def;
	private float score;

	public War(King atk, King def) {
		this.atk = atk;
		this.def = def;
		score = 0;
		atk.setAtWar(true);
		def.setAtWar(true);
		atk.setCurrentWar(this);
		def.setCurrentWar(this);
	}

	public void changeWarScore(boolean isAtk) {}

	public WarType getWarType() {
		return warType;
	}

	public void setWarType(WarType warType) {
		this.warType = warType;
	}

	public King getAtk() {
		return atk;
	}

	public void setAtk(King atk) {
		this.atk = atk;
	}

	public King getDef() {
		return def;
	}

	public void setDef(King def) {
		this.def = def;
	}

	public float getScore() {
		return score;
	}

	public void addScore(float score) {
		this.score += score;
	}
}
