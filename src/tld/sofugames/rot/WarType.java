package tld.sofugames.rot;

public class WarType {
	private final String name;
	private final float targetScore;

	public WarType(String name, float targetScore) {
		this.name = name;
		this.targetScore = targetScore;
	}

	public String getName() {
		return name;
	}

	public float getTargetScore() {
		return targetScore;
	}

	public static WarType[] types = new WarType[] {
		new WarType("Humiliation", 0.5f),
		new WarType("Raid", 0.4f),
		new WarType("Expansion", 0.6f),
		new WarType("Vassalisation", 1f),
		new WarType("Conquest", 1f)
	};
}


