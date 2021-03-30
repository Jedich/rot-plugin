package tld.sofugames.rot;

import java.sql.Connection;
import java.sql.SQLException;

public interface Model {
	public boolean pushToDb(Connection connection) throws SQLException;
}
