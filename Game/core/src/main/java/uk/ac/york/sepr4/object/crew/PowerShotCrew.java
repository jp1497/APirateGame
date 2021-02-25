package uk.ac.york.sepr4.object.crew;

import uk.ac.york.sepr4.GameInstance;
import uk.ac.york.sepr4.object.entity.EntityManager;
import uk.ac.york.sepr4.object.entity.Player;

public class PowerShotCrew extends CrewMember {


    public PowerShotCrew() {
        super(4, "Power Shot", "5", 20.0,
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
            return true;
        }
        //cooling down
        return false;
    }
}
