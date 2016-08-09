package dnp3;
import com.automatak.dnp3.*;

public class DataHandler implements DataObserver {
    private Dnp3Connection dnp3Con;
    private long counter [] = new long [5];
    private boolean binaryoutputstatus []= new boolean [5];
    private boolean reading, newreadI, newreadB;



    public DataHandler(Dnp3Connection newdnp3) {
      dnp3Con = newdnp3;  
    }

    public void start()
    {
      reading = true;
      newreadI = true;
      newreadB = true;
    }

    public void update(BinaryInput meas, long index)
    {
    }

    public void update(AnalogInput meas, long index)
    {
    }

    public void update(Counter meas, long index)
    {
        counter[(int)index] = meas.getValue();
    }

    public void update(BinaryOutputStatus meas, long index)
    {
        binaryoutputstatus[(int)index] = meas.getValue();
    }

    public void update(AnalogOutputStatus meas, long index)
    {
    }

    public void end()
    {
       reading = false;
    }

    public boolean requestBoolResponse(int index) {
     newreadB = false;
     dnp3Con.getMaster().performIntegrityScan();
     while(!newreadB) waitResponse(1);
     return binaryoutputstatus[index];
    }

    public int requestIntResponse(int index) {
      newreadI = false;
      dnp3Con.getMaster().performIntegrityScan();
      while(!newreadI) waitResponse(1);
      return (int)counter[index];
    }

    private void waitResponse(long time) {
      try {
          Thread.sleep(time);                
      } catch(InterruptedException ex) {
          Thread.currentThread().interrupt();
      }
    }

}
