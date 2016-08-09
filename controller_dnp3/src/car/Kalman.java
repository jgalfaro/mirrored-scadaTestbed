package car;

import java.util.ArrayList;
import java.util.List;

import Jama.Matrix;


public class Kalman {
	// covariance R constants
    private static final double p1=0.001621;
    private static final double p2=-0.01779;
    private static final double p3=0.01477;
	//state
	Matrix X=new Matrix(3,1);
	Matrix P=new Matrix(3,3);

	Matrix GradFx=new Matrix(3,3);
	Matrix GradFw=new Matrix(3,2);
	//Noise
	Matrix Q=new Matrix(2,2);
	Matrix R=new Matrix(2,2);
	//Meassure/inovation
	Matrix V=new Matrix(2,1);
	Matrix Sxv=new Matrix(3,2);
	Matrix Svv=new Matrix(2,2);
	Matrix GradHx=new Matrix(2,3);
	Matrix GradHy=new Matrix(2,3);
	Matrix GradHv=new Matrix(2,2);
	//Kalman Gain
	Matrix K=new Matrix(3,2);
	
	//Robot Lego
	public static List<Car> Lego = new ArrayList<Car>();
	double CovDist=0.07;
	double CovAng=0.07;
	double CovIR=0.15;
	public Kalman(double [][] Xo,double [][] Po, double [][] Q, double [][] R, List<Car> Lego){
		X=new Matrix(Xo);
		P=new Matrix(Po);
		this.Q=new Matrix(Q);
		this.R=new Matrix(R);
		this.Lego=Lego;
		this.GetGradHx();
		this.GetGradHy();
	}

	public Kalman(double [][] Xo,double [][] Po, List<Car> Lego){
		X=new Matrix(Xo);
		P=new Matrix(Po);
		this.Lego=Lego;
		this.GetGradHx();
		this.GetGradHy();
	}
	public Kalman(double [][] Xo,Matrix Po,List<Car> Lego){
		X=new Matrix(Xo);
		P=Po;
		this.Lego=Lego;
		this.GetGradHx();
		this.GetGradHy();
	}
	public void Reset(double [][] Xo,Matrix Po){
		X=new Matrix(Xo);
		P=Po;
	}
	public void setCov(double CovDist,double CovAng,double CovIR){
		this.CovDist=CovDist;
		this.CovAng=CovAng;
		this.CovIR=CovIR;
	}
	public void GetQ(double deltang,double distance){
		Q.set(0,0,CovDist*Math.abs(distance));
		Q.set(1,1,CovAng*Math.abs(deltang));
	}
	public void GetR(double d1,double d2,double observTeta){
	    R.set(0,0,CovIR);
	    R.set(1,1,CovIR);
	}
	public void GetGradHx(){
	    GradHx.set(0,0,1);
	    GradHx.set(0,1,0);
	    GradHx.set(0,2,0);
	    GradHx.set(1,0,0);
	    GradHx.set(1,1,0);
	    GradHx.set(1,2,1);
	}
	public void GetGradHy(){
	    GradHy.set(0,0,0);
	    GradHy.set(0,1,1);
	    GradHy.set(0,2,0);
	    GradHy.set(1,0,0);
	    GradHy.set(1,1,0);
	    GradHy.set(1,2,1);
	}
	/*public void GetGradHv(int sign,double d1,double d2,double observTeta){
		   double dtetadd=Lego.sensDist/(GenOper.power(Lego.sensDist,2)+GenOper.power(d1-d2,2));
		   GradHv.set(0,0,sign*(-0.5*Math.cos(observTeta)-(d1+d2)/2*Math.sin(observTeta)*dtetadd));
		   GradHv.set(0,1,sign*(-0.5*Math.cos(observTeta)+(d1+d2)/2*Math.sin(observTeta)*dtetadd));
		   GradHv.set(1,0,dtetadd);
		   GradHv.set(1,1,-dtetadd);
		}
	public void GetGradFx(double deltang,double distance){
		GradFx.set(0,0,1);
		GradFx.set(0,1,0);
		GradFx.set(0,2,-distance*(Math.sin(Lego.ang+deltang/2)));
		GradFx.set(1,0,0);
		GradFx.set(1,1,1);
		GradFx.set(1,2,distance*(Math.cos(Lego.ang+deltang/2)));
		GradFx.set(2,0,0);
		GradFx.set(2,1,0);
		GradFx.set(2,2,1);
	}
	public void GetGradFw(double deltang, double distance){
	   GradFw.set(0,0,Math.cos(Lego.ang+deltang/2));
	   GradFw.set(0,1,-distance*Math.sin(Lego.ang+deltang/2)/2);
	   GradFw.set(1,0,Math.sin(Lego.ang+deltang/2));
	   GradFw.set(1,1,distance*Math.cos(Lego.ang+deltang/2)/2);
	   GradFw.set(2,0,0);
	   GradFw.set(2,1,1);
    }
	// covariances
	public void EstStepCov(){
		Matrix Pnew=((GradFx.times(P)).times(GradFx.transpose())).plus((GradFw.times(Q)).times(GradFw.transpose()));
		//P=(GradFw.times(Q)).times(GradFw.transpose());
		P= Pnew;
	}
	public void UpdateStepCov(){
		P.minusEquals((K.times(Svv)).times(K.transpose()));
		//.out.println(P.get(0,0));
	}
	// Inovation
	public void GetSxv(char ch){
	  if (ch=='x') Sxv=P.times(GradHx.transpose());
	  else Sxv=P.times(GradHy.transpose());
	  //System.out.println(this.Sxv.data[0][1]+" "+this.Sxv.data[1][0]+"\n");
	}
	public void GetSvv(char ch){
	  if (ch=='x') Svv=((GradHx.times(P)).times(GradHx.transpose())).plus((GradHv.times(R)).times(GradHv.transpose()));
	  else Svv=((GradHy.times(P)).times(GradHy.transpose())).plus((GradHv.times(R)).times(GradHv.transpose()));
	  //if (ch=='x') Svv=((GradHx.times(P)).times(GradHx.transpose())).plus(R);
	  //else Svv=((GradHy.times(P)).times(GradHy.transpose())).plus(R);
	//System.out.println(this.Svv.get(1,0)+" "+this.Svv.get(0,1)+"\n");
	//System.out.println(this.Svv.get(0,0)+" "+this.Svv.get(1,1)+"\n");
	
	}
	public void GetInnovation(double observXY,double angInov,char ch){
		   if (ch=='x'){this.V.set(0,0,observXY-this.X.get(0,0));}
		   else {this.V.set(0,0,observXY-this.X.get(1,0));}
		   this.V.set(1,0,GenOper.NormalizeAng(angInov-this.X.get(2,0)));
		   //System.out.println(this.V.get(1,0)*180/Math.PI+"\n");
	    }
	// gain
	public void GetKalmanGain(){
	  K=(this.Sxv).times(this.Svv.inverse());
	  //System.out.println(this.K.get(1,0)+" "+this.K.get(1,1)+"\n");
	}
	// handle state
	public void getState(){
		X.set(0,0,Lego.x);
		X.set(1,0,Lego.y);
	    X.set(2,0,Lego.ang);
	}
	public void setState(char ch){
		Lego.x=X.get(0,0);
		Lego.y=X.get(1,0);
		Lego.ang=GenOper.NormalizeAng(X.get(2,0));
		//System.out.println(Lego.y+"\n");
	}
	public void KalmanUpdateState(){
		X=X.plus(K.times(V));
   }
	// estimation step
	public void KalmanEstimationStep(){
	  //Kalman estimator
		//estimate
	  Lego.UpdatePos();
	  	//get gradients
	  GetGradFx(Lego.deltang,Lego.deltdist);//odometry
	  GetGradFw(Lego.deltang,Lego.deltdist);
	  GetQ(Lego.deltang,Lego.deltdist);
	  	// compute covariance
	  EstStepCov();
	}
	// update step
	public void KalmanUpdateStep(double d1,double d2,char ch,int sign,double baseAng){
	  double observTeta=this.ObservTeta(d1,d2);
	  double obserD=this.ObservD(observTeta,d1,d2);
	  getState();
	  //Innovation
	  //System.out.println(sign*(1.25-obserD));
	  //System.out.println(GenOper.NormalizeAng(baseAng+observTeta)*180/Math.PI);
      GetInnovation(sign*(1.25-obserD),GenOper.NormalizeAng(baseAng+observTeta),ch);
      //
	  GetR(d1/100,d2/100,observTeta);
	  GetGradHv(sign,d1/100,d2/100,observTeta);
      // covariances
	  GetSxv(ch);
	  GetSvv(ch);
	  // Update
	  GetKalmanGain();
	  KalmanUpdateState();
	  setState(ch);
	  UpdateStepCov();
	}
	public int TestAng(double d1,double d2){
    	if (d1>=d2) return 1;
    	else return -1;
	}
	public double ObservD(double observTeta,double d1,double d2){
	    return Math.abs(d1+d2)/200*Math.cos(observTeta);
	}
	public double ObservTeta(double d1,double d2){
	    return Math.atan2((d1-d2)/100,Lego.sensDist);
	}
	public void PrintP(){
		double p1 = P.get(0,0);
		double p2 = P.get(1,1);
		double p3 = P.get(2,2);
		double p12 = P.get(0,1);
		double p13 = P.get(0,2);
		double p32 = P.get(1,2);
		System.out.println(p3*180/Math.PI+"\n");
	}*/
}
