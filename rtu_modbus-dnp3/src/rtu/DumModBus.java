package rtu;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;



public class DumModBus {
	
	private static String mbslave = "192.168.2.4";
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
	
	
	
	public static void main(String[] args) throws Exception {
    	
        ModBusConnection mbc = new ModBusConnection(mbslave, 1);
        String line = "";
        InputStreamReader converter = new InputStreamReader(System.in);
        BufferedReader in = new BufferedReader(converter);


        int i = 0;
        int dist = 0;
        int speed = 0;
        //Start car
        mbc.setBool(STATUS_ACTIVE,true);
        mbc.setBool(STATUS_CSPEED,true);
        mbc.setBool(STATUS_CAR,true);
        while (System.in.available() == 0) {
            dist = mbc.getIntRO(STATUS_WALL_DISTANCE);
            System.out.println("Distance: " + dist);
            System.out.println("Direction: " + mbc.readBoolRW(ETAT));
            speed = 300;
            mbc.setInt(STATUS_SPEED,speed);
            System.out.println("Sending speed: " + speed);
            Thread.sleep(500);
        }
        mbc.setInt(STATUS_SPEED,0);
        mbc.setBool(STATUS_ACTIVE,false);
        mbc.setBool(STATUS_CSPEED,false);
        mbc.setBool(STATUS_CAR,false);
    }
}
