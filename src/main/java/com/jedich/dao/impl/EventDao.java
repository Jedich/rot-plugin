package com.jedich.dao.impl;

import com.jedich.dao.Dao;
import com.jedich.data.Data;
import com.jedich.models.DeferredEvent;
import com.jedich.rot.DeferredEventType;

import java.sql.*;
import java.util.Map;
import java.util.UUID;

public class EventDao extends PersistentData implements Dao<DeferredEvent> {

	Connection connection;

	public EventDao() {
		connection = Data.getInstance().getConnection();
	}

	@Override
	public Map<String, DeferredEvent> getAll() {
		try {
			if(getInstance().deferredEventData.size() == 0) {
				PreparedStatement stmt = connection.prepareStatement("SELECT * FROM deferred_events");
				ResultSet results = stmt.executeQuery();
				while(results.next()) {
					DeferredEvent e = new DeferredEvent(
							results.getInt("id"),
							UUID.fromString(results.getString("affects_uuid")),
							results.getDate("issued_at"),
							DeferredEventType.valueOf(results.getString("type"))
					);
					getInstance().deferredEventData.put(e.getAffectsUUID().toString(), e);
				}
			}
			return getInstance().deferredEventData;
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void save(DeferredEvent event) {
		try {
			PreparedStatement stmt = connection.prepareStatement(
					"INSERT INTO deferred_events(affects_uuid, issued_at, type) VALUES(?, ?, ?)");
			stmt.setString(1, event.getAffectsUUID().toString());
			stmt.setDate(2, new java.sql.Date(event.getIssuedAt().getTime()));
			stmt.setString(3, event.getType().toString());
			stmt.executeUpdate();
			ResultSet retrievedId = stmt.getGeneratedKeys();
			if(retrievedId.next()) {
				event.setId(retrievedId.getInt(1));
			}
			System.out.println(stmt.toString());
		} catch(SQLException e) {
			e.printStackTrace();
		}
		getInstance().deferredEventData.put(event.getAffectsUUID().toString(), event);
	}

	@Override
	public void update(DeferredEvent deferredEvent, Map<String, Object> params) {

	}

	@Override
	public void delete(DeferredEvent event) {
		try {
			PreparedStatement pstmt = connection.prepareStatement("DELETE FROM deferred_events WHERE id = ?");
			pstmt.setInt(1, event.getId());
			pstmt.executeUpdate();
			getInstance().deferredEventData.remove(event.getAffectsUUID().toString());
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
}
