package tld.sofugames.models;

import java.sql.Connection;
import java.sql.SQLException;

public interface Model {
	public boolean pushToDb(Connection connection) throws SQLException;
	boolean readFromDb(Connection connection) throws SQLException;
}
