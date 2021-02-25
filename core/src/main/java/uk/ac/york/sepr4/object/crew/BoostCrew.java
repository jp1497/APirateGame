package uk.ac.york.sepr4.object.crew;

import uk.ac.york.sepr4.GameInstance;
import uk.ac.york.sepr4.object.entity.Player;

public class BoostCrew extends CrewMember {


    public BoostCrew() {
        super(3, "Boost", "4", 0.0,
                100, 3, 5, 15f);
    }

    @Override
    public boolean fire(float angle) {
        if(getCurrentCooldown() == 0) {
            //can fire
            Player player = GameInstance.INSTANCE.getEntityManager().getOrCreatePlayer();
            //"boost" player forward
            player.setSpeed(350f);
            setCurrentCooldown(getCooldown());
            return true;
        }
        //cooling down
        return false;
    }
}
