package com.diffuse.goldcrest.pool;

import java.sql.Connection;

public class PoolEntry {

	private Connection connection;
	
	public PoolEntry(Connection connection){
		this.connection = connection;
	}
	
	public Connection getConnection() {
		return this.connection;
	}
	
}
