package uk.ac.york.sepr4.object.entity.npc;

import com.badlogic.gdx.math.Vector2;
import uk.ac.york.sepr4.io.FileManager;
import uk.ac.york.sepr4.object.building.College;

import java.util.Optional;
import java.util.Random;

public class NPCBuilder {

    public NPCBuilder() {}

    //Changed for Assessment 3: changed the factory method to create a new boat instead of creating another NPCBuilder
    //Removed the getters and setters for the builder itself as they are never used when using a factory properly
    /**
     * Generate an enemy NPCBoat from base stats and difficulty
     * @param pos The position the NPCBoat is to have
     * @param allied The college the NPCBoat is allied to
     * @param difficulty Arbitrary difficulty value which determines health, damage, speed and accuracy
     * @param isBoss Is the NPCBoat a college boss
     * @return An NPCBoat with correct stats
     */
    public NPCBoat generateRandomEnemyBoat(Vector2 pos, Optional<College> allied, Integer difficulty, boolean isBoss) {
        Random random = new Random();

        NPCBoat npcBoat;
        if(allied.isPresent()) {
            if (isBoss) {
                npcBoat = new NPCBoat(FileManager.BOSS, pos, difficulty);
            } else {
                npcBoat = new NPCBoat(FileManager.COLLEGE_ENEMY, pos, difficulty);
            }
        } else {
            npcBoat = new NPCBoat(FileManager.ENEMY, pos, difficulty);
        }

        npcBoat.setAngle((float) (2*Math.PI*random.nextDouble()));;
        npcBoat.setAccuracy(npcBoat.getAccuracy());
        //10f extra speed per difficulty level
        npcBoat.setMaxSpeed(npcBoat.getMaxSpeed() + (difficulty*10f));
        //5.0 extra health per difficulty
        npcBoat.setMaxHealth(npcBoat.getMaxHealth() + (difficulty*5.0));
        npcBoat.setHealth(npcBoat.getMaxHealth());
        //0.25f extra turning speed per level
        npcBoat.setTurningSpeed(npcBoat.getTurningSpeed() + (difficulty*0.25f));
        npcBoat.setAllied(allied);
        npcBoat.setBoss(isBoss);
        //2.0 extra damage per level
        npcBoat.setDamage(npcBoat.getDamage() + (difficulty*2.0));
        //0.05s less cooldown per level
        npcBoat.setReqCooldown(npcBoat.getReqCooldown() - (difficulty*0.05f));

        return npcBoat;
    }

    /**
     * Generate an enemy NPCMonster from base stats and difficulty
     * @param pos The position the NPCMonster is to have
     * @param difficulty Arbitrary difficulty value which determines health, damage, speed and accuracy
     * @return An NPCMonster with correct stats
     */
    public NPCMonster generateRandomMonster(Vector2 pos, Integer difficulty) {
        Random random = new Random();

        NPCMonster npcMonster = new NPCMonster(pos, difficulty);

        npcMonster.setAngle((float) (2*Math.PI*random.nextDouble()));;
        //5f extra speed per difficulty level
        npcMonster.setMaxSpeed(npcMonster.getMaxSpeed() + (difficulty*5f));
        //0.5f extra turning speed per level
        npcMonster.setTurningSpeed(npcMonster.getTurningSpeed() + (difficulty*0.5f));
        npcMonster.setDamage(npcMonster.getDamage() + (difficulty / 50));
        //4.0 extra damage per level
        npcMonster.setDamage(npcMonster.getDamage() + (difficulty*4.0));
        //0.05s less cooldown per level
        npcMonster.setReqCooldown(npcMonster.getReqCooldown() - (difficulty*0.05f));

        return npcMonster;
    }

}
