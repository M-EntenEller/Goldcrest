package com.diffuse.goldcrest.tasks;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

import com.diffuse.goldcrest.pool.PoolEntry;

public class addPoolEntryTask implements Runnable {
	
	private BlockingQueue<PoolEntry> queue;
	private DataSource dataSource;
	
	public addPoolEntryTask(BlockingQueue<PoolEntry> queue, DataSource dataSource) {
		this.queue = queue;
		this.dataSource = dataSource;
	}

	@Override
	public void run() {
		
		Connection connection;
		
		try {
			connection = this.dataSource.getConnection();
		} catch (SQLException e) {
			//e.printStackTrace();
			return;
		}
		
		try {
			this.queue.offer(new PoolEntry(connection), 5000, TimeUnit.MILLISECONDS);
		} catch (Exception e){
			try(Connection; connection){
			} catch (Exception x){
				// continue with close regardless.
			}
		}
		
	}

}
