package uk.ac.york.sepr4.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import uk.ac.york.sepr4.GameInstance;
import uk.ac.york.sepr4.io.FileManager;
import uk.ac.york.sepr4.utils.StyleManager;

public class EndScreen extends PirateScreen {

        private GameInstance gameInstance;
        private boolean win;

        public EndScreen(GameInstance gameInstance, boolean win) {
            super(gameInstance, new Stage(new ScreenViewport()), FileManager.mainMenuScreenBG);

            this.gameInstance = gameInstance;
            this.win = win;

            setupScreen();
        }

        /***
         * Create table for screen elements.
         * Label for win/lose.
         * Exit button to menu.
         */
        private void setupScreen() {
            Table table = new Table();
            table.setFillParent(true);

            //set win/lose label
            Label label = new Label("", StyleManager.generateLabelStyle(40, Color.BLACK));
            if (win) {
                label.setText("You Won!");
                label.setColor(Color.GOLD);
            } else {
                label.setText("You Lost!");
                label.setColor(Color.RED);
            }
            label.setAlignment(Align.center);

            table.add(label)
                    .padTop(Value.percentHeight(0.03f, table))
                    .fillX().uniformX();
            table.row();

            //play again button
            TextButton playAgain = new TextButton("Play Again!", StyleManager.generateTBStyle(25, Color.GREEN, Color.GRAY));
            playAgain.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent ev, float x, float y) {
                    //start a new game
                    GameInstance newGI = new GameInstance(gameInstance.getGame());
                    newGI.start(false);
                }
            });

            //exit to menu button
            TextButton exitButton = new TextButton("Exit to Menu!", StyleManager.generateTBStyle(25, Color.RED, Color.GRAY));
            exitButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent ev, float x, float y) {
                    gameInstance.switchScreen(gameInstance.getGame().getMenuScreen());
                }
            });
            table.add(exitButton).padTop(Value.percentHeight(0.02f, table)).fillX().uniformX();
            table.row();
            table.add(playAgain).fillX().uniformX();

            getStage().addActor(table);
        }

        @Override
        public void renderInner(float delta) {
            //no moving parts - not required
        }

}
