package sigma;

import robocode.*;
import robocode.util.Utils;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class GIGACHAD extends AdvancedRobot {

    private static final int BINS = 47;
    private static final int MIDDLE_BIN = (BINS - 1) / 2;
    private ArrayList<EnemyWave> enemyWaves;
    private double[] surfStats;
    private Point2D.Double myLocation;
    private Point2D.Double enemyLocation;
    private Rectangle2D.Double fieldRectangle;
    private int turnsSinceLastDirectionChange = 0; // Adicionando o número de turnos desde a última mudança de direção
    private static final int MIN_TURNS_BETWEEN_DIRECTION_CHANGE = 20; // Aumentando o tempo mínimo entre as mudanças de direção

    public void run() {
        enemyWaves = new ArrayList<>();
        surfStats = new double[BINS];
        fieldRectangle = new Rectangle2D.Double(18, 18, getBattleFieldWidth() - 36, getBattleFieldHeight() - 36);

        setColors(Color.GRAY, Color.RED, Color.BLACK); // body, gun, radar
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        while (true) {
            turnRadarRight(360);
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        myLocation = new Point2D.Double(getX(), getY());

        double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
        double latVel = e.getVelocity() * Math.sin(e.getHeadingRadians() - absoluteBearing);
        double enemyDistance = e.getDistance();
        enemyLocation = project(myLocation, absoluteBearing, enemyDistance);

        setTurnRadarRightRadians(Utils.normalRelativeAngle(absoluteBearing - getRadarHeadingRadians()) * 2);

        EnemyWave ew = new EnemyWave();
        ew.fireTime = getTime();
        ew.bulletVelocity = Rules.getBulletSpeed(e.getEnergy() - getEnergy());
        ew.distanceTraveled = 0;
        ew.direction = (latVel >= 0) ? 1 : -1;
        ew.directAngle = absoluteBearing;
        ew.fireLocation = (Point2D.Double) enemyLocation.clone();
        enemyWaves.add(ew);

        updateWaves();
        doSurfing();

        if (enemyDistance < 150) {
            setFire(3);
        } else {
            setFire(2);
        }

        setTurnGunRightRadians(Utils.normalRelativeAngle(absoluteBearing - getGunHeadingRadians()));
    }

    private void updateWaves() {
        for (int i = 0; i < enemyWaves.size(); i++) {
            EnemyWave ew = enemyWaves.get(i);

            ew.distanceTraveled = (getTime() - ew.fireTime) * ew.bulletVelocity;
            if (ew.distanceTraveled > myLocation.distance(ew.fireLocation) + 50) {
                enemyWaves.remove(i);
                i--;
            }
        }
    }

    private EnemyWave getClosestSurfableWave() {
        double closestDistance = Double.POSITIVE_INFINITY;
        EnemyWave surfWave = null;

        for (int i = 0; i < enemyWaves.size(); i++) {
            EnemyWave ew = enemyWaves.get(i);
            double distance = myLocation.distance(ew.fireLocation) - ew.distanceTraveled;

            if (distance > ew.bulletVelocity && distance < closestDistance) {
                surfWave = ew;
                closestDistance = distance;
            }
        }

        return surfWave;
    }

    private void doSurfing() {
        EnemyWave surfWave = getClosestSurfableWave();

        if (surfWave == null) {
            return;
        }

        double dangerLeft = checkDanger(surfWave, -1);
        double dangerRight = checkDanger(surfWave, 1);

        double goAngle;
        if (dangerLeft < dangerRight) {
            goAngle = getBearing(surfWave) - Math.PI / 2;
        } else {
            goAngle = getBearing(surfWave) + Math.PI / 2;
        }

        // Limitar o tremulação verificando se passaram turnos suficientes desde a última mudança de direção
        if (turnsSinceLastDirectionChange >= MIN_TURNS_BETWEEN_DIRECTION_CHANGE) {
            goAngle = wallSmoothing(myLocation, goAngle, (dangerLeft < dangerRight ? -1 : 1));
            setBackAsFront(this, goAngle);
            turnsSinceLastDirectionChange = 0; // Resetar o contador
        } else {
            turnsSinceLastDirectionChange++; // Incrementar o contador
        }
    }

    private double checkDanger(EnemyWave surfWave, int direction) {
        int index = getFactorIndex(surfWave, direction);
        return surfStats[index];
    }

    private int getFactorIndex(EnemyWave surfWave, int direction) {
        double offsetAngle = (surfWave.directAngle - getBearing(surfWave)) * direction;
        double factor = Utils.normalRelativeAngle(offsetAngle) / maxEscapeAngle(surfWave.bulletVelocity) * direction;

        return (int) limit(0, (factor * ((BINS - 1) / 2)) + MIDDLE_BIN, BINS - 1);
    }

    private double getBearing(EnemyWave surfWave) {
        return Math.atan2(surfWave.fireLocation.x - getX(), surfWave.fireLocation.y - getY());
    }

    private double maxEscapeAngle(double velocity) {
        return Math.asin(8.0 / velocity);
    }

    private int limit(int min, double value, int max) {
        return (int) Math.max(min, Math.min(value, max));
    }

    private void setBackAsFront(AdvancedRobot robot, double goAngle) {
        double angle = Utils.normalRelativeAngle(goAngle - getHeadingRadians());
        if (Math.abs(angle) > Math.PI / 2) {
            if (angle < 0) {
                angle += Math.PI;
            } else {
                angle -= Math.PI;
            }
            setBack(100);
        } else {
            setAhead(100);
        }
        setTurnRightRadians(angle);
    }

    private double wallSmoothing(Point2D.Double botLocation, double angle, int orientation) {
        while (!fieldRectangle.contains(project(botLocation, angle, 160))) {
            angle += orientation * 0.05;
        }
        return angle;
    }

    private Point2D.Double project(Point2D.Double sourceLocation, double angle, double length) {
        return new Point2D.Double(sourceLocation.x + Math.sin(angle) * length,
                                  sourceLocation.y + Math.cos(angle) * length);
    }

    public void onHitByBullet(HitByBulletEvent e) {
        Point2D.Double hitBulletLocation = new Point2D.Double(e.getBullet().getX(), e.getBullet().getY());
        EnemyWave hitWave = null;

        for (int i = 0; i < enemyWaves.size(); i++) {
            EnemyWave ew = enemyWaves.get(i);
            if (Math.abs(ew.distanceTraveled - myLocation.distance(ew.fireLocation)) < 50
                    && Math.abs(Rules.getBulletSpeed(e.getBullet().getPower()) - ew.bulletVelocity) < 0.001) {
                hitWave = ew;
                break;
            }
        }

        if (hitWave != null) {
            logHit(hitWave, hitBulletLocation);
            enemyWaves.remove(hitWave);
        }
    }

    private void logHit(EnemyWave hitWave, Point2D.Double hitBulletLocation) {
        int index = getFactorIndex(hitWave, 1);

        for (int i = 0; i < surfStats.length; i++) {
            surfStats[i] += 1.0 / (Math.pow(index - i, 2) + 1);
        }
    }

    public void onBulletHitBullet(BulletHitBulletEvent e) {
        Bullet hitBullet = e.getHitBullet();
        Point2D.Double hitBulletLocation = new Point2D.Double(hitBullet.getX(), hitBullet.getY());
        EnemyWave hitWave = null;

        for (int i = 0; i < enemyWaves.size(); i++) {
            EnemyWave ew = enemyWaves.get(i);
            if (Math.abs(ew.distanceTraveled - myLocation.distance(ew.fireLocation)) < 50
                    && Math.abs(Rules.getBulletSpeed(hitBullet.getPower()) - ew.bulletVelocity) < 0.001) {
                hitWave = ew;
                break;
            }
        }

        if (hitWave != null) {
            logHit(hitWave, hitBulletLocation);
            enemyWaves.remove(hitWave);
        }
    }

    private class EnemyWave {
        Point2D.Double fireLocation;
        long fireTime;
        double bulletVelocity;
        double directAngle;
        double distanceTraveled;
        int direction;

        public EnemyWave() {
        }
    }
}

