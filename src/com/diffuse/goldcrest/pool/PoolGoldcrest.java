package com.diffuse.goldcrest.pool;

import java.util.concurrent.BlockingQueue;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import com.diffuse.goldcrest.GoldcrestConfig;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import com.diffuse.goldcrest.tasks.ReleaseConnectionTask;
import com.diffuse.goldcrest.tasks.addPoolEntryTask;

public class PoolGoldcrest extends PoolBase {
	
	private BlockingQueue<PoolEntry> queue;
	private final int QUEUE_TIMEOUT = 2000;
	private final int IDDLE_TIMEOUT = 0;
	private AtomicLong lastTouch = new AtomicLong(System.currentTimeMillis()); 
	private ThreadPoolExecutor recycleConnectionExecutor;
	private ThreadPoolExecutor addPoolEntryExecutor;
	private ThreadPoolExecutor deletePoolEntryExecutor;
	private final ScheduledExecutorService houseKeepingExecutorService;
	private int maxPoolSize;
	private int minIdleConnections;
	private AtomicInteger connectionsOutstanding;
	
	public PoolGoldcrest(final GoldcrestConfig config) {

		super(config);
		this.queue = new ArrayBlockingQueue<PoolEntry>(config.maxPoolSize);
		this.recycleConnectionExecutor = Executors.newFixedThreadPool(1);
		this.addPoolEntryExecutor = Executors.newFixedThreadPool(1);
		this.deletePoolEntryExecutor = Executors.newFixedThreadPool(1);

	}

	@Override
	public void recycleConnection(final Connection connection) {
		
		recycleConnectionExecutor.submit(new RecycleConnectionTask(this.queue, connection));
		this.connectionsOutstanding.getAndDecrement();
		
	}

	@Override
	public Connection takeConnection() throws SQLException {
		
		return takeConnection(true);

	}

	public Connection takeConnection(boolean shouldTouch) throws SQLException{		

		try {
			PoolEntry entry = this.queue.poll(QUEUE_TIMEOUT, TimeUnit.MILLISECONDS);

			if (shouldTouch){
				touch(); 	
				this.connectionsOutstanding.getAndIncrement();		
			}
			return entry.getConnection();
		} catch (InterruptedException e) {
			throw new SQLException("Interrupted during connection acquisition");
		}
	}

	private void touch(){

		this.lastTouch.set(System.currentTimeMillis());

	}
	
	private int getTotalConnections() {
		
		return this.connectionsOutstanding.get() + this.queue.size();
		
	}

	private boolean alreadyFilling(){
		
		return addPoolEntryExecutor.getQueue().size() > 0;

	}

	private boolean alreadyReducing(){

		return this.deletePoolEntryExecutor.getQueue().size() > 0;

	}
	
	/**
	* Fill pool up to minimum idle connections.
	*/
	public synchronized void fillPool() {
		
		if (alreadyFilling()){
			return;
		}

		boolean shouldAdd = getTotalConnections() < this.maxPoolSize && this.queue.size() < this.minIdleConnections;		
		
		if (shouldAdd)
		{
			int numToAdd = Math.min(this.minIdleConnections - this.queue.size(), this.maxPoolSize - this.getTotalConnections); 
			for (int i=0; i<numToAdd; i++) 
			{
				addPoolEntryExecutor.submit(new addPoolEntryTask(this.queue, this.dataSource));
			}
		}
		
	}

	/***
	* Reduce pool down to min idle connections if idle timeout is exceeded.
	*/
	public synchronized void reducePool(){

		if (alreadyReducing()){
			return; 
		}

		int maxToRemove = this.queue.size() - this.minIdleConnections;
		
		for (int i=0; i<maxToRemove; i++)
		{
			if (System.currentTimeMillis() - this.lastTouch.get() > this.IDDLE_TIMEOUT;){ 
				this.deletePoolEntryExecutor.submit(new DeletePoolEntryTask(this));
			}
		}

	}
	

}
