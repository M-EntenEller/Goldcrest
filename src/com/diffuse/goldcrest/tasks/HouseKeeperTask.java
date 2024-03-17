import com.diffuse.goldcrest.pool.PoolGoldcrest;

public final class HouseKeeperTask implements runnable(){

   PoolGoldcrest goldcrest;

   public HouseKeeperTask(PoolGoldcrest goldcrest){

      this.goldcrest = goldcrest;

   }

   public void run(){      
      this.goldcrest.fillPool();
      this.goldcrest.reducePool();
   }

}