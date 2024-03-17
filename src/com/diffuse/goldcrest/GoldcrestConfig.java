package com.diffuse.goldcrest;

import static java.util.concurrent.TimeUnit.SECONDS;

public class GoldcrestConfig {
	
	public String poolName;
   	public long connectionTimeout;  
	public long validationTimeout;
	public int maxPoolSize;
	
	public GoldcrestConfig(){
		this.poolName = "ColdcrestCP";
		this.maxPoolSize = 8;
	    	connectionTimeout = SECONDS.toMillis(30);
	    	validationTimeout = SECONDS.toMillis(5);
	}

}
