package tld.sofugames.data;

public class DaoFactory {
	public KingDao getKings() {
		return new KingDao();
	}

	public ClaimDao getClaims() {
		return new ClaimDao();
	}
	public WarDao getWars() {
		return new WarDao();
	}
	public HouseDao getHouses() {
		return new HouseDao();
	}
}
