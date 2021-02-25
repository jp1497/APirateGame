package uk.ac.york.sepr4.object.crew;

import uk.ac.york.sepr4.GameInstance;
import uk.ac.york.sepr4.io.FileManager;
import uk.ac.york.sepr4.object.entity.EntityManager;
import uk.ac.york.sepr4.object.entity.Player;

public class FireShotCrew extends CrewMember {
    public FireShotCrew() {
        super(2, "Fire Shot", "3", 3.0, 150, 3, 3, 10f);
    }

    @Override
    public boolean fire(float angle) {
        if(getCurrentCooldown() == 0) {
            //can fire
            EntityManager entityManager = GameInstance.INSTANCE.getEntityManager();
            Player player = GameInstance.INSTANCE.getEntityManager().getOrCreatePlayer();
            entityManager.getProjectileManager().spawnProjectile(player, FileManager.CANNONBALL_FIRE,
                    player.getSpeed(), angle, getDamage(), true);
            entityManager.getAnimationManager().addFiringAnimation(player, angle - (float)Math.PI/2);
            setCurrentCooldown(getCooldown());

            return true;
        }
        return false;
    }
}
