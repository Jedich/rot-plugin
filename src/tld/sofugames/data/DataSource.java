package tld.sofugames.data;

import com.zaxxer.hikari.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DataSource {

	private static HikariConfig config = new HikariConfig();
	private static HikariDataSource ds;

	static {
		config.setJdbcUrl("jdbc:sqlite:plugins/RoT-Reloaded/rotr.db");
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		ds = new HikariDataSource(config);

		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(DataSource.class.getResourceAsStream("/database/init.sql")));
		StringBuilder lines = new StringBuilder();
		String strLines;
		String line;
		int lineNum = 0;
		try {
			while((line = bufferedReader.readLine()) != null) {
				if(lineNum == 0) {
					if(line.length() < 15) {
						continue;
					}
				}
				lines.append(line);
				lineNum++;
			}
			strLines = lines.toString();
			Statement statement = ds.getConnection().createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			statement.executeUpdate(strLines);
		} catch(IOException | SQLException e) {

		}
	}

	private DataSource() {
	}

	public static Connection getConnection() throws SQLException {
		return ds.getConnection();
	}
}