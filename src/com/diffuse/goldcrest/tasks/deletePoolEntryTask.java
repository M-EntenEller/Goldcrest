import com.diffuse.goldcrest.pool.PoolGoldcrest;
import com.diffuse.goldcrest.pool.PoolEntry;
import java.concurrent.BlockingQueue;
import java.sql.Connection;

public class DeletePoolEntryTask implements runnable{

    PoolGoldcrest goldcrest;

    public DeletePoolEntryTask(PoolGoldcrest goldcrest){
        this.goldcrest = goldcrest;
    }

    public void run(){

        try {
            Connection connection = this.goldcrest.takeConnection(false);
            try (Connection; connection){                
            } catch{
                //continue with close regardless.
            }
        } catch (Exception e){
        }
        
    }

}