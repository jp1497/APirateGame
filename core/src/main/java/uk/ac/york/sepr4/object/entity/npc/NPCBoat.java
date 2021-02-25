package uk.ac.york.sepr4.object.entity.npc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import lombok.Data;
import uk.ac.york.sepr4.GameInstance;
import uk.ac.york.sepr4.object.building.College;
import uk.ac.york.sepr4.object.entity.LivingEntity;
import uk.ac.york.sepr4.object.entity.Player;
import uk.ac.york.sepr4.object.item.Reward;
import uk.ac.york.sepr4.object.item.RewardManager;
import uk.ac.york.sepr4.object.projectile.Projectile;
import java.util.Optional;

@Data
public class NPCBoat extends NPCEntity {

    //NPCBoat-specific variables
    private Optional<College> allied = Optional.empty(); //This is the faction the boat is allied with

    private boolean isBoss;

    public NPCBoat(Texture texture, Vector2 pos, Integer difficulty) {
        super(texture, pos, difficulty);

    }

    /**
     * Checks whether the livingEntity is part of the same college/faction
     * However if the NPC is below a certain health it will be scared so will fight targets,
     * usually only happens when player is shooting allied targets so they will fight back
     *
     * @param livingEntity
     * @return true if they are allied, false if not
     */
    private boolean areAllied(LivingEntity livingEntity) {
        if (livingEntity instanceof Player) {
            Player player = (Player) livingEntity;
            if (getAllied().isPresent()) {
                //Implements the if they have low health fight
                if (getHealth() < 3 * getMaxHealth() / 4) {
                    return false;
                }
                return player.getCaptured().contains(getAllied().get());
            }
        } else if(livingEntity instanceof NPCBoat) {
            //must be an NPCBoat
            NPCBoat npcBoat = (NPCBoat) livingEntity;
            if (npcBoat.getAllied().isPresent() && getAllied().isPresent()) {
                return (npcBoat.getAllied().get().equals(getAllied().get()));
            }
        }

        return false;
    }

    /**
     * Used in conjunction with the target selection stuff to be able to pick the nearest target to the NPC
     * Tries to always pick player
     *
     * @return the nearest target
     */
    @Override
    protected Optional<LivingEntity> getNearestTarget() {
        Player player = GameInstance.INSTANCE.getEntityManager().getOrCreatePlayer();
        Array<LivingEntity> nearby = getLivingEntitiesInRange();
        if (!areAllied(player)) {
            //not allied - target player
            if (nearby.contains(player, false)) {
                //if player is in range - target
                //Gdx.app.debug("NPCBoat", "Got nearby player");

                return Optional.of(player);
            }
        }
        //player has captured this NPCs allied college
        if (nearby.size > 0) {
            Optional<LivingEntity> nearest = Optional.empty();
            for (LivingEntity livingEntity : nearby) {
                if (!areAllied(livingEntity)) {
                    if (nearest.isPresent()) {
                        if (nearest.get().distanceFrom(this) > livingEntity.distanceFrom(this)) {
                            //closest enemy
                            nearest = Optional.of(livingEntity);
                            //Gdx.app.debug("NPCBoat", "Got closer nearby enemy");

                        }
                    } else {
                        nearest = Optional.of(livingEntity);
                        //Gdx.app.debug("NPCBoat", "Got new nearby enemy");
                    }
                }
            }
            return nearest;
        }
        //Gdx.app.debug("NPCBoat", "No nearby enemy");
        return Optional.empty();
    }

    /***
     * Override
     * @param projectile which damaged LivingEntity
     * @return true if NPCBoat still alive
     */
    @Override
    public boolean damage(Projectile projectile) {
        //if (projectile.getShooter() instanceof NPCBoat) {
        //    //if shooter is NPCBoat, dont damage (NPCs shouldnt be able to damage eachother)
        //    return true;
        //}
        if (!super.damage(projectile)) {
            //is dead
            if(projectile.getShooter() instanceof Player) {
                Gdx.app.debug("NPCBoat", "Issuing reward to player!");
                Player player = (Player) projectile.getShooter();
                Reward reward = RewardManager.generateReward((int) getDifficulty());
                player.issueReward(reward);

                if (getAllied().isPresent()) {
                    College allied = getAllied().get();
                    if (isBoss) {
                        Gdx.app.debug("NPCBoat", "Boss defeated - capturing allied college!");
                        player.capture(allied);
                        Gdx.app.debug("NPCBoat", "Unlocked crew member: " + allied.getCrewMember().getName());
                        player.addCrewMember(allied.getCrewMember());
                    } else {
                        if (allied.getBossSpawnThreshold() > 0) {
                            allied.decrementBossSpawnThreshold();
                        }
                    }
                }
            }

            return false;
        }
        return true;
    }

}
