package RobotPackage;
import robocode.util.Utils;
import robocode.*;
import java.awt.*;

public class ButtonsRobot extends AdvancedRobot {
	
	private byte moveDirection = 1; //Direção definida para movimento da arma.
	private double previousEnergy = 100.0; //Variável que guarda a energia do enimigo.
	
	public void run() {
		//Mudança das cores do robô.
		
		setBodyColor(new Color(0, 0, 0)); //Cor do corpo do robô = preto.
		setGunColor(new Color(255, 255, 255)); //Cor da arma do robô = branco.
		setRadarColor(new Color(192, 192, 192)); //Cor do radar = Cinzento claro.
	
		while(true){
			//Permite mover a arma e o radar do robô separadamente.
			setAdjustRadarForGunTurn(true);
			//Permite mover a arma e o corpo do robô separadamente.
			setAdjustGunForRobotTurn(true);
			
			//Este ciclo while tentar aumentar a probabilidade de detatar robôs movendo o radar alternadamente.
			//A cada iteração deste ciclo o robô muda de direção.
			while(true){
				turnRadarRightRadians(Double.POSITIVE_INFINITY);
				moveDirection *= -1;
				setAhead(100 * moveDirection);
			}
		}
	}
	
	public void onScannedRobot(ScannedRobotEvent e) {
		
		//O robô move-se perpendicularmente em relação à posição do enimigo.
		setTurnRight(e.getBearing() + 90);
		
		//Valor que guarda a mudança de energia do enimigo.
		double energyChange = previousEnergy - (e.getEnergy());
		
		//Se a diferença de energia parecer ter sido causada pelo disparo de uma bala - tentamos desviar.
		if(energyChange >= 0.1 && energyChange <= 3) {
			moveDirection *= -1;
			setAhead(100*moveDirection);
		}
		
		//Quando o enimigo está muito longe tentar aproximar.
		if(e.getDistance() > 700) {
			ahead(100);
		}
		
		//Tenta prever quando é que a bala deve ser disparada. Tempo atual + tempo que a bala demora a viajar pela arena.
		long time = getTime() + (int) (e.getDistance() / (20 - (3 * fireGoodAmount(getEnergy(), e.getDistance()))));
		
		//Soma entre heading atual e bearing módulo 180 graus, ou 2 * PI.
		double totalBearing = (getHeadingRadians() + e.getBearingRadians()) % (2*Math.PI);
		
		//Soma entre bearing e heading.
        double absBearing = e.getBearingRadians() + getHeadingRadians();
        
        //Dado que no robocode os graus são medidos na direção oposta, usamos:
        // - A função seno para calcular o X.
        // - A função cosseno para calcular o Y.
       
        //Recalculamos a posição do enimigo usando um triângulo. Usamos a nossa localização e o seno com a total bearing e a distância.
        double curX = getX()+Math.sin(totalBearing)*e.getDistance();
        double curY = getY()+Math.cos(totalBearing)*e.getDistance();
        

        //Para prever onde é que o inimigo vai, usamos uma função auxiliar do tipo predict.
        double predX = predictX(curX, e.getHeading(), e.getVelocity(), getTime(), time);
        double predY = predictY(curY, e.getHeading(), e.getVelocity(), getTime(), time);;
        
        //Apontar a arma para a posição que foi prevista.
        double gunOffset = getGunHeadingRadians() - absBearing(getX(),getY(),predX,predY);
        setTurnGunLeftRadians(normaliseBearingRadians(gunOffset));
        fire(fireGoodAmount(getEnergy(), e.getDistance()));
        
        //Fazer o radar seguir o inimigo.
        setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians()) * 2);
        
        //Se o robô inimigo estiver perto disparar.
		if(e.getDistance() < 80){	
			fire(fireGoodAmount(getEnergy(), e.getDistance()));
		}
		
		//Atualizar o valor de energia.
		previousEnergy = e.getEnergy();
	}
	
	//Robô afasta-se da parede quando embate.
	//@param evento com o robô enimigo.
	public void onHitWall(HitWallEvent e) {
		double robotBearing = e.getBearing();
		turnRight(-robotBearing);
		ahead(200);
	}	
	
	//Quando um robô colide com outro viramos a arma para a posição atual desse robô e disparamos.
	//@param evento com o robô enimigo.
	public void onHitRobot(HitRobotEvent e) {
		double turn = getHeading() - getGunHeading() + e.getBearing();
		setTurnGunRight(normalizeBearing(turn));
		fire(fireGoodAmount(getEnergy(), 100));
	}
	
	
	//Função que tenta prever qual a melhor ação para o robô numa dada situação.
	//Esta função tem como base a energia atual do nosso robô e a distância a que se encontra do outro robô.
	
	//@param energy (energia atual do robô e distance - distância atual do robô).
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
				return 2;
			}
			else if(energy >= 40) {
				setBulletColor(new Color(255, 255, 51)); //Amarelo
				return 1;
			}
			else {
				setBulletColor(new Color(255, 255, 255)); //Branco
				return 0.25;
			}
		}
		else if(distance > 200 && distance >= 280) {
			if(energy >= 30) {
				setBulletColor(new Color(255, 255, 51)); //Amarelo
				return 0.5;
			}
			else if(energy >= 20) {
				setBulletColor(new Color(0, 0, 0)); //Branco
				return 0.25;
			}
		}
		else if(distance > 280 && distance < 500) {
			if(energy >= 40) {
				setBulletColor(new Color(255, 255, 51)); //Amarelo
				return 0.3;
			}
			setBulletColor(new Color(255, 255, 255)); //Branco
			return 0.1;
		}
		return 0;
	}
	
	//Normaliza o bearing em radianos.
	//@param ang é o ângulo em randianos.
	//@return ângulo normalizado.
	private double normaliseBearingRadians(double ang) {
        if(ang > Math.PI)
        	ang -= 2*Math.PI;
        if(ang < -Math.PI)
        	ang += 2*Math.PI;
        return ang;
	}
	
	//Normaliza o bearing em graus.
	//@param ang é o ângulo em graus.
		//@return ângulo normalizado.
	private double normalizeBearing(double angle) {
		while(angle >  180) 
			angle -= 360;
		while(angle < -180) 
			angle += 360;
		return angle;
	}
	
	//Para prever a posição X do inimigo, usamos a última posição registada mais o seno da heading 
	//multiplicado pela velocidade e variação do tempo
	
	//@param posição atual, heading, velocidade, tempo inicial, tempo atual.
	//@return X da posição atual.
	private double predictX(double curX, double heading, double vel, long startTime, long curTime) {
		return curX + Math.sin(heading) * vel * (curTime - startTime);
	}
	
	//Para prever a posição Y do inimigo, usamos a última posição registada mais o cosseno da heading 
	//multiplicado pela velocidade e variação do tempo
	
	//@param posição atual, heading, velocidade, tempo inicial, tempo atual.
	//@return X da posição atual.
	private double predictY(double curY, double heading, double vel, long startTime, long curTime) {
		return curY + Math.cos(heading) * vel * (curTime - startTime);
	}
	
	//Calcular a distância entre dois pontos.
	//@param x1, y1, x2, y2 são as coordenadas
	//@return double com a distância
	public double distanceBetweenPoints(double x1, double y1, double x2, double y2){
            double disX = x2-x1;
            double disY = y2-y1;
            double h = Math.sqrt( disX*disX + disY*disY );
            return h;
    }
	
	// Function to calculate the absolute bearing given the quadrant of 
	// Which the distances between the X and Y coordinates would be a part of
	// Using the sin inverse trigonometric function
	
	//Função que calcula a bearing absoluta entre o quadrante 
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