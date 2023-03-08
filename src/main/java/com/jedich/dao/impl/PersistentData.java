package com.jedich.dao.impl;

import com.jedich.models.*;

import java.util.HashMap;

public class PersistentData {

	private static PersistentData instance = null;
	//key Chunk.toString()
	protected HashMap<String, ClaimedChunk> claimData = new HashMap<>();
	//key UUID.toString()
	protected HashMap<String, King> kingData = new HashMap<>();
	//key bedBlock.toString()
	protected HashMap<String, House> houseData = new HashMap<>();
	protected HashMap<String, DeferredEvent> deferredEventData = new HashMap<>();
	//key UUID.toString()
	protected HashMap<String, War> wars = new HashMap<>();

	public static PersistentData getInstance() {
		if(instance == null) {
			instance = new PersistentData();
		}
		return instance;
	}
}
