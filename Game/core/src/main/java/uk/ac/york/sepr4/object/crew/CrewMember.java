package uk.ac.york.sepr4.object.crew;

import lombok.Data;

@Data
public abstract class CrewMember {

    private String name, key;
    private Integer id, baseUpgradeCost, upgradeCostMultiplier, level = 1, maxLevel;
    private Double baseDamage;
    private float baseCooldown, currentCooldown = 0f;

    public CrewMember(Integer id, String name, String key, Double baseDamage,
                      Integer baseUpgradeCost, Integer upgradeCostMultiplier, Integer maxLevel, float baseCooldown) {
        this.id = id;
        this.name = name;
        this.key = key;
        this.baseDamage = baseDamage;
        this.baseUpgradeCost = baseUpgradeCost;
        this.upgradeCostMultiplier = upgradeCostMultiplier;
        this.maxLevel = maxLevel;
        this.baseCooldown = baseCooldown;
    }

    /***
     * Calculate cost of upgrade.
     * @return Integer cost of next upgrade.
     */
    public Integer getUpgradeCost() {
        return baseUpgradeCost*level;
    }

    //is crew member at max level
    public boolean canUpgrade() {
        return level<maxLevel;
    }

    //increase crew member level
    public void upgrade() {
        level++;
    }

    //implemented by extending classses
    public abstract boolean fire(float angle);

    public void decrementCooldown(float delta) {
        if(currentCooldown<delta) {
            currentCooldown = 0f;
        } else {
            currentCooldown-=delta;
        }
    }

    protected float getCooldown() {
        //each level reduces cooldown by 1 second
        return baseCooldown - (1*(level-1));
    }

    protected Double getDamage() {
        //each level increase damage by 50% of basedamage of each projectile fired by this crew member
        return baseDamage + (baseDamage * (level-1) * 0.5);
    }

}
