package uk.ac.york.sepr4.object.item;

import com.badlogic.gdx.Gdx;
import lombok.Data;

import java.util.Random;

@Data
public class RewardManager {

    private static Integer baseXP = 10, baseGold = 5;

    /***
     * Generates reward that scales with difficulty of enemy defeated.
     * @param difficulty difficulty of enemy (1-10)
     * @return reward (gold, xp) to give player
     */
    public static Reward generateReward(Integer difficulty) {
        Reward reward = new Reward(baseXP, baseGold);
        reward.setGold(reward.getGold() * difficulty);
        reward.setXp(reward.getXp() * difficulty);
        Gdx.app.debug("RM", "Generated Gold: "+reward.getGold()+", XP: "+reward.getXp()
        + " for difficulty: "+difficulty);
        return reward;
    }

}
