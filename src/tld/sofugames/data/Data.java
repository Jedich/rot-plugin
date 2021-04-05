package tld.sofugames.data;

import tld.sofugames.models.ClaimedChunk;
import tld.sofugames.models.King;

import java.sql.Connection;
import java.util.HashMap;

public class Data {
	public static final int basicFee = 2;
	private static Data instance = null;
	public HashMap<String, ClaimedChunk> claimData = new HashMap<>();
	public HashMap<String, King> kingData = new HashMap<>();
	public Connection connection;
	public int lastClaim = 1, lastKing = 1;

	public static Data getInstance() {
		if(instance == null) {
			instance = new Data();
		}
		return instance;
	}

	public int getLastClaim() {
		return lastClaim++;
	}

	public int getLastKing() {
		return lastKing++;
	}
}