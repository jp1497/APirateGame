package uk.ac.york.sepr4.object.entity.npc;

import com.badlogic.gdx.math.Vector2;
import lombok.Data;
import uk.ac.york.sepr4.GameInstance;
import uk.ac.york.sepr4.io.FileManager;
import uk.ac.york.sepr4.object.entity.EntityManager;
import uk.ac.york.sepr4.object.projectile.Projectile;

@Data
public class NPCMonster extends NPCEntity {

    private Integer spriteFrame = 1;
    private float spriteUpdate = 0.05f;

    public NPCMonster(Vector2 pos, Integer difficulty) {
        super(FileManager.krackenFrame(1), pos, difficulty);
        //half speed and slower firing but more damage
        setMaxSpeed(50f);
        setDamage(4.0);
        setReqCooldown(1.2f);
    }

    /***
     *  This is the control logic of the NPCs AI. It uses functions from mainly AIUtil to be able to make decisions on how it is meant to behave.
     *  They are broken down into sections as to be able to make the code and control structure easier to read.
     *  When calling this function it will actually make the NPC that is in the world do the actions.
     *
     * @param deltaTime time since last act
     */
    public void act(float deltaTime) {
        upateKrakenSprite(deltaTime);
        super.act(deltaTime);
    }

    /***
     * Overrides from LivingEntity.
     * NPCMonster should not take damage (as per requirements, must be avoided!)
     * @param projectile which damaged LivingEntity
     * @return true (still alive)
     */
    @Override
    public boolean damage(Projectile projectile) {
        return true;
    }

    /***
     * Overrides from LivingEntity
     * NPCMonster should have different projectile texture and no cannon firing animation!
     * @param angle angle at which to fire
     * @return
     */
    @Override
    public boolean fire(float angle) {
        EntityManager entityManager = GameInstance.INSTANCE.getEntityManager();
        if (getCurrentCooldown() >= getReqCooldown()) {
            setCurrentCooldown(0f);
            entityManager.getProjectileManager().spawnProjectile( this, FileManager.KRAKEN_WAVE,
                    getSpeed(), angle, getDamage(), false);
            return true;
        }

        return false;
    }

    /***
     * Update Kraken Sprite to next frame if enough time has passed since last update.
     * @param delta time since last render
     */
    private void upateKrakenSprite(float delta) {
        if(spriteUpdate <= delta) {
            spriteUpdate = 0.05f;
            if (spriteFrame == 17) {
                spriteFrame = 1;
            } else {
                spriteFrame++;
            }
            setTexture(FileManager.krackenFrame(spriteFrame));
        } else {
            spriteUpdate-=delta;
        }
    }


}
