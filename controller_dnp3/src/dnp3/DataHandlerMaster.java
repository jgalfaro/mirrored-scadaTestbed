package dnp3;
import com.automatak.dnp3.mock.*;

public class DataHandlerMaster extends FormattingDataObserver {
	private static String toInterface;
    private static boolean reading;
    private Dnp3Connection dnp3Con ;

    public DataHandlerMaster(Dnp3Connection newdnp3) {
    	
    	super(new OutputHandler() {
            
            @Override
            public void handleOutput(String output) {
            //System.out.println("====Ouput: " + output);
                         
            }
        });

        dnp3Con = newdnp3;
  
    }

    
}