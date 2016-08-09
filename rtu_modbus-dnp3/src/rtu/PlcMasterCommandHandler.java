package rtu;

import com.automatak.dnp3.*;
/*
In order to code main commands that are used for reading and writing to the car we will code the commands 
from the master inside the parameters of different commands, this command will be abstracted on the master
side to avoid knowing which type of command is need for a certain reading or writing to the car
*/

//Java libraries to process and code a ip address into a 32 bit integer to be transmitted with dnp3
import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class PlcMasterCommandHandler implements CommandHandler {
    private static PlcMasterCommandHandler ourInstance = new PlcMasterCommandHandler();
    private final CommandStatus status = CommandStatus.SUCCESS;
	
	// Bool RW
	private static final int STATUS_ACTIVE = 0; 
	private static final int STATUS_CSPEED = 1;
	private static final int STATUS_CAR = 2;
	private static final int ETAT = 3;
	private static final int ALARME = 4;
	// Int RO
	private static final int STATUS_UNIT_ID = 0;
	private static final int STATUS_WALL_DISTANCE = 1;
	private static final int STATUS_POS_LATT = 2;
	private static final int STATUS_POS_LONG = 3;
	// Int RW
	private static final int STATUS_SPEED=0;
	private static final int STATUS_ROTATE=1;	
    
	public static PlcMasterCommandHandler getInstance() {
		return ourInstance;
	}

    public CommandStatus select(ControlRelayOutputBlock command, long index)
    {	
		return status;
    }

    public CommandStatus select(AnalogOutputInt32 command, long index)
    {      
		return status;
    }
    public CommandStatus select(AnalogOutputInt16 command, long index)
    {
		return status;
    }
    public CommandStatus select(AnalogOutputFloat32 command, long index)
    {
        return status;
    }
    public CommandStatus select(AnalogOutputDouble64 command, long index)
    {
        return status;
    }

    public CommandStatus operate(ControlRelayOutputBlock command, long index)
    {
        return status;
    }
    public CommandStatus operate(AnalogOutputInt32 command, long index)
    {
        return status;
    }
    public CommandStatus operate(AnalogOutputInt16 command, long index)
    {
        return status;
    }
    public CommandStatus operate(AnalogOutputFloat32 command, long index)
    {
        return status;
    }
    public CommandStatus operate(AnalogOutputDouble64 command, long index)
    {
        return status;
    }
    public CommandStatus directOperate(ControlRelayOutputBlock command, long index)
    {
       	return status;
    }
    public CommandStatus directOperate(AnalogOutputInt32 command, long index)
    {		
		System.out.println("Device ID: " + index);
		Dnp3Connection dnp3Conn = new Dnp3Connection((int)index);	
	try {
		InetAddress trainAddress = InetAddresses.fromInteger(command.value);
		System.out.println("IP received: " + trainAddress);
		ModBusConnection mbc = new ModBusConnection(trainAddress, (int)index, dnp3Conn);
		dnp3Conn.setCHModbus(mbc);
        mbc.setBool(STATUS_ACTIVE,true);
			
	} catch (Exception e) {
		System.err.println("COMMAND_HANDLER: Error using the ip from command: " + command + " -- Exception: " + e);
	    	return CommandStatus.HARDWARE_ERROR;
	}
	
        return status;
    }
    public CommandStatus directOperate(AnalogOutputInt16 command, long index)
    {
        return status;
    }
    public CommandStatus directOperate(AnalogOutputFloat32 command, long index)
    {
        return status;
    }
    public CommandStatus directOperate(AnalogOutputDouble64 command, long index)
    {
        return status;
    }
	
}
