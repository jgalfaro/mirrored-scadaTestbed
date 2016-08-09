package rtu;

import com.automatak.dnp3.*;
import com.automatak.dnp3.impl.DNP3ManagerFactory;	
import com.automatak.dnp3.mock.PrintingLogSubscriber;
import com.automatak.dnp3.mock.SuccessCommandHandler;

public class Dnp3Connection {
	private String masterIp = "192.168.2.2";
	private int port = 20000;
	public DataObserver data;
	protected DNP3Manager manager;
	private Channel channel;
	private DatabaseConfig db;
    private PlcCommandHandler plcch;

	public Dnp3Connection() {
        System.out.println("New dnp3, port: " + port);
		manager = DNP3ManagerFactory.createManager(1);
		channel = manager.addTCPServer("client", LogLevel.INFO, 5000, masterIp, port);
		channel.addStateListener(new ChannelStateListener() {
            @Override
            public void onStateChange(ChannelState state) {
                System.out.println("server state: " + state);
            }
        });
       	db = new DatabaseConfig(0,0,3,3,0);
        OutstationStackConfig config = new OutstationStackConfig(db);
        config.outstationConfig.disableUnsol = true;
		config.outstationConfig.allowTimeSync = false;
        config.outstationConfig.staticAnalogInput = StaticAnalogResponse.GROUP30_VAR1;
        PlcMasterCommandHandler plcch = PlcMasterCommandHandler.getInstance();
        Outstation outstation = channel.addOutstation("outstation", LogLevel.INTERPRET, plcch, config);
        outstation.addStateListener(new StackStateListener() {
            @Override
            public void onStateChange(StackState state) {
                System.out.println("Outstation state: " + state + " from port " + port);
            }

	    	
        });
        data = outstation.getDataObserver();

	}

	public Dnp3Connection(int newport) {
		port = newport;
        System.out.println("New dnp3, port: " + port);
		manager = DNP3ManagerFactory.createManager(1);
		channel = manager.addTCPServer("client", LogLevel.INFO, 5000, masterIp, port);
		channel.addStateListener(new ChannelStateListener() {
            @Override
            public void onStateChange(ChannelState state) {
                System.out.println("server state: " + state);
            }
        });
       	db = new DatabaseConfig(0,0,3,6,0);
        OutstationStackConfig config = new OutstationStackConfig(db);
        config.outstationConfig.disableUnsol = true;
		config.outstationConfig.allowTimeSync = false;
        config.outstationConfig.staticAnalogInput = StaticAnalogResponse.GROUP30_VAR1;
        plcch = new PlcCommandHandler(port);
        plcch.setDnp3Con(this);
        Outstation outstation = channel.addOutstation("outstation", LogLevel.INTERPRET, plcch, config);
        outstation.addStateListener(new StackStateListener() {
            @Override
            public void onStateChange(StackState state) {
                System.out.println("Outstation state: " + state + " from port " + port);
            }

            
        });
        data = outstation.getDataObserver();

	}

	public Dnp3Connection(String newip,int newport) {
		port = newport;
		masterIp = newip;
		manager = DNP3ManagerFactory.createManager(1);
		channel = manager.addTCPServer("client", LogLevel.INFO, 5000, masterIp, port);
		
       	db = new DatabaseConfig(0,0,3,3,0);
        OutstationStackConfig config = new OutstationStackConfig(db);
        config.outstationConfig.disableUnsol = true;
		config.outstationConfig.allowTimeSync = false;
        config.outstationConfig.staticAnalogInput = StaticAnalogResponse.GROUP30_VAR1;
        plcch = new PlcCommandHandler(port);
        plcch.setDnp3Con(this);
        Outstation outstation = channel.addOutstation("outstation", LogLevel.INTERPRET, plcch, config);
        
        data = outstation.getDataObserver();

	}

    public void setCHModbus(ModBusConnection mbc) {
        plcch.setModbus(mbc);
    }

	public void close() {
        manager.shutdown();
	}

}
