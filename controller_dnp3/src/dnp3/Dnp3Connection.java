package dnp3;

//Dnp3 libraries
import com.automatak.dnp3.*;
import com.automatak.dnp3.impl.DNP3ManagerFactory;
import com.automatak.dnp3.mock.PrintingDataObserver;
import com.automatak.dnp3.mock.PrintingLogSubscriber;

//Network addresses handling
import com.google.common.net.InetAddresses;
import java.net.*;

public class Dnp3Connection {
	private String slaveIp;
	private int port;
	private boolean connected;

	private DNP3Manager manager;
	private Master master;
	private CommandProcessor processor;
	private DataHandler dh;

	public Dnp3Connection(String newip) {
		slaveIp = newip;
		port = 20000;
		connected = false;
	}

	public Dnp3Connection(String newip, int newport) {
		slaveIp = newip;
		port = newport;
		connected = false;
	}

	public boolean isConnected() {
		return connected;
	}

	public boolean connect() throws Exception {
		manager = DNP3ManagerFactory.createManager(1);
        Channel channel = manager.addTCPClient("client" + port, LogLevel.ERROR, 5000, slaveIp, port);
        
        MasterStackConfig config = new MasterStackConfig();
        dh = new DataHandler(this);
        master = channel.addMaster("master", LogLevel.ERROR, dh, config);
        master.addStateListener(new StackStateListener() {
            @Override
            public void onStateChange(StackState state) {
                if (state.toString().equals("COMMS_UP")){
                	connected = true;
                } else if (state.equals("COMMS_DOWN")) {
                	connected = false;
                }


            }
        });
        processor = master.getCommandProcessor();
        System.out.print("Connecting to " + slaveIp + " in port " + port + "... ");
        while (!connected){
        	Thread.sleep(10);
        };
        System.out.println("Connected");
		return true;
	}

	public void disconnect() {
		manager.shutdown();
	}

	public Master getMaster() {
		return master;
	}

	public CommandProcessor getProcessor() {
		return processor;
	}

	public DataHandler getDataHandler() {
		return dh;
	}

}