package rtu;

import com.automatak.dnp3.DataObserver;
import com.automatak.dnp3.Counter;
import com.automatak.dnp3.CounterInputQuality;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class CarThread extends Thread {

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

	private ModBusConnection mbc;
	private Dnp3Connection dnp3;
	private int id;
	private long delay = 100;

	public CarThread(ModBusConnection mbc, int id, Dnp3Connection dnp3){
		this.mbc = mbc;
		this.id = id;
		this.dnp3 = dnp3;
	}
	
	public CarThread(ModBusConnection mbc, int id, long delay, Dnp3Connection dnp3){
		this.mbc = mbc;
		this.id = id;
		this.dnp3= dnp3;
		this.delay = delay;	
	}	
	
	public void run() {

		int readDistance = 0;
		int prereadDistance = 0;
		while ( true ) {
			
			try {		
				Thread.sleep(delay);				
				readDistance =  mbc.getIntRO(STATUS_WALL_DISTANCE);
				if (prereadDistance != readDistance) {
					dnp3.data.start();
					//System.out.println("DEBUG - readDistance: " + readDistance);
					dnp3.data.update(new Counter(readDistance, CounterInputQuality.ONLINE.toByte(), System.currentTimeMillis()), STATUS_WALL_DISTANCE);
					dnp3.data.end();
				}
				prereadDistance=readDistance;
			} catch (Exception e) {
				System.err.println("Poller thread reading error, car id:" + id);
				dnp3.data.start();
				dnp3.data.update(new Counter(prereadDistance, CounterInputQuality.COMM_LOST.toByte(), System.currentTimeMillis()), STATUS_WALL_DISTANCE);
				dnp3.data.end();
			}
		}
	}

	
}
