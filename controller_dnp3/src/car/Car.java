package car;

import dnp3.*;
import com.automatak.dnp3.*;

import graphics.CarPanel;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import control.ControlCenter;

import com.google.common.net.InetAddresses;
import java.net.*;

import jkalman.JKalman;
import jama.Matrix;
import java.util.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException; 

import java.util.concurrent.ThreadLocalRandom;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Car {
	private int id, x, y;
	private String ip;
	private int port;
	private Dnp3Connection dnp3c;
	private int speed = 0;
	private boolean dnp3connected, mbconnected, runThread;
	public boolean enableControl;
	private CarPanel cp;
	private int distanceGlobal;

	//Time between reading and writings to car, in milliseconds
	private int readPause = 500;
	private int writePause = 500;

	/////////////////////////////////////////////MODBUS INDEXES
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

	public Car(int newid, String newip, int newport, Dnp3Connection newdnp3con) throws CarException {
		//Control variables
		id = newid;
		ip = newip;
		dnp3c = newdnp3con;
		port = newport;
		speed = 0;
		//distanceGlobal = readInt(STATUS_WALL_DISTANCE);
		//Graphic variables
		x=110+id*300;
		y=10;
		cp = new CarPanel(this,ControlCenter.window.getContent(),x,y);
		cp.initPanel();
		ControlCenter.window.validate();
        ControlCenter.window.repaint();
	}

	public String getLabel() {
		return ip;
	}

	public int getCarId() {
		return id;
	}

	public boolean readBool(int index) throws Exception {
		AnalogOutputInt16 query = new AnalogOutputInt16((short)index,CommandStatus.SUCCESS);
		ListenableFuture<CommandStatus> future = dnp3c.getProcessor().selectAndOperate(query, index);
		future.get();
		return dnp3c.getDataHandler().requestBoolResponse(index);
	}

	public void writeBool(boolean value, int index) throws Exception {
		long numbool=0;
		if (value) numbool = 1;
		ControlRelayOutputBlock query = new ControlRelayOutputBlock(ControlCode.LATCH_OFF,(short)index,(long)0,numbool,CommandStatus.SUCCESS);
		ListenableFuture<CommandStatus> future = dnp3c.getProcessor().selectAndOperate(query, index);
		future.get();
	}

	public int readInt(int index) throws Exception {
		AnalogOutputInt32 query = new AnalogOutputInt32(index,CommandStatus.SUCCESS);
		ListenableFuture<CommandStatus> future = dnp3c.getProcessor().selectAndOperate(query, index);
		future.get();
		return dnp3c.getDataHandler().requestIntResponse(index);
	}

	public void writeInt(int value, int index) throws Exception {
		ControlRelayOutputBlock query = new ControlRelayOutputBlock(ControlCode.LATCH_OFF,(short)index,(long)id,(long)value,CommandStatus.SUCCESS);
		ListenableFuture<CommandStatus> future = dnp3c.getProcessor().directOperate(query, index);
		future.get();
	}

	public int readIntNU(int index) throws Exception {
		return dnp3c.getDataHandler().requestIntResponse(index);
	}

	private int ipCodeTo32(String ip) throws CarException{
        int address = 0;
        try {
        	InetAddress addr = InetAddresses.forString(ip);
        	address = InetAddresses.coerceToInteger(addr);
        } catch (Exception e) {
        	throw new CarException("Invalid ip -- previous exception trace > " + e);
        }
        
        return address;
    }

	public class CarException extends Exception {
	    public CarException(String message) {
	        super(message);
	    }
	}

	public void stopCar() {
		runThread = false;
	}

	public static String getCurrentTimeStamp() {
        return new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
    }

	private void drawGui(int dist, int speed, boolean forward,int alarm){
		cp.getWallDist().setText(Integer.toString(dist));
		cp.getSpeed().setText(Integer.toString(speed));
		cp.getState().setText(forward?"Forward":"Backward");
		cp.getAlarm().setText(Integer.toString(alarm));
		switch (alarm){
    		case 0:
    			cp.getAlert().setText("None");
    		break;
    		case 1:
    			cp.getAlert().setText("Low");
    		break;
    		case 2:
    			cp.getAlert().setText("Medium");
    		break;
    		default:
    			cp.getAlert().setText("High");
	    	break;
    	}
    	cp.getPanel().repaint();
	}

	public void start() {
		readCar();
		controlCar();
	}

	public void controlCar(){
		Thread t = new Thread(new Runnable() {
			public void run() {
				
				enableControl = true;
				///////////////////////////////////////////////////////////////////////
			        ///// Code ajoutÃ©
				///////////////////////////////////////////////////////////////////////      
			    final int minDistance = 0; 
			    boolean statusActive;
			    boolean status_cspeed;
			    boolean statusCar;
			    boolean ancienEtat;
			    boolean etat=true;
			    boolean gRising = false;
			    ///////////////////////////////////////////////////
			    //Multi watermark
			    boolean multiWater = false;
			    int mwCounter = 1;
		        int mwPeriod = 32;            
			    int watermark = ThreadLocalRandom.current().nextInt(1, 4);
			    ////////////////////////////////////////////////////
			    boolean dumpVars = true;

			    int conAlarm = 0;
			    int move=0;
			    int sens = 1;
			    int pos_latt=0;
			    int pos_long=0;
			    
			    int speed0=1;
			    Matrix d_initial= new Matrix(1,1,0);
			    int alarm=0;
			    long start_time=0;
			    long end_time=0;
			    double now=0;
			    Random randomno = new Random();
			    double variation=0;
			    //Alarme
			    int start=100;
			    int end=0;
			    boolean alerte=false;
			    boolean capteur=false;
			    double g_total_old=0;
			    double mean=0;
			    int wallDistance_old=0;
			    int alarme_cycle=0;
			    
			//*************************************************************************************
			    ///////////////////////////////////////////////////////////////////
				//System control model 
				// x(t+1) = Ax(t) +B(u(t) + w(t))
				// y(t) = Cx(t) + Du(t) + v(t)
				// state transition matrix (A) 
				double [][] A_a={{0.0821,0.0551}, {0,1.0}}; //015s
				// double [][] A_a={{0.2867,0.0571}, {0,1.0}}; //01s
				// double [][] A_a={{1,0.5}, {0,1.0}}; //vel=250
				Matrix A = new Matrix(A_a);
				// state transition matrix (B)
				//double [][] B_b = {{0.09245},{0.004745}};  //new double [2][1];
				//double [][] B_b = {{0.0571},{0.0039}};        
				// double [][] B_b = {{0.0551},{0.0257}};//015s
				double [][] B_b = {{0.0551},{0.0357}};//015s
				// double [][] B_b = {{0.5},{0.125}};//vel=250
				// double [][] B_b = {{0.0571},{0.0034}};//01s
				Matrix B = new Matrix(B_b);
				// measurement matrix (H)
				//double [][] C_c = {{0.0 , 0.081}};//new double [1][2];
				//double [][] C_c = {{0.0 , 0.6309}};//new double [1][2];
				double [][] C_c = {{0.0 , 0.8386}};//015s
				// double [][] C_c = {{0 , -0.021}};//vel=250
				// double [][] C_c = {{0.0 , 0.6304}};//01s
			    Matrix C = new Matrix(C_c);
			    /** predicted state (x'(k)): x(k)=A*x(k-1)+B*u(k) */
			    Matrix state_pre = new Matrix(2,1,0.0);
			    /** corrected state (x(k)): x(k)=x'(k)+K(k)*(z(k)-H*x'(k)) */
			    Matrix state_post = new Matrix(2,1,0.0);
			    ///Initialization
			    // system output ( measurement)
			    Matrix distance= new Matrix(1,1,0.0); 
			    int wallDistance=(int)distance.get(0,0);
			    //Matrix control input value
			    Matrix u=new Matrix(1,1,(double)speed);
			    // kalman filter gain
			    Matrix kf = new Matrix(2,1); //{{0.0001},{0.0198}};
			    //Detector
			    // ChiÂ² detector
			    int wind=3;
			    Matrix g = new Matrix(wind, 1, 0.0);
			    //Parametres
			    double g_next=0.0; 
			    double g_total=0.0;
			    // Covariance detector error
			    Matrix cov=new Matrix(1,1,0.0);
			    //P --> Covariance of error
			    Matrix P = new Matrix(2,2,0.0);
			    //Residue=distance -Cx
			    Matrix residue=new Matrix(1,1,0.0);
			    // sensor noise R
			    Matrix R = new Matrix(1,1,0.8);
			    //Process noise
			    double [][] Q_m={{1,0.0}, {0.0,1}}; //new double [2][2];
			    Matrix Q = new Matrix(Q_m);
			    double threshold=21*wind;//threshold
			    int dInitialInt = 0;
			//**************************************************************************************
		    	int lowthreshold = 35;
		    	int stabled = 0;
		        long durationMean = 0;
		        long iterationCounter = 0;
		        JKalman car = null;
		        int watermarkchange = wind - 2;
		        int stawmcounter = 0;
		        int storespeed = 120;
		        try {
		        	car= new JKalman(2,1,1);
		        } catch (Exception e) {
					System.err.println("Main Thread: Car control thread error, car: " + id + " Exception: " + e);
				}
			////////////////// Fin code ajoutÃ©
				try {
					writeBool(true, STATUS_CSPEED);
					writeBool(true, STATUS_CAR);
					long initialTime = System.currentTimeMillis();
					while (runThread) {
						long startTime = System.currentTimeMillis();
						wallDistance_old=wallDistance;
						wallDistance = distanceGlobal;
						distance.set(0, 0, wallDistance);
						ancienEtat=etat;
						etat = readBool(ETAT);
						//////////////////////////////////////////////////////////////////////
						if(ancienEtat!=etat) d_initial.set(0,0,wallDistance);				
						if((ancienEtat!=etat)||(speed0==1)){
								if (!(ancienEtat)){
		     	    				System.out.println("Round trip finished");
		     	    			}
								System.out.println("Initializing distance"); 		
								d_initial.set(0,0,wallDistance);
								dInitialInt = wallDistance;
								System.out.println(" Initial_distance= "  + d_initial.toString()+"\n");
								speed0=0;
						    }
						////////////////////////////////////////////////////////////////////////			
						//System.out.println("Distance: " + distanceGlobal);
						if(multiWater) {
							if(mwCounter == mwPeriod){
								mwCounter = 0;
								int prevwm = watermark;
								while(prevwm == watermark) {
									watermark = ThreadLocalRandom.current().nextInt(1, 4);
								}
								
							}
							switch (watermark){
					    		case 1:
					    			variation=25+5*randomno.nextGaussian();
					    		break;
					    		case 2:
					    			variation=30*randomno.nextGaussian();
					    		break;
					    		case 3:
					    			variation=-20+10*randomno.nextGaussian();
					    		break;
					    	}

							mwCounter++;
						} else {						
							variation=30*randomno.nextGaussian();				
						}
						// if(stawmcounter == 0){
						// 	storespeed = 250+(int)variation;
						// }
						// stawmcounter++;
						// speed = storespeed;
						// if(stawmcounter == watermarkchange){
						// 	stawmcounter = 0;
						// }
						
						if(!etat){
        	    			sens=-1;
        	    		}
        	    		else {
        	    			sens=1;
        	    		}
						speed = sens*250+(int)variation;

						u.set(0, 0, (double)speed);

						//////////////////////////////////////////////////////
						// kalman filter
						//Dynamic of the system
						//x=Ax + B(u + w)  //distance=Cx + v
						//Matrix
						car.setTransition_matrix(A);
						car.setControl_matrix(B);
						car.setMeasurement_matrix(C);
						///////////////////////////
						//Noise
						car.setMeasurement_noise_cov(R);
						car.setProcess_noise_cov(Q);
						//////////////////////////
						// (x'(k)): x(k)=A*x(k-1)+B*u(k)
						//status
						car.setState_pre(state_post);
						state_pre=car.Predict(u);
						// (x(k)): x(k)=x'(k)+K(k)*(z(k)-H*x'(k))
						state_post=car.Correct(d_initial.minus(distance));
						car.setState_post(state_post);
						//residue=distance -Cx
						residue=car.getResidue();
						//residue=(d_initial.minus(distance)).minus((car.getMeasurement_matrix()).times(car.getState_pre()));
						//x(t+1)=Ax(t) +Bu(t) + kf(distance-Cx(t))
						//x_next=((A.times(car.getState_pre())).plus(B.times(speed))).plus(kf.times(residue));
						/////////////////////////=/////////////////////////////
						//detection
						// x² detector parameter g
						cov=((car.getMeasurement_matrix().times(P)).times((car.getMeasurement_matrix()).transpose())).plus(R);//inv((CPC^T) + R) ---> R=noise
						g_next=(((residue.transpose()).times(cov.inverse())).times(residue)).get(0,0);//g=residue^T(inv(cov))residue
						g_total=0.0;

						for (int i=0;i<wind;i++){
							if(i<(wind-1)){
								g.set(i, 0, g.get(i+1, 0));//g[i] [1]=g[i+1] [1];
							}else{
								g.set(i, 0, g_next);
							}
							g_total=g_total + g.get(i, 0);
						}


						//the mean variable used for the alarm
						String data[]={Integer.toString(speed),(d_initial.minus(distance)).toString(),distance.toString(),Double.toString(now)};				
						System.out.println("GTotal: " + g_total);
						//System.out.println("P: " + P);
						//System.out.println("kf: " + kf);
						//System.out.println("Residue: " + residue);
						Matrix estimation = car.getMeasurement_matrix().times(car.getState_pre());
						//Matrix estimation = (C.times(state_pre));
						//System.out.println(" x_pre= " + (car.getState_pre()).toString() + "\n x_post= " + (car.getState_post()).toString() + "\n distance= " + data[1] +" estimation= " + estimation.toString() + "\n");
						System.out.println("Real distance= " + wallDistance +" distance= " + data[1] +" estimation= " + estimation.toString() + "\n");
						//System.out.println("Distance: " + wallDistance + " estimation: " + (Math.abs(estimation.get(0,0)) + dInitialInt));			
						//***********************************************************************
						if((g_total+g_total_old)/2 < lowthreshold && alarme_cycle > 0 ) stabled++;
						if(stabled>8) {
							stabled = 0;
							alarme_cycle--;
						}
						//***********************************************************************
						if (g_total_old!=0.0){
						    mean=(g_total-g_total_old); //(wallDistance_old-wallDistance);
						}
						g_total_old=g_total;
						if (g_total>threshold){
						alarm=1;
							
						}else{
						alarm=0;
						conAlarm = 0;
						}
						
						
						
						if ((Math.abs(mean)>80)&&(Math.abs(mean)<200)){
						 	//if(mean > 0)alerte=true;
							System.out.println("******Delta G High!: " + Math.abs(mean));	 
						 }else{
						 	 alerte=false;
						}
						if ((alarm==1)||alerte){
						    if (wallDistance > 50){
							if (wallDistance<180){
							    //alarme_cycle++;
							    stabled = 0;
							    conAlarm++;
							}
						    }
						}
						if(conAlarm>wind){
							alarme_cycle++;	
						}
						long duration = 0;
						if(enableControl) {
							
							writeInt(Math.abs(speed),STATUS_SPEED);				
							long endTime = System.currentTimeMillis();
							duration = (endTime - startTime);
							iterationCounter++;
							durationMean = (durationMean + duration);
							//System.out.println("Writing latency in ms: " + duration + " Average: " + durationMean/iterationCounter);
							drawGui(distanceGlobal,speed,etat,alarme_cycle);

						}
						if(dumpVars) {
							long estimatedTime = System.currentTimeMillis() - initialTime;
							try(FileWriter fw = new FileWriter("data/gtotal_untouch_cyberphysical" + initialTime, true);
							    BufferedWriter bw = new BufferedWriter(fw);
							    PrintWriter out = new PrintWriter(bw))
							{
							    out.println(getCurrentTimeStamp() + " timeelapsed: " + estimatedTime/1000.0 + " gtotal: " + g_total + " distance: " + distanceGlobal + " estimation: " +  Math.abs(dInitialInt-estimation.get(0,0)) + " speed: " + speed + " alarmcycle: " + alarme_cycle);
							} catch (IOException e) {
								System.out.println(e);
							}
						}
						long sleepnow = writePause-duration > 0 ?writePause-duration:0;
						Thread.sleep(sleepnow);
					}
					writeInt(0,STATUS_SPEED);
					writeBool(false, STATUS_CSPEED);
					writeBool(false, STATUS_CAR);
				} catch (Exception e) {
					System.err.println("Main Thread: Car control thread error, car: " + id + " Exception: " + e);
				}

			}

		});
	
		t.start();
	}

	public void readCar(){
		Thread t = new Thread(new Runnable() {
			public void run() {
				runThread = true;
				long startTime, endTime, duration;
				while (runThread) {
					try {
												
						startTime = System.currentTimeMillis();						
						distanceGlobal = readIntNU(STATUS_WALL_DISTANCE);
						endTime = System.currentTimeMillis();
						duration = (endTime - startTime);
						System.out.println("Reading latency in ms: " + duration);
						long sleepnow = readPause-duration > 0 ?readPause-duration:0;
						Thread.sleep(sleepnow);
					} catch (Exception e) {
						System.err.println("Main Thread: Car control thread error, car: " + id + " Exception: " + e);
					}
				}
			}

		});
	
		t.start();
	}
}
