package uk.ac.york.sepr4.object.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import lombok.Data;
import uk.ac.york.sepr4.GameInstance;
import uk.ac.york.sepr4.hud.HealthBar;
import uk.ac.york.sepr4.object.projectile.Projectile;
import uk.ac.york.sepr4.utils.AIUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public abstract class LivingEntity extends Entity {

    private Double health = 10.0, maxHealth = 10.0, damage = 4.0;
    private boolean isAccelerating, isBraking, isDead, isDying;
    private float currentCooldown = 0f, reqCooldown = 0.8f, maxSpeed = 140f,
            angularSpeed = 0f, acceleration = 60f, turningSpeed = 2f, onFire = 0f, fireDmgCooldown;

    //TODO: Better ways to monitor this
    private int collidedWithIsland = 0, colliedWithBoat = 0;

    private HealthBar healthBar;

    private List<UUID> animationIDs = new ArrayList<>();

    public LivingEntity(Texture texture, Vector2 pos) {
        super(texture, pos);

        this.healthBar = new HealthBar(this);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }

    public void kill(boolean silent) {
        //if not silent, death animation will appear
        this.isDying = !silent;
        this.isDead = silent;
    }

    /***
     * Called to action collision action (boat reversal)
     * @param withBoat true if collision was with another LivingEntity (boat)
     */
    public void collide(boolean withBoat, float thetaTP) {
        if(withBoat) {
            setColliedWithBoat(10);
            setAngle(thetaTP);
        } else {
            setCollidedWithIsland(10);
            setAngle(AIUtil.normalizeAngle(getAngle() - (float) Math.PI));
        }
        //setAngle(AIUtil.normalizeAngle(getAngle() - (float) Math.PI));
        if (getSpeed() > getMaxSpeed() / 5) {
            setSpeed(getMaxSpeed() / 5);
        }
    }

    public boolean isOnFire() {
        return onFire > 0f;
    }

    public HealthBar getHealthBar() {
        this.healthBar.update();
        return this.healthBar;
    }

    /***
     * LivingEntity update/act method
     * @param deltaTime time since last render
     */
    @Override
    public void act(float deltaTime) {

        //decrease weapon cooldown
        setCurrentCooldown(getCurrentCooldown() + deltaTime);
        if(isOnFire()) {
            //if on fire decrease fire time
            if(getOnFire()>deltaTime) {
                setOnFire(getOnFire() - deltaTime);
            } else {
                setOnFire(0f);
            }
        }

        if (!this.isDying) {
            //if not dying

            if(isOnFire()) {
                //if on fire take damage
                if (fireDmgCooldown - deltaTime < 0) {
                    setHealth(getHealth() - 0.5);
                    setFireDmgCooldown(0.5f);
                } else {
                    fireDmgCooldown-=deltaTime;
                }
            }

            //change speed based on acceleration
            float speed = getSpeed();
            if (isAccelerating) {
                if (speed > maxSpeed) {
                    speed = maxSpeed;
                } else {
                    //Changed for Assessment 3: acceleration is now a variable
                    speed += acceleration * deltaTime;
                }
            } else if (isBraking) {
                if (speed > 0) {
                    speed -= 80f * deltaTime;
                }
            } else {
                if (speed > 0) {
                    speed -= 30f * deltaTime;
                }
            }
            setSpeed(speed);
            super.act(deltaTime);
        }
    }

    /***
     * Called to inflict damage on LivingEntity
     * @param projectile which damaged LivingEntity
     * @return true if LivingEntity alive
     */
    public boolean damage(Projectile projectile) {
        this.health = this.health - projectile.getDamage();
        if (this.health <= 0) {
            kill(false);
            Gdx.app.debug("LE", "LE died.");
            return false;
        }
        if(projectile.isOnFire()) {
            setOnFire(5f);

        }
        return true;
    }

    /***
     * Called when a LivingEntity is to fire a shot.
     * @param angle angle at which to fire
     * @return true if cooldown sufficient and shot has been fired
     */
    public boolean fire(float angle) {
        EntityManager entityManager = GameInstance.INSTANCE.getEntityManager();
            if (getCurrentCooldown() >= getReqCooldown()) {
                setCurrentCooldown(0f);
                entityManager.getProjectileManager().spawnProjectile( this, getSpeed(), angle, getDamage());
                entityManager.getAnimationManager().addFiringAnimation(this,angle - (float)Math.PI/2);
                return true;
            }

        return false;
    }
}
