package uk.ac.york.sepr4.object.entity.npc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import lombok.Data;
import uk.ac.york.sepr4.GameInstance;
import uk.ac.york.sepr4.object.building.College;
import uk.ac.york.sepr4.object.entity.Entity;
import uk.ac.york.sepr4.object.entity.LivingEntity;
import uk.ac.york.sepr4.object.entity.Player;
import uk.ac.york.sepr4.object.projectile.Projectile;
import uk.ac.york.sepr4.utils.AIUtil;

import java.util.Optional;

@Data
public abstract class NPCEntity extends LivingEntity {

    private float range = 1000f; //How far away it can see livingEntities/objects
    private float accuracy = 0.5f; //This is how accurate this is (currently is (1/0.5)*Math.PI/32 which allows for that much range on both sides of the perfect shot and picks a random angle from that range

    private Optional<LivingEntity> lastTarget = Optional.empty(); //This is the target currently being fought

    private boolean previousTurn = true; //right = true, left = false this is used to be able to take infomation over frames it is the prevoius turn the NPC did
    private boolean turning = false; //This is wether the boat is turning or not. ~In conjuction these to help turnPreCalc determine the angularSpeed across multiple frames

    private int dodging = 0; //Not dodging = 0 anything other than 0 is meaning dodging. this is the amount of frames you want the NPC to dodge for
    //Adjusted - should be 1-10 for correct balancing
    private Integer difficulty;

    private float targetCheck = 3f; //Timer so target check aint every frame


    public NPCEntity(Texture texture, Vector2 pos, Integer difficulty) {
        super(texture, pos);
        this.difficulty = difficulty;
    }

    /***
     *  This is the control logic of the NPCs AI. It uses functions from mainly AIUtil to be able to make decisions on how it is meant to behave.
     *  They are broken down into sections as to be able to make the code and control structure easier to read.
     *  When calling this function it will actually make the NPC that is in the world do the actions.
     *
     * @param deltaTime time since last act
     */
    public void act(float deltaTime) {
        AIUtil.actNPCEntity(this, deltaTime);
        checkDespawn();
        super.act(deltaTime);
    }

    /***
     * Check whether Entity is far enough away from the player to despawn.
     */
    private void checkDespawn() {
        Player player = GameInstance.INSTANCE.getEntityManager().getOrCreatePlayer();

        if (this.distanceFrom(player) > 3000) {
            setDead(true);
            Gdx.app.debug("NPCEntity", "Too far from player, despawning!");
            if(this instanceof NPCBoat) {
                NPCBoat npcBoat = (NPCBoat) this;
                if(npcBoat.isBoss() && npcBoat.getAllied().isPresent()) {
                    //if boss is spawning, set spawned to false (so it will spawn again)
                    College allied = npcBoat.getAllied().get();
                    allied.setBossSpawned(false);
                }
            }
        }
    }

    /**
     * Checks whether the target passed to it is a good target to choose and can actually be chosen
     *
     * @param optionalLivingEntity
     * @return true if valid target, false otherwise
     */
    private boolean validTarget(Optional<LivingEntity> optionalLivingEntity) {
        if (optionalLivingEntity.isPresent()) {
            LivingEntity livingEntity = optionalLivingEntity.get();
            if (!(livingEntity.isDying() || livingEntity.isDead())) {
                if (livingEntity.distanceFrom(this) <= getRange()) {
                    //if last target exists, not dead and is still in range
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Knits together all the target functions to be able to get a correct target in the range of the NPC
     *
     * @return theTarget
     */
    public Optional<LivingEntity> getTarget() {
        if (validTarget(this.lastTarget)) {
            //Gdx.app.debug("Target", "Last");
            return this.lastTarget;
        } else {
            this.lastTarget = Optional.empty();
            if (targetCheck > 4f) {
                //Gdx.app.debug("Target", "Nearest");
                targetCheck = 0f;
                return getNearestTarget();
            } else {
                return Optional.empty();
            }
        }
    }

    /**
     * Is an extension of the getLivingEntitiesInRange() so that it removes the target in the array being passed back of all the entitys in the range of the NPC
     *
     * @param target
     * @return Array of all livingEntitys in the range of NPC - itself and target
     */
    public Array<LivingEntity> getLivingEntitiesInRangeMinusTarget(LivingEntity target) {
        Array<LivingEntity> nearby = getLivingEntitiesInRange();
        if (nearby.contains(target, false)) {
            nearby.removeValue(target, false);
        }
        return nearby;
    }

    /**
     * returns all livingEntities in the range of the NPC except itself
     *
     * @return Array of livingEntities in range of NPC - itself
     */
    protected Array<LivingEntity> getLivingEntitiesInRange() {
        Array<LivingEntity> nearby = GameInstance.INSTANCE.getEntityManager().getLivingEntitiesInArea(getRangeArea());
        if (nearby.contains(this, false)) {
            nearby.removeValue(this, false);
        }
        return nearby;
    }

    /**
     * Gets all projectiles in range of NPC
     *
     * @return Array of all projectiles in the range of the NPC
     */
    public Array<Projectile> getProjectilesInRange() {
        Array<Projectile> nearby = GameInstance.INSTANCE.getEntityManager().getProjectileManager().getProjectileInArea(getRangeArea());
        return nearby;
    }

    /**
     * Returns an array of all the projectiles that are going to collide with the NPC out of the ones that have been passed to the function
     *
     * @param projectiles (All projectiles that want to be checked)
     * @return An array of projectiles that will collide with the NPC from the Array given to this function
     */
    public Array<Entity> getProjectilesToDodge(Array<Projectile> projectiles) {
        Array<Entity> projectilesToDodge = new Array<Entity>();
        for (Projectile projectile : projectiles) {
            //Checkout NPC Functions 2 but rather than the source being a NPC and the target being the target, the AI is now the target and the source is the projectile on the loop of iteration
            float thetaToThisInFuture = AIUtil.perfectAngleToCollide(projectile, this, projectile.getSpeed());
            float thetaActual = AIUtil.normalizeAngle(projectile.getAngle());
            float dist = (float) projectile.distanceFrom(this);
            boolean isTriangle = true;
            float theta;
            if (thetaToThisInFuture < thetaActual && thetaActual - thetaToThisInFuture < Math.PI / 2) {
                theta = thetaActual - thetaToThisInFuture;
            } else if (thetaActual < thetaToThisInFuture && thetaToThisInFuture - thetaActual < Math.PI / 2) {
                theta = thetaToThisInFuture - thetaActual;
            } else if (thetaActual < thetaToThisInFuture && (2 * Math.PI - thetaToThisInFuture) + thetaActual < Math.PI / 2) {
                theta = (float) (2 * Math.PI - thetaToThisInFuture) + thetaActual;
            } else if (thetaToThisInFuture < thetaActual && (2 * Math.PI - thetaActual) + thetaToThisInFuture < Math.PI / 2) {
                theta = (float) (2 * Math.PI - thetaActual) + thetaToThisInFuture;
            } else {
                theta = 0;
                isTriangle = false;
            }

            //Check out NPC Functions 1
            if (isTriangle == true) {
                float opp = (float) Math.tan(theta) * dist;
                if (opp < 0) {
                    opp = -opp;
                }
                if (opp < Math.max(3 * this.getRectBounds().height / 4, 3 * this.getRectBounds().width / 4)) {
                    projectilesToDodge.add(projectile);
                }
            }

        }
        return projectilesToDodge;
    }

    /**
     * Used in conjunction with the target selection stuff to be able to pick the nearest target to the NPC
     * Tries to always pick player
     *
     * @return the nearest target
     */
    protected Optional<LivingEntity> getNearestTarget() {
        Player player = GameInstance.INSTANCE.getEntityManager().getOrCreatePlayer();
        Array<LivingEntity> nearby = getLivingEntitiesInRange();
            //target player
            if (nearby.contains(player, false)) {
                //if player is in range - target
                //Gdx.app.debug("NPCBoat", "Got nearby player");

                return Optional.of(player);
        }
        //player not nearby
        if (nearby.size > 0) {
            Optional<LivingEntity> nearest = Optional.empty();
            for (LivingEntity livingEntity : nearby) {
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
            return nearest;
        }
        //Gdx.app.debug("NPCBoat", "No nearby enemy");
        return Optional.empty();
    }

    private Rectangle getRangeArea() {
        Rectangle radius = getRectBounds();
        radius.set(radius.x - range, radius.y - range, radius.width + 2 * range, radius.height + 2 * range);
        return radius;
    }


    /**
     * This is needed to be able to make the AI turn like the player does because it doesn't have a person key up it doesn't know to change angular speed
     * This is needed to be called before any setAngle() made by the npc
     *
     * @param right = true if turning right, else is a left turn this is only used if turning = true
     */
    public void turnPreCalcs(boolean right) {
        if (this.previousTurn == true && right == false || this.previousTurn == false && right == true || this.turning == false) {
            setAngularSpeed(0);
        } else {
            if (right) {
                setAngularSpeed(-getTurningSpeed());
            } else {
                setAngularSpeed(getTurningSpeed());
            }
        }
    }



}
