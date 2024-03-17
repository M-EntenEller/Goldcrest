package com.diffuse.goldcrest.tasks;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.sql.Connection;
import com.diffuse.goldcrest.pool.PoolEntry;

public class RecycleConnectionTask implements Runnable{
	
	private Connection connection;
	private BlockingQueue bq;
	
	public RecycleConnectionTask(BlockingQueue bq, Connection connection){
		this.bq = bq;
		this.connection = connection;
	}
	
	public void run() {
		
		try {
			
			this.bq.offer(new PoolEntry(connection), 5000, TimeUnit.MILLISECONDS);
			
		} catch (InterruptedException e) {
			try(Connection; connection){
			} catch (Exception x){
				// continue with close regardless.
			}
		}
	}

}
