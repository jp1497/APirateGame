package uk.ac.york.sepr4.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import uk.ac.york.sepr4.APirateGame;
import uk.ac.york.sepr4.GameInstance;
import uk.ac.york.sepr4.io.FileManager;
import uk.ac.york.sepr4.utils.StyleManager;

public class MenuScreen implements Screen {

    private APirateGame game;

    private Stage stage;

    public MenuScreen(APirateGame game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());

        Gdx.input.setInputProcessor(stage);
        createTable();
    }


    private void createTable() {
        // Create a table that fills the screen. Everything else will go inside this table.
        Table table = new Table();
        Table logoTable = new Table();
        table.setFillParent(true);
        logoTable.setFillParent(true);
        stage.addActor(table);
        stage.addActor(logoTable);

        //add team and game logos
        Image gameLogo = new Image(FileManager.gameLogo);
        gameLogo.setScaling(Scaling.fit);
        logoTable.add(gameLogo)
                .padTop(Value.percentHeight(0.07f, logoTable))
                .maxHeight(Value.percentWidth(0.17f, logoTable))
                .expandX();
        logoTable.row();
        Image teamLogo = new Image(FileManager.teamLogo);
        teamLogo.setScaling(Scaling.fit);
        logoTable.add(teamLogo)
                .padTop(Value.percentHeight(0.55f, logoTable))
                .maxHeight(Value.percentWidth(0.08f, logoTable))
                .expandX();

        //create buttons
        TextButton howToPlay = new TextButton("How to Play", StyleManager.generateTBStyle(30, Color.BLACK, Color.GRAY));
        TextButton newGame = new TextButton("New Game", StyleManager.generateTBStyle(40, Color.BLACK, Color.GRAY));
        TextButton exit = new TextButton("Exit", StyleManager.generateTBStyle(30, Color.BLACK, Color.GRAY));

        //add buttons to table
        table.add(howToPlay).padTop(Value.percentHeight(0.01f, table)).fillX().uniformX();
        table.row();
        table.add(newGame).padTop(Value.percentHeight(0.01f, table)).fillX().uniformX();
        table.row();
        table.add(exit).padTop(Value.percentHeight(0.01f, table)).fillX().uniformX();

        // create button listeners
        exit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        //Changed for Assessment 3: Added a start button to the menu
        newGame.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameInstance gameInstance = new GameInstance(game);
                //start game and display how to play screen
                gameInstance.start(true);
            }
        });

        howToPlay.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new HowToPlayScreen(game, game.getMenuScreen()));
            }
        });


    }

    /***
     * Draw screen's background.
     */
    private void drawMenuBackground() {
        //sets background texture
        stage.getBatch().begin();
        Texture texture = FileManager.mainMenuScreenBG;
        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        stage.getBatch().draw(texture, 0, 0, stage.getWidth(), stage.getHeight());
        stage.getBatch().end();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        // clear the screen ready for next set of images to be drawn
        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        drawMenuBackground();

        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
    }
}
