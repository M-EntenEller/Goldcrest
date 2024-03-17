package com.diffuse.goldcrest.pool;

import javax.sql.DataSource;
import com.diffuse.goldcrest.GoldcrestConfig;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

abstract public class PoolBase {
	
	public final GoldcrestConfig config;	
	protected long connectionTimeout;
	protected long validationTimeout;	
	private int networkTimeout;
	protected DataSource dataSource;
	private Executor netTimeoutExecutor;
	
	public PoolBase(GoldcrestConfig config) {
		
		this.config = config;
		this.connectionTimeout = config.connectionTimeout;
		this.validationTimeout = config.validationTimeout;		
		this.netTimeoutExecutor = Executors.newCachedThreadPool();
		
	}
	
	abstract public void recycleConnection(final Connection connection);
	
	abstract public Connection takeConnection() throws SQLException;
	
	protected boolean isConnectionDead(final Connection connection) {
		
		int validationTimeoutInSeconds = (int) this.validationTimeout / 1000;
		
		try {
			return !connection.isValid(validationTimeoutInSeconds);
		} catch (Exception e) {
		}
		
		return true;
		
	}
	
	protected void quietlyCloseConnection(final Connection connection, final String closureReason) {
		
		if( connection != null) {
			try (Connection; connection){
			} catch (Exception e) {
				// continue with close regardless.
			}
		}
		
	}
	
	// ------------------------------------- private utility functions ------------------------------
	
	/** 
	 * Sets the maximum period a Connection or objects created from the Connection will wait for the database to reply to any one request.
	 * @param connection
	 * @param timeoutMs
	 * @throws SQLException
	 */
	private void setNetworkTimeOut(final Connection connection, final long timeoutMs) throws SQLException {
		
		try {
			connection.setNetworkTimeout(this.netTimeoutExecutor, (int) timeoutMs);
		} catch (Exception e) {
			// JDBC driver does not support set network timeout on connection. --> TO DO: handle that.
		}
		
	}
	
	/**
    * Set the query timeout, if it is supported by the driver.
    *
    * @param statement a statement to set the query timeout on
    * @param timeoutSec the number of seconds before timeout
    */
	
	private void setQueryTimeout(final Statement statement, final int timeoutSec) {
		
		try {
			statement.setQueryTimeout(timeoutSec);
		} catch (Exception e) {
			//.setQueryTimeout() not supported by driver.
		}
		
	}
	
	/* 
	* Note on how to do it manually. ALWAYS use isConnectionDead() instead.
	*/
	private boolean manualIsConnectionValid(final Connection connection) {
		// TO DO:
		// set network timeout to validation timeout.
		// create statement and execute a query.
		// if timeout is not triggered, then query was successfull (in the sense a response from database was received)
		// set back networktimeout. 
		return false;
	}

}
