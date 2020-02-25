package man;
import robocode.*;
import java.util.Random;
import static robocode.util.Utils.normalRelativeAngleDegrees;

 public class MyFirstRobot extends AdvancedRobot {
	 private static final Random RANDOM = new Random();
	 private byte scanDirection = 1;
	 private byte moveDirection = 1;
	 private double previousEnergy = 100;
	 private int counter = 0;
	 private int turn = 1;
	 public void run(){
		 while (true){
			 turnRight(5*turn);
		 }
	 }
	 public void onScannedRobot(ScannedRobotEvent e){
		double turnGunAmt = normalRelativeAngleDegrees(e.getBearing() + getHeading() - getGunHeading());
		turnGunRight(turnGunAmt);
		fireGoodAmount(getEnergy());
		setTurnRight(e.getBearing() + 90);
		double energyChange = previousEnergy - (e.getEnergy());
		if(energyChange >= 0.1 && energyChange <= 3){
			moveDirection *= -1;
			setAhead((randomDistance())*moveDirection);
		}
		scanDirection *= -1;
		setTurnGunRight(99999*scanDirection);
		fireGoodAmount(getEnergy());
		previousEnergy = e.getEnergy();
	 }
	 
	 private void fireGoodAmount(double en) {
		 if(en >= 90)
			 fire(3);
		 else if(en >= 80)
			 fire(2);
		 else if(en >= 40)
			 fire(1);
		 else
			 fire(0.5);
	 }
	 
	 //Talvez venham a dar jeito
	 
	 private int randomDistance() {
		 return RANDOM.nextInt(200);
	 }
	 private int randomDirection(){
		 return RANDOM.nextBoolean() ? 1 : -1;
	 }
	 
	 private int randomTurn(){
		 return RANDOM.nextInt(360);
	 }
 }