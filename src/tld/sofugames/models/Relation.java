package tld.sofugames.models;

public class Relation {
	private King king1, king2;
	private float relation;

	public Relation(King king1, King king2) {
		this.king1 = king1;
		this.king2 = king2;
	}

	public Relation(King king1, King king2, float relation) {
		this.king1 = king1;
		this.king2 = king2;
		this.relation = relation;
	}

	public King getKing1() {
		return king1;
	}

	public King getKing2() {
		return king2;
	}

	public float getRelation() {
		return relation;
	}
}
