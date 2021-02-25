package uk.ac.york.sepr4.io;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import uk.ac.york.sepr4.screen.MinigameScreen;

public class MinigameInputProcessor implements InputProcessor {

    private MinigameScreen minigameScreen;

    public MinigameInputProcessor(MinigameScreen minigameScreen) {
        this.minigameScreen = minigameScreen;
    }

    @Override
    public boolean keyDown(int keycode) {
        if(keycode == Input.Keys.SPACE) {
            //start game if difficulty set but game not started
            if (minigameScreen.getDifficulty() != null && !minigameScreen.isGameStarted()) {
                minigameScreen.setGameStarted(true);
                return true;
            }
        }
        if(keycode == Input.Keys.Z) {
            if(minigameScreen.isGameStarted()) {
                //can shoot - game is started
                minigameScreen.playerShoot();
            }
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
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
