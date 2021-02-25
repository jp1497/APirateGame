package uk.ac.york.sepr4.io;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector3;
import uk.ac.york.sepr4.GameInstance;
import uk.ac.york.sepr4.object.building.Building;
import uk.ac.york.sepr4.object.building.Department;
import uk.ac.york.sepr4.object.building.MinigameBuilding;
import uk.ac.york.sepr4.object.entity.Player;
import uk.ac.york.sepr4.screen.DepartmentScreen;
import uk.ac.york.sepr4.screen.EndScreen;
import uk.ac.york.sepr4.screen.MinigameScreen;
import java.util.Optional;

public class SailInputProcessor implements InputProcessor {

    private GameInstance gameInstance;

    public SailInputProcessor(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    //Added for Assessment 3: key listener for game events
    @Override
    public boolean keyDown(int keycode) {

        if (keycode == Input.Keys.E) {
            //attempt to enter building (successful if one in range of player)
            Optional<Building> optionalBuilding = gameInstance.getEntityManager().getPlayerLocation();
            if(optionalBuilding.isPresent()) {
                Building building = optionalBuilding.get();
                if(building instanceof Department) {
                    gameInstance.fadeSwitchScreen(new DepartmentScreen(gameInstance, (Department) building));
                    return true;
                } else if (building instanceof MinigameBuilding) {
                    gameInstance.fadeSwitchScreen(new MinigameScreen(gameInstance));
                    return true;
                }
            }
        }
        if(keycode == Input.Keys.NUM_1) {
            gameInstance.getEntityManager().getOrCreatePlayer().setSelectedCrewMember(Optional.empty());
            return true;
        }
        if(gameInstance.getCrewBank().getCrewKeys().contains(Input.Keys.toString(keycode))) {
            gameInstance.getEntityManager().getOrCreatePlayer().setSelectedCrewMember(
                    gameInstance.getCrewBank().getCrewFromKey(Input.Keys.toString(keycode)));
            return true;
        }

        //debug keycodes
        if(gameInstance.getGame().DEBUG) {
            if (keycode == Input.Keys.L) {
                // DEBUG code used to test minigame easily!
                gameInstance.fadeSwitchScreen(new MinigameScreen(gameInstance));
                return true;
            } else if(keycode == Input.Keys.MINUS) {
                //used to test end screen (death)
                gameInstance.fadeSwitchScreen(new EndScreen(gameInstance, true));
            }
        }

        if (keycode == Input.Keys.Q) {
            Gdx.app.debug("SIP", "Toggling Map On!");
            gameInstance.getStatsHUD().toggleMap(true);
            return true;
        }

        if (keycode == Input.Keys.ESCAPE) {
            //toggle pause menu on/off
            gameInstance.setPaused(!gameInstance.isPaused());
            return true;
        }

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.Q) {
            Gdx.app.debug("SIP", "Toggling Map Off!");
            gameInstance.getStatsHUD().toggleMap(false);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        //Added for Assessment 3: code for pausing game
        if (gameInstance.isPaused()) {
            return false;
        }
        if (button == Input.Buttons.LEFT) {
            Player player = gameInstance.getEntityManager().getOrCreatePlayer();
            Vector3 clickLoc = gameInstance.getSailScreen().getOrthographicCamera().unproject(new Vector3(screenX, screenY, 0));
            float fireAngle = (float) (-Math.atan2(player.getCentre().x - clickLoc.x, player.getCentre().y - clickLoc.y));
            Gdx.app.debug("SailScreen", "Firing: Click at (rad) " + fireAngle);
            //Added for Assessment 3: Allow player to use triple shot
            if (!player.fire(fireAngle)) {
                Gdx.app.debug("SailScreen", "Firing: Error! (cooldown?)");
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
