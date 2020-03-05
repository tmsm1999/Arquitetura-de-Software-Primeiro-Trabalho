package RobotPackage;
import robocode.*;
import robocode.util.Utils;
import java.awt.*;

public class ButtonsRobot extends AdvancedRobot {
	private byte moveDirection = 1;
	private double previousEnergy = 100.0;
	
	public void run() {
		// Change robot's body, gun and radar colors
		setBodyColor(new Color(0, 0, 0));
		setGunColor(new Color(255, 255, 255));
		setRadarColor(new Color(192, 192, 192));
	
		while(true){
			// Separate radar from gun
			setAdjustRadarForGunTurn(true);
			// Separate gun from body
			setAdjustGunForRobotTurn(true);
			
			while(true){
				// Scan all around for robots
				turnRadarRightRadians(Double.POSITIVE_INFINITY);
				// Move left/right alternately
				moveDirection *= -1;
				setAhead(100*moveDirection);
			}
		}
	}
	
	public void onScannedRobot(ScannedRobotEvent e) {
		
		// Get in a perpendicular position towards the enemy
		setTurnRight(e.getBearing() + 90);
		
		// Value that stores enemies change in energy
		double energyChange = previousEnergy - (e.getEnergy());
		
		// If the enemie's energy change looks like a shot bullet, try to dodge
		if(energyChange >= 0.1 && energyChange <= 3) {
			moveDirection *= -1;
			setAhead(100*moveDirection);
		}
		
		// If the enemy is too far away, try to get closer
		if(e.getDistance() > 700) {
			ahead(100);
		}
		
		// To calculate the time at which we should it the bullet, 
		//we sum the current time and the time that the bullet takes to travel (distance/speed)
		long time = getTime() + (int)(e.getDistance()/(20-(3*fireGoodAmount(getEnergy(), e.getDistance()))));
		
		// Total bearing is going to be the sum between the heading and the bearing module 180 degrees, or 2*PI radians
		double totBear = (getHeadingRadians()+e.getBearingRadians())%(2*Math.PI);
		
		// The absolute bearing is simply the sum between the bearing and the heading
        double absBearing = e.getBearingRadians() + getHeadingRadians();
        
       
        // Since in robocode the degrees are measured on the opposite direction, we use the sin function to calculate the X
        // And the cos function to calculate the Y
       
        // We can calculate the enemies position using a triangle, by using our location, the sin of the total bearing and the distance
        double curX = getX()+Math.sin(totBear)*e.getDistance();
        double curY = getY()+Math.cos(totBear)*e.getDistance();
        
        // In order to predict where the enemy might end up, we will use an auxiliary function that will be explained further
        double predX = predictX(curX, e.getHeading(), e.getVelocity(), getTime(), time);
        double predY = predictY(curY, e.getHeading(), e.getVelocity(), getTime(), time);;
        
        // Point the gun to the predicted position and fire away!
        double gunOffset = getGunHeadingRadians() - absbearing(getX(),getY(),predX,predY);
        setTurnGunLeftRadians(normaliseBearingRadians(gunOffset));
        fire(fireGoodAmount(getEnergy(), e.getDistance()));
        
        // Make the radar follow the enemy
        setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians()) * 2);
        
        // If the target is very close, fire
		if(e.getDistance() < 80){	
			fire(fireGoodAmount(getEnergy(), e.getDistance()));
		}
		
		// Update the previous energy value
		previousEnergy = e.getEnergy();
	}
	
	// If a wall is hit, back off!
	public void onHitWall(HitWallEvent e) {
		double robotBearing = e.getBearing();
		turnRight(-robotBearing);
		ahead(200);
	}	
	
	// If a robot is hit turn the gun to it's position and fire again
	public void onHitRobot(HitRobotEvent e) {
		double turn = getHeading() - getGunHeading() + e.getBearing();
		setTurnGunRight(normalizeBearing(turn));
		fire(fireGoodAmount(getEnergy(), 100));
	}
	
	
	// Function to try to calculate the best power that the bullet should be shot at
	private double fireGoodAmount(double energy, double distance) {	
		if(distance <= 80 && energy >= 25) {
			setBulletColor(new Color(255, 51, 51)); //Red
			return 3;
		}
		else if(distance > 80 && distance <= 200) {
			if(energy >= 95) {
				setBulletColor(new Color(255, 51, 51)); //Red
				return 3;
			}
			else if(energy < 90 && energy >= 70){
				setBulletColor(new Color(255, 153, 51)); //Orange
				return 2;
			}
			else if(energy >= 40) {
				setBulletColor(new Color(255, 255, 51)); //Yellow
				return 1;
			}
			else {
				setBulletColor(new Color(255, 255, 255)); //White
				return 0.25;
			}
		}
		else if(distance > 200 && distance >= 280) {
			if(energy >= 30) {
				setBulletColor(new Color(255, 255, 51)); //Yellow
				return 0.5;
			}
			else if(energy >= 20) {
				setBulletColor(new Color(0, 0, 0)); //White
				return 0.25;
			}
		}
		else if(distance > 280 && distance < 500) {
			if(energy >= 40) {
				setBulletColor(new Color(255, 255, 51)); //Yellow
				return 0.3;
			}
			setBulletColor(new Color(255, 255, 255)); //White
			return 0.1;
		}
		return 0;
	}
	
	// Normalize the bearing in radians
	double normaliseBearingRadians(double ang) {
        if(ang > Math.PI)
        	ang -= 2*Math.PI;
        if(ang < -Math.PI)
        	ang += 2*Math.PI;
        return ang;
	}
	
	// Normalize the bearing in degrees
	double normalizeBearing(double angle) {
		while(angle >  180) 
			angle -= 360;
		while(angle < -180) 
			angle += 360;
		return angle;
	}
	
	// To predict the Y position of the enemy, we use it's last seen position plus the sin of the 
	// heading multiplied by its speed and the time variation
	double predictX(double curX, double heading, double vel, long startTime, long curTime) {
		return curX + Math.sin(heading)*vel*(curTime-startTime);
	}
	
	// To predict the Y position of the enemy, we use it's last seen position plus the cos of the 
	// heading multiplied by its speed and the time variation
	double predictY(double curY, double heading, double vel, long startTime, long curTime) {
		return curY + Math.cos(heading)*vel*(curTime-startTime);
	}
	
	// Simple function to calculate the distance between two points
	public double dis2Points(double x1, double y1, double x2, double y2){
            double disX = x2-x1;
            double disY = y2-y1;
            double h = Math.sqrt( disX*disX + disY*disY );
            return h;
    }
	
	// Function to calculate the absolute bearing given the quadrant of 
	// Which the distances between the X and Y coordinates would be a part of
	// Using the sin inverse trigonometric function
	public double absbearing(double x1, double y1, double x2, double y2) {
            double disX = x2-x1;
            double disY = y2-y1;
            double h = dis2Points( x1,y1, x2,y2 );
            if(disX > 0 && disY > 0) {
                    return Math.asin(disX / h);
            }
            if(disX > 0 && disY < 0) {
                    return Math.PI - Math.asin(disX / h);
            }
            if(disX < 0 && disY < 0){
                    return Math.PI + Math.asin(-disX / h);
            }
            if(disX < 0 && disY > 0){
                    return 2.0*Math.PI - Math.asin(-disX / h);
            }
            return 0;
    }
    
}