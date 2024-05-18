package meu_robo;

import robocode.*;
import java.awt.Color;

public class MeuRoboEvasivo extends AdvancedRobot {
    private int moveDirection = 1;
    private int zigZagCounter = 0; // contador para o movimento em zigue-zague

    @Override
    public void run() {
        setColors(Color.blue, Color.white, Color.red); // cores do robo

        setAdjustGunForRobotTurn(true); // Mantém o canhão ajustado enquanto o robô se move
        setAdjustRadarForGunTurn(true); // Mantém o radar ajustado enquanto o canhão se move
        setAdjustRadarForRobotTurn(true); // Mantém o radar ajustado enquanto o robô se move

        while (true) {
            setTurnRadarRight(360); // giro para procurar por inimigos

            // Movimentação em zigue-zague
            if (zigZagCounter % 20 == 0) {
                moveDirection = -moveDirection;
                setTurnRight(45 * moveDirection);
            }
            zigZagCounter++;
            setAhead(100 * moveDirection);
            execute();
        }
    }
    
    @Override
    public void onHitWall(HitWallEvent event) {
        // caso ele colida com algo troque o movimento
        stop(); // para o robô
        turnRight(180); // gira 180 graus
        moveDirection = -moveDirection; // inverte a direção de movimento
        ahead(100 * moveDirection); // continua andando na nova direção
        execute();
        
        // Caso detecte um robô vai se mover de forma mais aleatória
        if (event.getDistance() < 150) {
            moveDirection = -moveDirection; // muda a direção do movimento
            setAhead(100 * moveDirection);
        } else {
            setTurnRight(bearing);
            setAhead(100);
        }
    }
    
    
    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        double bearing = event.getBearing();
        double absoluteBearing = getHeading() + bearing;
        
        // "Radar" para procurar inimigos
        setTurnRadarRight(normalRelativeAngleDegrees(absoluteBearing - getRadarHeading()));
        
        // Ajuste da mira para o robô inimigo
        setTurnGunRight(normalRelativeAngleDegrees(absoluteBearing - getGunHeading()));
        
        // Tiro com potência ajustada para reduzir o tempo de resfriamento
        if (getGunHeat() == 0) {
            fire(1.5); // tiro com potência constante
        }
    }
    
    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        // caso leve muitos tiros em um curto período, mude a direção
        moveDirection = -moveDirection;
        setAhead(150 * moveDirection); // aumenta a distância para sair da linha de fogo
        execute();
    }
}
