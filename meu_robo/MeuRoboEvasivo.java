package meu_robo;

import robocode.*;
import java.awt.Color;

public class MeuRoboEvasivo extends AdvancedRobot {
    private int moveDirection = 1;

    @Override
    public void run() {
        setColors(Color.blue, Color.white, Color.red); // Define as cores do robô

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setAdjustRadarForRobotTurn(true);

        while (true) {
            setTurnRadarRight(360); // Continua girando o radar para procurar inimigos
            setAhead(100 * moveDirection);
            execute();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        double bearing = event.getBearing();
        double absoluteBearing = getHeading() + bearing;

        // Ajusta o radar para o robô inimigo
        setTurnRadarRight(normalRelativeAngleDegrees(absoluteBearing - getRadarHeading()));

        // Ajusta a mira para o robô inimigo
        setTurnGunRight(normalRelativeAngleDegrees(absoluteBearing - getGunHeading()));

        // Atira sempre que a arma estiver resfriada
        if (getGunHeat() == 0) {
            fire(2); // Tiro de potência média constante
        }

        // Movimenta-se de forma mais evasiva ao detectar um robô
        if (event.getDistance() < 150) {
            moveDirection = -moveDirection;
            setAhead(100 * moveDirection);
        } else {
            setTurnRight(bearing);
            setAhead(100);
        }
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        // Reverte a direção do movimento e dá um "dash" para frente ou para trás
        moveDirection = -moveDirection;
        setAhead(150 * moveDirection); // Aumenta a distância do "dash" para 150
        execute();
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        // Reverte a direção do movimento ao colidir com a parede
        moveDirection = -moveDirection;
        setBack(100 * moveDirection);
        execute();
    }
}
