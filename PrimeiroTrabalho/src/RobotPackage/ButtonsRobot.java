/*
@author Jos� Ferreira
@author Tom�s Mamede
@version 1.2
@since 03/03/2020
*/

package RobotPackage;
import robocode.util.Utils;
import robocode.*;
import java.awt.*;

public class ButtonsRobot extends AdvancedRobot {
	
	private byte moveDirection = 1; //Dire��o definida para movimento da arma.
	private double previousEnergy = 100.0; //Vari�vel que guarda a energia do inimigo.
	private double hitsTaken = 0;
	
	public void run() {
		
		//Mudan�a das cores do robot.
		setBodyColor(new Color(0, 0, 0)); //Cor do corpo do robot = preto.
		setGunColor(new Color(255, 255, 255)); //Cor da arma do robot = branco.
		setRadarColor(new Color(192, 192, 192)); //Cor do radar = Cinzento claro.
	
		while(true){
			//Permite mover a arma e o radar do robot separadamente.
			setAdjustRadarForGunTurn(true);
			//Permite mover a arma e o corpo do robot separadamente.
			setAdjustGunForRobotTurn(true);
			
			//Este ciclo while tenta aumentar a probabilidade de detetar robot movendo o radar alternadamente.
			//A cada itera��o deste ciclo o robot muda de dire��o aleatoriamente movimentando-se um pouco na dire��o atual.
			while(true){
				turnRadarRightRadians(Double.POSITIVE_INFINITY);
				double r = Math.random();
				if(r < 0.5)
					moveDirection = -1;
				else
					moveDirection = 1;
				if(hitsTaken%5 == 0)
					setAhead(100 * moveDirection);
				else
					setTurnRight(90);
					setAhead(100 * moveDirection);
			}
		}
	}
	
	public void onScannedRobot(ScannedRobotEvent e) {
		
		//O root move-se perpendicularmente em rela��o �posi��o do inimigo.
		setTurnRight(e.getBearing() + 90);
		
		//Valor que guarda a mudan�a de energia do inimigo.
		double energyChange = previousEnergy - (e.getEnergy());
		
		//Se a diferen�a de energia parecer ter sido causada pelo disparo de uma bala - tentamos desviar.
		if(energyChange >= 0.1 && energyChange <= 3) {
			double r = Math.random();
			if(r < 0.5)
				moveDirection = -1;
			else
				moveDirection = 1;
			if(hitsTaken%5 == 0)
				setAhead(100 * moveDirection);
			else
				setTurnRight(90);
				setAhead(100 * moveDirection);
				setTurnRight(e.getBearing() + 90);
				
		}
		
		//Quando o inimigo est� muito longe, tentar aproximar.
		if(e.getDistance() > 700) {
			ahead(100);
		}
		
		//Tenta prever quando � que a bala deve ser disparada. Tempo atual + tempo que a bala demora a viajar pela arena.
		long time = getTime() + (int) (e.getDistance() / (20 - (3 * fireGoodAmount(getEnergy(), e.getDistance()))));
		
		//Soma entre heading atual e bearing m�dulo 180 graus, ou 2 * PI.
		double totalBearing = (getHeadingRadians() + e.getBearingRadians()) % (2*Math.PI);
		
		//Soma entre bearing e heading.
        double absBearing = e.getBearingRadians() + getHeadingRadians();
        
        //Dado que no robocode os graus s�o medidos na dire��o oposta, usamos:
        // - A fun��o seno para calcular o X.
        // - A fun��o cosseno para calcular o Y.
       
        //Recalculamos a posi��o do inimigo usando um tri�ngulo. Usamos a nossa localiza��o e o seno com a total bearing e a dist�ncia
        //Para calcular o X e a nossa localiza��o, o cosseno com do �ngulo, o total bearing e a dist�ncia para calcular o Y.
        double curX = getX()+Math.sin(totalBearing)*e.getDistance();
        double curY = getY()+Math.cos(totalBearing)*e.getDistance();
        

        //Para prever onde � que o inimigo vai, usamos uma fun��o auxiliar do tipo predict.
        double predX = predictX(curX, e.getHeading(), e.getVelocity(), getTime(), time);
        double predY = predictY(curY, e.getHeading(), e.getVelocity(), getTime(), time);;
        
        //Apontar a arma para a posi��o que foi prevista.
        double gunOffset = getGunHeadingRadians() - absBearing(getX(),getY(),predX,predY);
        setTurnGunLeftRadians(normaliseBearingRadians(gunOffset));
        fire(fireGoodAmount(getEnergy(), e.getDistance()));
        
        //Fazer o radar seguir o inimigo.
        setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians()) * 2);
        
        //Se o robot inimigo estiver perto, disparar.
		if(e.getDistance() < 80){	
			fire(fireGoodAmount(getEnergy(), e.getDistance()));
		}
		
		//Atualizar o valor de energia.
		previousEnergy = e.getEnergy();
	}
	
	//Robot afasta-se da parede quando embate.
	//@param evento com o robot inimigo.
	public void onHitWall(HitWallEvent e) {
		double robotBearing = e.getBearing();
		turnRight(-robotBearing);
		ahead(200);
	}	
	
	//Quando o nosso robot colide com outro, viramos a arma para a posi��o atual desse robot e disparamos.
	//@param evento com o robot inimigo.
	public void onHitRobot(HitRobotEvent e) {
		double turn = getHeading() - getGunHeading() + e.getBearing();
		setTurnGunRight(normalizeBearing(turn));
		fire(fireGoodAmount(getEnergy(), 100));
	}
	
	public void onHitByBullet(HitByBulletEvent e) {
		hitsTaken++;
	}
	
	
	//Fun��o que tenta prever qual a melhor a��o para o robot numa dada situa��o.
	//Esta fun��o tem como base a energia atual do nosso robot e a dist�ncia a que se encontra do outro robot.
	
	//@param energy (energia atual do robot e distance - dist�ncia atual do robot).
	private double fireGoodAmount(double energy, double distance) {	
		if(distance <= 80 && energy >= 25) {
			setBulletColor(new Color(255, 51, 51)); //Vermelho
			return 3;
		}
		else if(distance > 80 && distance <= 200) {
			if(energy >= 95) {
				setBulletColor(new Color(255, 51, 51)); //Vermelho
				return 3;
			}
			else if(energy < 90 && energy >= 70){
				setBulletColor(new Color(255, 153, 51)); //Laranja
				return 3;
			}
			else if(energy >= 40) {
				setBulletColor(new Color(255, 255, 51)); //Amarelo
				return 2;
			}
			else {
				setBulletColor(new Color(255, 255, 255)); //Branco
				return 1.5;
			}
		}
		else if(distance > 150 && distance <= 280) {
			if(energy >= 30) {
				setBulletColor(new Color(255, 255, 51)); //Amarelo
				return 2;
			}
			else if(energy >= 20) {
				setBulletColor(new Color(0, 0, 0)); //Branco
				return 1;
			}
		}
		else if(distance > 150 && distance < 500) {
			if(energy >= 40) {
				setBulletColor(new Color(255, 255, 51)); //Amarelo
				return 2;
			}
			setBulletColor(new Color(255, 255, 255)); //Branco
			return 1;
		}
		return 1;
	}
	
	//Normaliza o bearing em radianos.
	//@param ang � o �ngulo em randianos.
	//@return �ngulo normalizado.
	private double normaliseBearingRadians(double ang) {
        if(ang > Math.PI)
        	ang -= 2*Math.PI;
        if(ang < -Math.PI)
        	ang += 2*Math.PI;
        return ang;
	}
	
	//Normaliza o bearing em graus.
	//@param ang � o �ngulo em graus.
	//@return �ngulo normalizado.
	private double normalizeBearing(double angle) {
		while(angle >  180) 
			angle -= 360;
		while(angle < -180) 
			angle += 360;
		return angle;
	}
	
	//Para prever a posi��o X do inimigo, usamos a �ltima posi��o registada mais o seno da heading 
	//multiplicado pela velocidade e varia��o do tempo
	
	//@param posi��o atual, heading, velocidade, tempo inicial, tempo atual.
	//@return X da posi��o que se prev�.
	private double predictX(double curX, double heading, double vel, long startTime, long curTime) {
		return curX + Math.sin(heading) * vel * (curTime - startTime);
	}
	
	//Para prever a posi��o Y do inimigo, usamos a �ltima posi��o registada mais o cosseno da heading 
	//multiplicado pela velocidade e varia��o do tempo
	
	//@param posi��o atual, heading, velocidade, tempo inicial, tempo atual.
	//@return Y da posi��o que se prev�.
	private double predictY(double curY, double heading, double vel, long startTime, long curTime) {
		return curY + Math.cos(heading) * vel * (curTime - startTime);
	}
	
	//Calcular a dist�ncia entre dois pontos.
	//@param x1, y1, x2, y2 s�o as coordenadas
	//@return double com a dist�ncia
	public double distanceBetweenPoints(double x1, double y1, double x2, double y2){
            double disX = x2-x1;
            double disY = y2-y1;
            double h = Math.sqrt( disX*disX + disY*disY );
            return h;
    }
	
	// Function to calculate the absolute bearing given the quadrant of 
	// Which the distances between the X and Y coordinates would be a part of
	// Using the sin inverse trigonometric function
	
	//Fun��o que calcula o bearing absoluto dado o quadrante ao qual as dist�ncias entre as coordenadas
	//X e Y pertenceriam, usando a fun��o trigonom�trica arco seno
	public double absBearing(double x1, double y1, double x2, double y2) {
            double disX = x2-x1;
            double disY = y2-y1;
            
            double h = distanceBetweenPoints(x1, y1, x2, y2);
            
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