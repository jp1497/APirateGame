package uk.ac.york.sepr4.object.projectile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import lombok.Data;
import uk.ac.york.sepr4.object.entity.Entity;
import uk.ac.york.sepr4.object.entity.LivingEntity;

@Data
public class Projectile extends Entity {

    private LivingEntity shooter;
    private Double damage = 5.0;
    private Integer baseSpeed = 125;

    private boolean active = true, onFire = false;

    /**
     * @param shooter The entity shooting the projectile
     * @param speed Speed of the projectile
     * @param angle Angle at which the projectile is shot
     * @param damage Damage dealt on impact by projectile
     */
    public Projectile(LivingEntity shooter, Texture texture, float speed, float angle, double damage){
         super(texture, shooter.getCentre());

        this.shooter = shooter;

        setAngle(angle);
        setSpeed(speed + baseSpeed);
        setDamage(damage);
    }

    /***
     * Move projectile and despawn if too far from shooter.
     * @param deltaTime time since last render
     */
    @Override
    public void act(float deltaTime) {
        if(this.distanceFrom(shooter) > 1000) {
            Gdx.app.debug("Projectile","Clearing up distant projectile!");
            this.active = false;
        } else {
            super.act(deltaTime);
        }
    }
}
