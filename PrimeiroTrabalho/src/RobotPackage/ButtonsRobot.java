package RobotPackage;
import robocode.*;
import java.awt.*;

public class ButtonsRobot extends AdvancedRobot {
	
	double battleFieldHeight = getBattleFieldHeight();
	double battleFieldWidth = getBattleFieldWidth();
	
	int missedBullets = 0;
	int hitBullets = 0;
	
	byte scanDirection = 1;
	
	public void run() {
		
		setBodyColor(new Color(0, 0, 0));
		setGunColor(new Color(255, 255, 255));
		setRadarColor(new Color(192, 192, 192));
		
		double heading = getHeading();
		System.out.println(heading);
	
		while(true) {
			// Replace the next 4 lines with any behavior you would like
			setAdjustRadarForRobotTurn(true);
			while(true) {
				turnRadarRight(360);
			}
		}
	}
	
	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		// Replace the next line with any behavior you would like
		setTurnGunRight(getHeading() - getGunHeading() + e.getBearing()); //Points the gun to the opponent.
		
		double bulletStrength = Math.min(400 / e.getDistance(), 3);
		
		if(bulletStrength >= 0 && bulletStrength < 1) {
			setBulletColor(new Color(255, 255, 51)); //Yellow
		}
		else if(bulletStrength >= 1 && bulletStrength < 2) {
			setBulletColor(new Color(255, 153, 51)); //Orange
		}
		else {
			setBulletColor(new Color(255, 51, 51)); //Red
		}
		
		setFireBullet(Math.min(400 / e.getDistance(), 3));
		
		scanDirection *= -1; // changes value from 1 to -1
		setTurnRadarRight(360 * scanDirection);
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	//Turn perpendicularly to the bullet path.
	public void onHitByBullet(HitByBulletEvent e) {
		// Replace the next line with any behavior you would like
		back(10);
		turnRight(90 - e.getBearing());
	}
	
	/**
	 * onHitWall: What to do when you hit a wall
	 */
	public void onHitWall(HitWallEvent e) {
		// Replace the next line with any behavior you would like
		double robotBearing = e.getBearing();
		turnRight(-robotBearing);
		ahead(200);
	}	
	
	public void onBulletMissed(BulletMissedEvent e) {
		missedBullets++;
	}
	
	public void onHitRobot(HitRobotEvent e) {
		//Face the robot.
		turnRight(e.getBearing());

		//Fire on him!
		if (e.getEnergy() > 16) {
			fire(3);
		} else if (e.getEnergy() > 10) {
			fire(2);
		}
		
		ahead(40); //Smash him!
	}
	
	public void onStatus(StatusEvent e) {
		
	}
	
	public void onRoundEnded(RoundEndedEvent e) {
		
	}
	
	public void onBulletHit(BulletHitEvent e) {
		
		double xPosition = getX();
		double yPosition = getY();
		double heading = getHeading();
		
		if(xPosition <= battleFieldWidth / 2) {
			if(yPosition <= battleFieldHeight / 2) {
				while(heading != 0) {
					turnRight(1);
				}
			}
			else {
				while(heading != 90) {
					turnRight(1);
				}
			}
		}
		else if(xPosition > battleFieldWidth / 2) {
			if(yPosition <= battleFieldHeight / 2) {
				while(heading != 0) {
					turnRight(1);
				}
			}
			else {
				while(heading != 90) {
					turnRight(1);
				}
			}
		}
		
		scanDirection *= -1; // changes value from 1 to -1
		setTurnRadarRight(360 * scanDirection);
		
		hitBullets++;
	}
}