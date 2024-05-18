package meu_robo;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import java.awt.Color;

public class MeuRoboEvasivo extends AdvancedRobot {
    private boolean movimentoFrente = true; // Flag para controlar a direção do movimento

    @Override
    public void run() {
        setColors(Color.blue, Color.white, Color.red); // Define as cores do robô
        
        setAdjustGunForRobotTurn(true); // Mantém o canhão ajustado enquanto o robô se move
        setAdjustRadarForGunTurn(true); // Mantém o radar ajustado enquanto o canhão se move
        setAdjustRadarForRobotTurn(true); // Mantém o radar ajustado enquanto o robô se move
        
        while (true) {
            // Movimenta-se continuamente para frente ou para trás
            if (movimentoFrente) {
                setAhead(Double.POSITIVE_INFINITY);
            } else {
                setBack(Double.POSITIVE_INFINITY);
            }
            execute();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        // Ajusta o radar para o robô inimigo
        double bearing = event.getBearing();
        double absoluteBearing = getHeading() + bearing;
        setTurnRadarRight(normalRelativeAngleDegrees(absoluteBearing - getRadarHeading()));
        
        // Ajusta a mira para o robô inimigo
        setTurnGunRight(normalRelativeAngleDegrees(absoluteBearing - getGunHeading()));
        
        // Atira se a arma estiver resfriada
        if (getGunHeat() == 0) {
            double distance = event.getDistance();
            if (distance < 200) {
                fire(3); // Tiro forte para alvos próximos
            } else {
                fire(1); // Tiro fraco para alvos distantes
            }
        }
        
        // Movimenta-se em direção ao inimigo
        setTurnRight(bearing);
        if (event.getDistance() > 300) {
            setAhead(100);
        } else {
            setBack(100);
        }
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        // Reverte a direção do movimento quando atingido por uma bala
        movimentoFrente = !movimentoFrente;
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        // Reverte a direção do movimento ao colidir com a parede
        movimentoFrente = !movimentoFrente;
    }
}
