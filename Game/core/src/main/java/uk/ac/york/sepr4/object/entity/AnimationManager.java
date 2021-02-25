package uk.ac.york.sepr4.object.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import lombok.Getter;
import uk.ac.york.sepr4.io.FileManager;
import uk.ac.york.sepr4.object.entity.npc.NPCBoat;
import uk.ac.york.sepr4.object.entity.npc.NPCMonster;
import uk.ac.york.sepr4.utils.AIUtil;

import java.io.File;
import java.util.*;

public class AnimationManager {

    private EntityManager entityManager;

    //For cleanup
    private Array<Entity> lastFrameEffects = new Array<>(); //Needed for clean up
    @Getter
    private List<DeathAnimation> deathAnimations = new ArrayList<>();
    private List<FireAnimation> fireAnimations = new ArrayList<>();

    //Death Animations
    private Array<Entity> effects = new Array<>();
    //Water Trails
    private List<WaterTrail> waterTrails = new ArrayList<>();
    //Cannon "boom" animation
    private List<CannonExplosion> cannonExplosions = new ArrayList<>();

    public AnimationManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    //Takes the centre x,y of where you want the effect to appear
    public void addEffect(float x, float y, float angle, Texture texture, int width, int height, float alpha){
        Entity effect = new Entity(texture, new Vector2(x,y)) {};
        effect.setY(y - height/2);
        effect.setX(x - width/2);
        effect.setWidth(width);
        effect.setHeight(height);
        effect.setAlpha(alpha);
        effect.setAngle(angle);
        this.effects.add(effect);
    }

    /**
     * Removes all effects then adds all new effects
     * Effects work on a frame by frame basis so need to be spawned in every frame
     */
    public void handleEffects(Stage stage, float delta) {
        updateDeathAnimations(delta);
        updateWaterTrails();
        updateFiringAnimations();
        updateBoatFire();
        stage.getActors().removeAll(this.lastFrameEffects, true);

        for (Entity effect : effects) {
            if (!stage.getActors().contains(effect, true)) {
                stage.addActor(effect);
            }
        }

        this.lastFrameEffects = this.effects;
        this.effects = new Array<>();
    }

    public void addFiringAnimation(LivingEntity livingEntity, float firingAngle) {
        cannonExplosions.add(new CannonExplosion(livingEntity, firingAngle));
    }

    /***
     * Remove complete cannon effects and activate current ones.
     */
    private void updateFiringAnimations() {
        List<CannonExplosion> toRemove = new ArrayList<>();
        for(CannonExplosion cannonExplosion: cannonExplosions) {
            if(cannonExplosion.isComplete()) {
                toRemove.add(cannonExplosion);
            } else {
                cannonExplosion.spawnEffects(this);
            }
        }
    }


    /***
     * Add new, remove complete and then activate current boat fire animations.
     */
    private void updateBoatFire() {
        for(LivingEntity livingEntity : entityManager.getLivingEntities()) {
            if(livingEntity.isOnFire()) {
                boolean isAdded = false;
                for(FireAnimation fireAnimation : fireAnimations) {
                    if(fireAnimation.getLE().equals(livingEntity)) {
                        isAdded = true;
                        break;
                    }
                }
                if(!isAdded) {
                    fireAnimations.add(new FireAnimation(livingEntity));
                }
            } else {
                FireAnimation toRemove = null;
                for(FireAnimation fireAnimation : fireAnimations) {
                    if(fireAnimation.getLE().equals(livingEntity)) {
                        toRemove = fireAnimation;
                        break;
                    }
                }
                if(toRemove !=null) {
                    fireAnimations.remove(toRemove);
                }
            }
        }

        for(FireAnimation fireAnimation:fireAnimations) {
            fireAnimation.spawnEffects(this);
        }
    }

    //TODO: Could be cleaned up
    /***
     * Add new, remove complete and then activate current water trails.
     */
    private void updateWaterTrails() {
        List<WaterTrail> toRemove = new ArrayList<>();
        for(WaterTrail waterTrail : waterTrails) {
            if(waterTrail.getLE() instanceof NPCBoat) {
                //remove dead NPCs trail
                if(!entityManager.getNpcList().contains((NPCBoat) waterTrail.getLE(), false)){
                    toRemove.add(waterTrail);
                } else {
                    //if not dead, update effects
                    waterTrail.spawnEffects(this);
                }
            } else if(waterTrail.getLE() instanceof Player) {
                //remove dead players trail
                if(entityManager.getOrCreatePlayer().isDead()) {
                    toRemove.add(waterTrail);
                } else {
                    //if not dead, update effects
                    waterTrail.spawnEffects(this);
                }
            } else {
                Gdx.app.error("AnimationManager", "Trail found for unknown LE");
                toRemove.add(waterTrail);
            }
        }
        waterTrails.removeAll(toRemove);
    }

    public void createWaterTrail(LivingEntity livingEntity) {
        waterTrails.add(new WaterTrail(livingEntity));
    }

    /***
     * Add new, remove complete and then activate current boat death animations.
     */
    private void updateDeathAnimations(float delta) {
        //add dead NPCs if not yet animating
        for(LivingEntity livingEntity : entityManager.getLivingEntities()) {
            if(livingEntity.isDying()) {
                boolean isAdded = false;
                for(DeathAnimation dA : deathAnimations) {
                    if(dA.getLE().equals(livingEntity)) {
                        isAdded = true;
                        break;
                    }
                }
                if(!isAdded) {
                    deathAnimations.add(new DeathAnimation(livingEntity));
                }
            }
        }

        List<DeathAnimation> toRemove = new ArrayList<>();
        for(DeathAnimation deathAnimation : deathAnimations) {
            if(deathAnimation.getDeathTimer() <= 5) {
                deathAnimation.spawnEffects(this, delta);
            } else {
                toRemove.add(deathAnimation);
            }
        }
        deathAnimations.removeAll(toRemove);
    }

}

class DeathAnimation {
    @Getter
    private LivingEntity lE;
    private int frame = 1;
    @Getter
    private float deathTimer = 0f;

    public DeathAnimation(LivingEntity lE) {
        this.lE = lE;
        lE.setTexture(FileManager.DEAD_ENEMY);
        lE.setAlpha(1-(deathTimer/5));
    }

    public void spawnEffects(AnimationManager animationManager, float delta) {
        animationManager.addEffect(lE.getCentre().x, lE.getCentre().y, lE.getAngle(),
                FileManager.deathFrame(frame), 40, 40, 1);

        deathTimer+=delta;
        if (deathTimer > 5){
            //animation over -- set dead
            lE.setDead(true);
            lE.setDying(false);
            return;
        } else {
            if(frame == 3) {
                frame = 1;
            } else {
                frame ++;
            }
        }
    }
}

class FireAnimation {
    @Getter
    private LivingEntity lE;
    private int frame = 1;

    public FireAnimation(LivingEntity lE) {
        this.lE = lE;
    }
    public void spawnEffects(AnimationManager animationManager) {
        animationManager.addEffect(lE.getCentre().x,
                lE.getCentre().y,
                lE.getAngle(),
                FileManager.boatFireFrame(frame),
                (int)lE.getWidth(),
                (int)lE.getHeight(),
                1);
        if(frame==17) {
            frame=1;
        } else {
            frame++;
        }
    }
}

class CannonExplosion {
    private LivingEntity lE;
    private Float firingAngle;
    private int frame = 1;

    public CannonExplosion(LivingEntity lE, float firingAngle) {
        this.lE = lE;
        this.firingAngle = firingAngle;
    }

    public boolean isComplete() {
        return (frame==21);
    }

    public void spawnEffects(AnimationManager animationManager) {
        animationManager.addEffect(AIUtil.getXwithAngleandDistance(lE.getCentre().x,
                firingAngle + (float)Math.PI/2, 50),
                AIUtil.getYwithAngleandDistance(lE.getCentre().y,
                        firingAngle + (float)Math.PI/2, 50),
                firingAngle, FileManager.firingFrame(frame),
                70, 50, 1);
        frame++;
    }
}

class WaterTrail {
    private List<Vector2> lTrails = new ArrayList<>(), rTrails = new ArrayList<>();
    @Getter
    private LivingEntity lE;
    
    public WaterTrail(LivingEntity livingEntity) {
        this.lE = livingEntity;
    }
    
    public void spawnEffects(AnimationManager animationManager) {
        shiftTrails();
        lTrails.add(new Vector2(AIUtil.getXwithAngleandDistance(lE.getCentre().x, (float) (lE.getAngle() - 7 * Math.PI / 8), 50f),
                AIUtil.getYwithAngleandDistance(lE.getCentre().y, (float) (lE.getAngle() - 7 * Math.PI / 8), 45f)));
        rTrails.add(new Vector2(AIUtil.getXwithAngleandDistance(lE.getCentre().x, (float) (lE.getAngle() + 7 * Math.PI / 8), 50f),
                AIUtil.getYwithAngleandDistance(lE.getCentre().y, (float) (lE.getAngle() + 7 * Math.PI / 8), 45f)));


        for (int i = 0; i < lTrails.size() - 1; i++) {
            float xM = getXmidPoint(lTrails.get(i).x, lTrails.get(i + 1).x);
            float yM = getYmidPoint(lTrails.get(i).y, lTrails.get(i + 1).y);
            float angleP = getAngleToPoint(lTrails.get(i).x, lTrails.get(i).y, lTrails.get(i + 1).x, lTrails.get(i + 1).y) + (float)Math.PI/2;
            float distance = getDistanceToPoint(lTrails.get(i).x, lTrails.get(i).y, lTrails.get(i + 1).x, lTrails.get(i + 1).y);

            float xM2 = getXmidPoint(rTrails.get(i).x, rTrails.get(i + 1).x);
            float yM2 = getYmidPoint(rTrails.get(i).y, rTrails.get(i + 1).y);
            float angleP2 = getAngleToPoint(rTrails.get(i).x, rTrails.get(i).y, rTrails.get(i + 1).x, rTrails.get(i + 1).y) + (float)Math.PI/2;
            float distance2 = getDistanceToPoint(rTrails.get(i).x, rTrails.get(i).y, rTrails.get(i + 1).x, rTrails.get(i + 1).y);


            if (distance > 0.1) {
                if (i < lTrails.size() / 4) {
                    animationManager.addEffect(xM, yM, angleP, FileManager.MIDDLEBOATTRAIL1, (int)(distance + 5), 10,0.1f);
                    animationManager.addEffect(xM2, yM2, angleP2,  FileManager.MIDDLEBOATTRAIL1, (int)(distance2 + 5), 10,0.1f);
                } else if (i < lTrails.size() / 2) {
                    animationManager.addEffect(xM, yM, angleP,  FileManager.MIDDLEBOATTRAIL1, (int)(distance + 5), 10,0.2f);
                    animationManager.addEffect(xM2, yM2, angleP2,  FileManager.MIDDLEBOATTRAIL1, (int)(distance2 + 5), 10,0.2f);
                } else if (i < 3 * lTrails.size() / 4) {
                    animationManager.addEffect(xM, yM, angleP,  FileManager.MIDDLEBOATTRAIL1, (int)(distance + 5), 10,0.3f);
                    animationManager.addEffect(xM2, yM2, angleP2,  FileManager.MIDDLEBOATTRAIL1, (int)(distance2 + 5), 10,0.3f);
                } else {
                    animationManager.addEffect(xM, yM, angleP,  FileManager.MIDDLEBOATTRAIL1, (int)(distance + 5), 10,0.5f);
                    animationManager.addEffect(xM2, yM2, angleP2, FileManager.MIDDLEBOATTRAIL1, (int)(distance2 + 5), 10,0.5f);
                }
            }
        }

    }

    private float getXmidPoint(float x1, float x2) {
        if (x2 > x1){
            return (x1+(x2-x1)/2);
        } else {
            return (x1-(x2-x1)/2);
        }
    }

    private float getYmidPoint(float y1, float y2) {
        if (y2 > y1){
            return (y1+(y2-y1)/2);
        } else {
            return (y1-(y2-y1)/2);
        }
    }

    private float getAngleToPoint(float x1, float y1, float x2, float y2) {
        double d_angle = Math.atan(((y2 - y1) / (x2 - x1)));
        if(x2 < x1){
            d_angle += Math.PI;
        }
        float angle = (float)d_angle + (float)Math.PI/2;
        return angle;
    }

    private float getDistanceToPoint(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
    }

    private void shiftTrails() {
        if(lTrails.size() >= 60) {
            lTrails.remove(0);
            rTrails.remove(0);
        }
    }
}