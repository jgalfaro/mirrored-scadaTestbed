package rtu;


import java.net.InetAddress;
import java.net.UnknownHostException;

import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.ModbusDeviceIdentification;
import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.ReadCoilsRequest;
import net.wimpi.modbus.msg.ReadCoilsResponse;
import net.wimpi.modbus.msg.ReadDeviceIdentificationRequest;
import net.wimpi.modbus.msg.ReadDeviceIdentificationResponse;
import net.wimpi.modbus.msg.ReadInputDiscretesRequest;
import net.wimpi.modbus.msg.ReadInputDiscretesResponse;
import net.wimpi.modbus.msg.ReadInputRegistersRequest;
import net.wimpi.modbus.msg.ReadInputRegistersResponse;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
import net.wimpi.modbus.msg.WriteCoilRequest;
import net.wimpi.modbus.msg.WriteSingleRegisterRequest;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.procimg.SimpleInputRegister;

public class ModBusConnection {
	private InetAddress ipAdd = null;
	private int port = Modbus.DEFAULT_PORT;
	private int index;
	private TCPMasterConnection con = null;
	private CarThread pllr;
	

	public ModBusConnection(InetAddress ip, int index, int port, Dnp3Connection dnp3) throws Exception {
		this.ipAdd = ip;
		this.port = port;
		this.index = index;
		this.connect();
		pllr = new CarThread(this,index,dnp3);
		pllr.start();
	}

	public ModBusConnection(InetAddress ip, int index, Dnp3Connection dnp3) throws Exception {
		this.ipAdd = ip;
		this.index = index;
		this.connect();
		pllr = new CarThread(this,index,dnp3);
		pllr.start();
	}
	
	public ModBusConnection(String ipstr, int index) throws Exception {
		InetAddress ip = InetAddress.getByName(ipstr);
		this.ipAdd = ip;
		this.index = index;
		this.connect();
	}

	public void connect() throws Exception {
		con = new TCPMasterConnection(ipAdd);
    	con.setPort(port);
		
    	try {
    		con.connect();
			System.out.println("Succesful connection to: " + ipAdd);
    	} catch (Exception e) {
    		System.err.println("MODBUS_CONNECTION: Error when attempting to connect");
		throw e;
    	}
	}

	public boolean readBoolRW(int registerRef) throws Exception{
		int nbValues = 1;
		boolean returnedValue;
		ModbusTCPTransaction trans = new ModbusTCPTransaction(this.con);

		ReadCoilsRequest request = new ReadCoilsRequest(registerRef, nbValues);
		ReadCoilsResponse result = null;
	    	trans.setRequest(request);
		
	    	try {
	    		trans.execute();
	    		result = (ReadCoilsResponse) trans.getResponse();
	    		returnedValue = result.getCoilStatus(0);
	    		return returnedValue;

	    	} catch (Exception e) {		    		
			System.err.println("Read coil failed");	    		
			throw e;
	    		
	    	}
	    			
	}

	public void setInt(int registerRef, int value)throws Exception {
		ModbusTCPTransaction trans = new ModbusTCPTransaction(this.con);		
	 	WriteSingleRegisterRequest request = new WriteSingleRegisterRequest(registerRef, new SimpleInputRegister(value));
	 	trans.setRequest(request);
	    	try {
	    		trans.execute();
	    		trans.getResponse();
	    	} catch (Exception e) {	
	    		System.err.println("Write input register failed");
			throw e;
	    	}
	}

	public void setBool(int registerRef, boolean value) {
		ModbusTCPTransaction trans = new ModbusTCPTransaction(this.con);
	 	WriteCoilRequest request = new WriteCoilRequest(registerRef, value);
	 	trans.setRequest(request);
	    	try {
	    		trans.execute();
	    		trans.getResponse();
	    		
	    	} catch (Exception e) {
	    		
	    		System.err.println("Write coil failed");
	    	}
	}
	
	public int getIntRW(int registerRef) {
		int nbValues = 1;
		int returnedValue;
		ModbusTCPTransaction trans = new ModbusTCPTransaction(this.con);
		ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(registerRef, nbValues);
		ReadMultipleRegistersResponse result = null;
	    	trans.setRequest(request);
	    	try {
	    		trans.execute();
	    		result = (ReadMultipleRegistersResponse) trans.getResponse();
	    		returnedValue = result.getRegisterValue(0);
	    		return returnedValue;
		    	} catch (Exception e) {	
		    		System.err.println("Read multiple register failed");
	    	}
	    	return 0;
	}

	public int getIntRO(int registerRef) {
		int nbValues = 1;
		int returnedValue;
		ModbusTCPTransaction trans = new ModbusTCPTransaction(this.con);
		ReadInputRegistersRequest request = new ReadInputRegistersRequest(registerRef, nbValues);
		ReadInputRegistersResponse result = null;
    	trans.setRequest(request);
    	try {
    		trans.execute();
    		result = (ReadInputRegistersResponse) trans.getResponse();
    		returnedValue = result.getRegisterValue(0);
    		return returnedValue;
    	} catch (Exception e) {		    		
    		//System.err.println("Read input register failed");
    	}
    	return 0;
	}
	



}
