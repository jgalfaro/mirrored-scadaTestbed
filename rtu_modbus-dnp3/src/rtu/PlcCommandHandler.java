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

public class PlcCommandHandler implements CommandHandler {
    private final CommandStatus status = CommandStatus.SUCCESS;
    private ModBusConnection mbc;
    private Dnp3Connection dnp3;
    private int id; 
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

	public PlcCommandHandler (int newid) {
		id = newid;
	}	
    
	public void setModbus(ModBusConnection newmbc){
		mbc = newmbc;	
	}

	public void setDnp3Con(Dnp3Connection newdnp3){
		dnp3 = newdnp3;	
	}

    public CommandStatus select(ControlRelayOutputBlock command, long index)
    {	
		if ( mbc != null ) {
			boolean write;
			if (command.offTimeMs == 0) {
				write = false;	
			} else {
				write = true;
			}
			System.out.println("Writing: " + write + " to: " + (int)command.count);
			try {
				mbc.setBool((int)command.count,write);
				select(new AnalogOutputInt16(command.count,status) , index);
				return status;
			} catch (Exception e) {
				System.err.println("COMMAND_HANDLER: Error writing boolean -- Exception: " + e);
				return CommandStatus.HARDWARE_ERROR;
			}			
		}
		System.err.println("COMMAND_HANDLER: Connection with device not established yet");
        return CommandStatus.HARDWARE_ERROR;
    }

    public CommandStatus select(AnalogOutputInt32 command, long index)
    {      
		if ( mbc != null ) {
			try {		
				int read = mbc.getIntRO(command.value);
				System.out.println("Response RW: " + read + " ID: " + id);
				dnp3.data.start();
				dnp3.data.update(new Counter(read, CounterInputQuality.ONLINE.toByte(), System.currentTimeMillis()), (int)index);
				dnp3.data.end();
				return status;
			} catch (Exception e) {
				System.err.println("COMMAND_HANDLER: Error reading: " + command + " -- Exception: " + e);
		    	return CommandStatus.HARDWARE_ERROR;
			}
		}
		System.err.println("COMMAND_HANDLER: Connection with device not established yet");
		return CommandStatus.HARDWARE_ERROR;
    }
    public CommandStatus select(AnalogOutputInt16 command, long index)
    {
		if ( mbc != null ) {
			try {
				boolean read = mbc.readBoolRW((int)command.value);
				System.out.println("Response BoolRW: " + read + " ID: " + id);
				dnp3.data.start();
				dnp3.data.update(new BinaryOutputStatus(read, CounterInputQuality.ONLINE.toByte(), System.currentTimeMillis()), (int)index);
				dnp3.data.end();
				return status;
			} catch (Exception e) {
				System.err.println("COMMAND_HANDLER: Error reading boolean -- Exception: " + e);
				return CommandStatus.HARDWARE_ERROR;
			}			
		}
		System.err.println("COMMAND_HANDLER: Connection with device not established yet");
        return CommandStatus.HARDWARE_ERROR;
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
        ///////////////////////////THIS OPERATIONS REPRESENTS A SENT INTEGER
        if ( mbc != null ) {
        //onTimeMs is used as the variable index
        //offTimeMs is used as the new value 
		try {	
			mbc.setInt((int)index,(int)command.offTimeMs);
			System.out.println("Writing succesful: " + (int)command.offTimeMs + " at index: " + (int)index + " and port " + id);
			return status;
		} catch (Exception e) {
			System.err.println("COMMAND_HANDLER: Error writing: " + command + " -- Exception: " + e);
	    		return CommandStatus.HARDWARE_ERROR;
		}
	}
	System.err.println("COMMAND_HANDLER: Connection with device not established yet");
	return CommandStatus.HARDWARE_ERROR;
    }
    public CommandStatus directOperate(AnalogOutputInt32 command, long index)
    {		
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
