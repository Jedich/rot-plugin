package com.jedich.models;

import com.jedich.rot.DeferredEventType;

import java.util.Date;
import java.util.UUID;

public class DeferredEvent {

	private int id;
	private UUID affectsUUID;
	private Date IssuedAt;
	private DeferredEventType type;

	public DeferredEvent(int id, UUID affectsUUID, Date issuedAt, DeferredEventType type) {
		this.id = id;
		this.affectsUUID = affectsUUID;
		IssuedAt = issuedAt;
		this.type = type;
	}

	public DeferredEvent(UUID affectsUUID, Date issuedAt, DeferredEventType type) {
		this.affectsUUID = affectsUUID;
		IssuedAt = issuedAt;
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public UUID getAffectsUUID() {
		return affectsUUID;
	}

	public void setAffectsUUID(UUID affectsUUID) {
		this.affectsUUID = affectsUUID;
	}

	public Date getIssuedAt() {
		return IssuedAt;
	}

	public void setIssuedAt(Date issuedAt) {
		IssuedAt = issuedAt;
	}

	public DeferredEventType getType() {
		return type;
	}

	public void setType(DeferredEventType type) {
		this.type = type;
	}
}
