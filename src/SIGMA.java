package sigma;

import robocode.*;
import java.awt.Color;

public class SIGMA extends AdvancedRobot {
    private int moveDirection = 1;
    private boolean targetLocked = false;

    @Override
    public void run() {
        setColors(Color.blue, Color.white, Color.red); // cores do robo

        setAdjustGunForRobotTurn(true); // Mantém o canhão ajustado enquanto o robô se move
        setAdjustRadarForGunTurn(true); // Mantém o radar ajustado enquanto o canhão se move
        setAdjustRadarForRobotTurn(true); // Mantém o radar ajustado enquanto o robô se move

        while (true) {
            if (!targetLocked) {
                setTurnRadarRight(360); // gira o radar para procurar por inimigos
            }
            execute();
        }
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        // Trava o movimento e dá ré por cerca de 30 ticks (30*0.1 segundos)
        stop(); // para o robô
        back(100); // dá ré por 100 unidades (aproximadamente 30 ticks)
        moveDirection = -moveDirection; // inverte a direção de movimento
        execute();
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        double bearing = event.getBearing();
        double absoluteBearing = getHeading() + bearing;

        // Trava o radar no robô inimigo
        setTurnRadarRight(normalRelativeAngleDegrees(absoluteBearing - getRadarHeading()));

        // Ajuste da mira para o robô inimigo
        setTurnGunRight(normalRelativeAngleDegrees(absoluteBearing - getGunHeading()));

        // Tiro com potência ajustada para reduzir o tempo de resfriamento
        if (getGunHeat() == 0) {
            fire(1.5); // tiro com potência constante
        }

        targetLocked = true; // travar o radar no inimigo
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        // Muda a direção e se move para sair da linha de fogo
        moveDirection = -moveDirection;
        setAhead(150 * moveDirection); // aumenta a distância para sair da linha de fogo
        execute();
    }

    @Override
    public void onRobotDeath(RobotDeathEvent event) {
        // Desbloqueia o radar se o alvo morrer
        targetLocked = false;
    }
}
