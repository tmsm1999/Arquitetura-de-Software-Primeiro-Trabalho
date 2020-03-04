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
			}
		}
	}
	
	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e){
		double energyChange = previousEnergy - (e.getEnergy());
		setTurnRight(e.getBearing() + 90);
		if(energyChange >= 0.1 && energyChange <= 3){
			moveDirection *= -1;
			setAhead(100*moveDirection);
		}
		if(e.getDistance() > 700)
			ahead(100);
		double turn = getHeading() - getGunHeading() + e.getBearing();
		setTurnGunRight(normalizeBearing(turn));

		fireGoodAmount(getEnergy(), e.getDistance());
		double radarTurn = getHeadingRadians() + e.getBearingRadians() - getRadarHeadingRadians();
		setTurnRadarRightRadians(2.0*normalRelativeAngle(radarTurn));
		previousEnergy = e.getEnergy();
	}
	public void onHitByBullet(HitByBulletEvent e) {
	
	}
	
	public void onHitWall(HitWallEvent e) {
		
	}	
	
	public void onBulletMissed(BulletMissedEvent e) {
		missedBullets++;
	}
	
	public void onHitRobot(HitRobotEvent e) {

	}
	
	public void onStatus(StatusEvent e) {
		
	}
	
	public void onRoundEnded(RoundEndedEvent e) {
		
	}
	
	public void onBulletHit(BulletHitEvent e) {
		
	}
	
	private void fireGoodAmount(double en, double dis) {
		if(dis < 600) {
			if(en >= 90)
				fire(3);
			else if(en >= 80)
				fire(2);
			else if(en >= 40)
				fire(1);
			else
				fire(0.5);
		}
	 }
	double normalizeBearing(double angle) {
		while (angle >  180) angle -= 360;
		while (angle < -180) angle += 360;
		return angle;
	}
}