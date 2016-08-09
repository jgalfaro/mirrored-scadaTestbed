package graphics;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import car.Car;
import control.ControlCenter;

public class CarPanel {
	private JLabel jlbStatusCars;
	private JLabel jlbCarName;
	private JLabel jlbStatusDist_label;
	private JLabel jlbStatusDist;
	private JLabel jlbSpeed;
	private JLabel jlbSpeed_label;
	private JLabel jlbState;
	private JLabel jlbState_label;
	private JLabel jlbAlarm;
	private JLabel jlbAlarm_label;
	private JLabel jlbAlert;
	private JLabel jlbAlert_label;

	protected int X;
	protected int Y;
	protected Car myCar;

	protected JPanel content;
	protected Container container;

	private JButton btnCar_Cspeed;
	protected JButton btnClose;

	public CarPanel (Car dev, Container con, int x, int y) {
		super();
		container = con;
		myCar = dev;
		X = x;
		Y = y;		
		initPanel();
	}
	
	public void initPanel(){
		content = new JPanel();
		content.setLayout(null);
		content.setBackground(Color.white);
		content.setBorder(BorderFactory.createLineBorder(Color.black));
		
		jlbCarName = new JLabel(myCar.getLabel());
		jlbCarName.setBounds(5, 5, 200, 15);
		content.add(jlbCarName);

		jlbStatusDist_label = new JLabel("Distance: ");
		jlbStatusDist_label.setBounds(55, 100, 110, 20);
		content.add(jlbStatusDist_label);
		jlbStatusDist = new JLabel("none");
		jlbStatusDist.setBounds(165, 100, 150, 20);
		content.add(jlbStatusDist);
		
		//Speed
		jlbSpeed_label = new JLabel("Speed: ");
		jlbSpeed_label.setBounds(55, 120, 110, 20);
		content.add(jlbSpeed_label);
		jlbSpeed = new JLabel("none");
		jlbSpeed.setBounds(165, 120, 150, 20);
		content.add(jlbSpeed);

		jlbState_label = new JLabel("Direction: ");
		jlbState_label.setBounds(55, 140, 110, 20);
		content.add(jlbState_label);
		jlbState = new JLabel("none");
		jlbState.setBounds(165, 140, 150, 20);
		content.add(jlbState);

		jlbAlarm_label = new JLabel("EVTs: ");
		jlbAlarm_label.setBounds(55, 160, 110, 20);
		content.add(jlbAlarm_label);
		jlbAlarm = new JLabel("none");
		jlbAlarm.setBounds(165, 160, 150, 20);
		content.add(jlbAlarm);

		jlbAlert_label = new JLabel("Risk");
		jlbAlert_label.setBounds(55, 180, 110, 20);
		content.add(jlbAlert_label);
		jlbAlert = new JLabel("None");
		jlbAlert.setBounds(165, 180, 150, 20);
		content.add(jlbAlert);

		btnClose = new JButton("Close");
		btnClose.setBounds(10, 235, 80, 20);
		btnClose.addActionListener(new ActionListener(){
	        public void actionPerformed(ActionEvent event){
	        	myCar.stopCar();
	        	removePanel();
	        	ControlCenter.removeCar(myCar.getCarId());
	          }
	        });
		content.add(btnClose);

		btnCar_Cspeed = new JButton("Control");
		btnCar_Cspeed.addActionListener(new ActionListener(){
	        public void actionPerformed(ActionEvent event){
	        	myCar.enableControl=!myCar.enableControl; 
	          }
	        });
		btnCar_Cspeed.setBounds(10 , 45, 100, 20);
		content.add(btnCar_Cspeed);

		JLabel picLabelCars = new JLabel( new ImageIcon( "/home/jose/workspace/dnp3_controlcenter/img/car.png"));
		picLabelCars.setBounds( 150, 35, 60, 60);
		content.add(picLabelCars);
		jlbStatusCars = new JLabel("");
		jlbStatusCars.setBounds( 200, 65, 30, 20);
		content.add(jlbStatusCars);

		content.setBounds(X, Y, 250, 270);
		content.setVisible(true);
		container.add(content);
		//content.validate();
        //content.repaint();

	}

	public JLabel getWallDist(){
		return jlbStatusDist;
	}

	public JLabel getSpeed(){
		return jlbSpeed;
	}

	public JLabel getState(){
		return jlbState;
	}

	public JLabel getAlarm(){
		return jlbAlarm;
	}

	public JLabel getAlert(){
		return jlbAlert;
	}


	public void removePanel(){
		content.setVisible(false);
		content.revalidate();
		content.repaint();
	}

	public JPanel getPanel(){
		return content;
	}

	public void updatePanel(int x, int y) {
		X = x;
		Y = y;
		removePanel();
		initPanel();
	}
	
	public JPanel getContent() {
		return content;
	}	
	/*
	protected void displayDiagFrame() {
    	JFrame winDiag = new DiagFrame(myDevice);
		winDiag.setVisible(true);
	}*/

}
