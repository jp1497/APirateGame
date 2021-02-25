package uk.ac.york.sepr4;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import lombok.Getter;
import uk.ac.york.sepr4.screen.MenuScreen;

public class APirateGame extends Game {

	@Getter
	private MenuScreen menuScreen;

	public static final boolean DEBUG = false;

	@Override
	public void create () {
		menuScreen = new MenuScreen(this);

		if (DEBUG) {
			//if debug, enable lower logging level and launch into game
			Gdx.app.setLogLevel(Application.LOG_DEBUG);
			GameInstance gameInstance = new GameInstance(this);
			gameInstance.start(false);
		} else {
			setScreen(menuScreen);
		}
	}

}
