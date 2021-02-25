package uk.ac.york.sepr4.object.crew;

import com.badlogic.gdx.Gdx;
import uk.ac.york.sepr4.GameInstance;
import uk.ac.york.sepr4.object.entity.EntityManager;
import uk.ac.york.sepr4.object.entity.Player;

import java.util.Timer;
import java.util.TimerTask;

public class DoubleShotCrew extends CrewMember {


    public DoubleShotCrew() {
        super(1, "Double Shot", "2", 5.0,
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

            //start a task to fire another projectile later
            TimerTask delayedThreadStartTask = new TimerTask() {
                @Override
                public void run() {
                    //moved to TimerTask
                    Gdx.app.postRunnable(() -> entityManager.getProjectileManager().spawnProjectile(player, player.getSpeed(), angle, getDamage()));
                }
            };

            timer.schedule(delayedThreadStartTask, 300); //0.3 seconds
            return true;
        }
        //cooling down
        return false;
    }
}
