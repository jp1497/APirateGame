package uk.ac.york.sepr4.object.crew;

import com.badlogic.gdx.Gdx;
import uk.ac.york.sepr4.GameInstance;
import uk.ac.york.sepr4.object.entity.EntityManager;
import uk.ac.york.sepr4.object.entity.Player;

import java.util.Timer;
import java.util.TimerTask;

public class TripleShotCrew extends CrewMember {


    public TripleShotCrew() {
        super(5, "Triple Shot", "6", 5.0,
                100, 3, 5, 15f);
    }

    @Override
    public boolean fire(float angle) {
        if(getCurrentCooldown() == 0) {
            //can fire
            EntityManager entityManager = GameInstance.INSTANCE.getEntityManager();
            Player player = GameInstance.INSTANCE.getEntityManager().getOrCreatePlayer();
            entityManager.getProjectileManager().spawnProjectile(player, player.getSpeed(), angle, getDamage());
            entityManager.getAnimationManager().addFiringAnimation(player, angle - (float)Math.PI/2);
            setCurrentCooldown(getCooldown());

            Timer timer = new Timer();

            //start a task to fire two more projectiles later
            //TODO: Better implementation!
            TimerTask delayedThreadStartTask = new TimerTask() {
                @Override
                public void run() {
                    //moved to TimerTask
                    Gdx.app.postRunnable(() -> entityManager.getProjectileManager().spawnProjectile(player, player.getSpeed(), angle, getDamage()));
                }
            };
            TimerTask delayedThreadStartTask1 = new TimerTask() {
                @Override
                public void run() {
                    //moved to TimerTask
                    Gdx.app.postRunnable(() -> entityManager.getProjectileManager().spawnProjectile(player, player.getSpeed(), angle, getDamage()));
                }
            };

            timer.schedule(delayedThreadStartTask, 300); //0.3 seconds
            timer.schedule(delayedThreadStartTask1, 600);
            return true;
        }
        //cooling down
        return false;
    }
}
