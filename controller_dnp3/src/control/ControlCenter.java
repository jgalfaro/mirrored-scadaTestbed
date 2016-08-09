package control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import dnp3.Dnp3Connection;
import car.Car;

import com.automatak.dnp3.*;
import com.google.common.net.InetAddresses;
import java.net.*;

import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import graphics.HomeFrame;

public class ControlCenter {
    public static HomeFrame window;
	private static final String RASPIIP = "192.168.2.2";
	public static Dnp3Connection dnp3c;
    public static int carNum = 0;
    public static Car carArray [] = new Car[10];

	public static void main(String[] args) throws Exception {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        window = new HomeFrame();
                        window.setVisible(true);
                    }
                });
                dnp3c = new Dnp3Connection(RASPIIP);
                dnp3c.connect();
	}

        private static int ipCodeTo32(String ip) {
                int address = 0;
                try {
                        InetAddress addr = InetAddresses.forString(ip);
                        address = InetAddresses.coerceToInteger(addr);
                } catch (Exception e) {
                        System.err.println("Impossible to parse IP");
                        throw e;
                }
                return address;
        }

        private static Dnp3Connection newRemoteDnp3(String ip, int newport) {
                if (dnp3c.isConnected()) {
                        AnalogOutputInt32 query = new AnalogOutputInt32(ipCodeTo32(ip),CommandStatus.SUCCESS);
                        ListenableFuture<CommandStatus> future = dnp3c.getProcessor().directOperate(query, newport);
                        System.out.println("Creating new connection in port " + newport);
                        String response = future.get().name();
                        System.out.println("Response: " + response);
                        Dnp3Connection dnp3con = new Dnp3Connection(RASPIIP,newport);
                        try {
                                dnp3con.connect();
                        } catch (Exception e) {
                                System.err.println("Impossible to open new dnp3 port " + e);
                        }
                        return dnp3con;
                } 
                return null;
                
        }

        public static void addCar() {
                if (dnp3c.isConnected()) {
                        String ipField = JOptionPane.showInputDialog("Car IP Address:");
                        Dnp3Connection dnp3sec = newRemoteDnp3(ipField,20001+carNum);
                        Car c1;
                        try {
                                c1 = new Car(carNum,ipField,20000+carNum,dnp3sec);
                                c1.start();
                                carArray[carNum] = c1;
                                carNum++;
                                window.addMessage("New Car Added"); 
                        } catch (Exception e) {
                                System.err.println("Impossible to connect to new car " + ipField + " Exception: " + e);
                        }
                        
                                       
                } else {
                        JOptionPane.showInputDialog("Not connected to DNP3 Host");
                }
                
        }

        public static void removeCar(int id) {
                carArray[id] = null;
                window.addMessage("New Car Removed");
                window.validate();
                window.repaint(); 
        }         	
}