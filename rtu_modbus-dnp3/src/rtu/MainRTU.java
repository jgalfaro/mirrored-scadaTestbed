package rtu;

import com.automatak.dnp3.*;
import com.automatak.dnp3.impl.DNP3ManagerFactory;	
import com.automatak.dnp3.mock.PrintingLogSubscriber;
import com.automatak.dnp3.mock.SuccessCommandHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.ModbusDeviceIdentification;
import net.wimpi.modbus.io.ModbusTCPTransaction;

public class MainRTU {
	public static Dnp3Connection dnp3Master;
	public static void main(String[] args) throws IOException, InterruptedException {
    	dnp3Master = new Dnp3Connection();

        // all this stuff just to read a line of text.
        String line = "";
        InputStreamReader converter = new InputStreamReader(System.in);
        BufferedReader in = new BufferedReader(converter);


        int i = 0;
        while (true) {
            System.out.println("Enter something to update a counter or type <quit> to exit");
            line = in.readLine();
            if(line.equals("quit")) break;
            else {
                //db = new DatabaseConfig(0,0,20,10,0);

            }
        }

        dnp3Master.manager.shutdown();
    }
}
