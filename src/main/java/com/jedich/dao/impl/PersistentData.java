package com.jedich.dao.impl;

import com.jedich.models.House;
import com.jedich.models.King;
import com.jedich.models.ClaimedChunk;
import com.jedich.models.War;

import java.util.HashMap;

public class PersistentData {

	private static PersistentData instance = null;
	//key Chunk.toString()
	protected HashMap<String, ClaimedChunk> claimData = new HashMap<>();
	//key UUID.toString()
	protected HashMap<String, King> kingData = new HashMap<>();
	//key bedBlock.toString()
	protected HashMap<String, House> houseData = new HashMap<>();
	//key UUID.toString()
	protected HashMap<String, War> wars = new HashMap<>();

	public static PersistentData getInstance() {
		if (instance == null) {
			instance = new PersistentData();
		}
		return instance;
	}
}
