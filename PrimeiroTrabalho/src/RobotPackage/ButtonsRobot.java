package RobotPackage;
import robocode.*;
import java.awt.*;
import static robocode.util.Utils.*;

public class ButtonsRobot extends AdvancedRobot {
	
	private double battleFieldHeight = 0.0;
	private double battleFieldWidth = 0.0;
	private byte moveDirection = 1;
	private int missedBullets = 0;
	private byte scanDirection = 1;
	private double previousEnergy = 100.0;
	
	public void run() {
		
		setBodyColor(new Color(0, 0, 0));
		setGunColor(new Color(255, 255, 255));
		setRadarColor(new Color(192, 192, 192));
		
		double heading = getHeading();
		System.out.println(heading);
	
		while(true){
			// Replace the next 4 lines with any behavior you would like
			setAdjustRadarForGunTurn(true);
			setAdjustGunForRobotTurn(true);
			
			battleFieldHeight = getBattleFieldHeight();
			battleFieldWidth = getBattleFieldWidth();
			
			while(true){
				turnRadarRightRadians(Double.POSITIVE_INFINITY);
				moveDirection *= -1;
				setAhead(100*moveDirection);
			}
		}
	}
	
	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		
		double energyChange = previousEnergy - (e.getEnergy());
		setTurnRight(e.getBearing() + 90);
		
		if(energyChange >= 0.1 && energyChange <= 3) {
			moveDirection *= -1;
			setAhead(100*moveDirection);
		}
		
		if(e.getDistance() > 700) {
			ahead(100);
		}
		

		double turn = getHeading() - getGunHeading() + e.getBearing();
		setTurnGunRight(normalizeBearing(turn));
		fireGoodAmount(getEnergy(), e.getDistance());
		
		double radarTurn = getHeadingRadians() + e.getBearingRadians() - getRadarHeadingRadians();
		setTurnRadarRightRadians(2.0 * normalRelativeAngle(radarTurn));
		
		if(e.getDistance() < 200){
			
			fireGoodAmount(getEnergy(), e.getDistance());
		}
		previousEnergy = e.getEnergy();
	}
	
	public void onHitByBullet(HitByBulletEvent e) {
		//turnRight(90 - e.getBearing());
		//ahead(250);
	}
	
	public void onHitWall(HitWallEvent e) {
		double robotBearing = e.getBearing();
		turnRight(-robotBearing);
		ahead(200);
	}	
	
	public void onBulletMissed(BulletMissedEvent e) {
		missedBullets++;
	}
	
	public void onHitRobot(HitRobotEvent e) {
		double turn = getHeading() - getGunHeading() + e.getBearing();
		setTurnGunRight(normalizeBearing(turn));
		fireGoodAmount(getEnergy(), 80);
		
		/*//Fire on him!
		if (e.getEnergy() > 40) {
			fire(3);
		} else if (e.getEnergy() > 20) {
			fire(2);
		}
		
		ahead(40); //Smash him!*/
	}
	
	public void onStatus(StatusEvent e) {
		
	}
	
	public void onRoundEnded(RoundEndedEvent e) {
		
	}
	
	public void onBulletHit(BulletHitEvent e) {
		//ahead(50);
	}
	
	private void fireGoodAmount(double energy, double distance) {
		//System.out.println(distance);
			
		if(distance <= 80 && energy >= 25) {
			setBulletColor(new Color(255, 51, 51)); //Red
			fire(3);
		}
		else if(distance > 80 && distance <= 200) {
			if(energy >= 95) {
				setBulletColor(new Color(255, 51, 51)); //Red
				fire(3);
			}
			else if(energy < 90 && energy >= 70){
				setBulletColor(new Color(255, 153, 51)); //Orange
				fire(2);
			}
			else if(energy >= 40) {
				fire(1);
				setBulletColor(new Color(255, 255, 51)); //Yellow
			}
			else {
				setBulletColor(new Color(255, 255, 255)); //White
				fire(0.25);
			}
		}
		else if(distance > 200 && distance >= 280) {
			if(energy >= 30) {
				setBulletColor(new Color(255, 255, 51)); //Yellow
				fire(0.5);
			}
			else if(energy >= 20) {
				setBulletColor(new Color(0, 0, 0)); //White
				fire(0.25);
			}
		}
		else if(distance > 280 && distance < 500) {
			if(energy >= 40) {
				setBulletColor(new Color(255, 255, 51)); //Yellow
				fire(0.3);
			}
			setBulletColor(new Color(255, 255, 255)); //White
			fire(0.1);
		}
	}
	
	double normalizeBearing(double angle) {
		while (angle >  180) angle -= 360;
		while (angle < -180) angle += 360;
		return angle;
	}
}