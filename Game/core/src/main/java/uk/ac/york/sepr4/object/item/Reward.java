package uk.ac.york.sepr4.object.item;

import lombok.Data;

@Data
public class Reward {

    private Integer xp;
    private Integer gold;

    public Reward(Integer xp, Integer gold) {
        this.xp = xp;
        this.gold = gold;
    }

}
