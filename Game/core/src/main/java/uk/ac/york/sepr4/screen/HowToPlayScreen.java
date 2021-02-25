package uk.ac.york.sepr4.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import uk.ac.york.sepr4.APirateGame;
import uk.ac.york.sepr4.GameInstance;
import uk.ac.york.sepr4.io.FileManager;
import uk.ac.york.sepr4.utils.StyleManager;

public class HowToPlayScreen implements Screen {

    private APirateGame game;
    private Stage stage;

    private Screen returnScreen;

    public HowToPlayScreen(APirateGame game, Screen returnScreen) {
        this.stage = new Stage(new ScreenViewport());
        this.game = game;
        this.returnScreen = returnScreen;

        Gdx.input.setInputProcessor(stage);

        createTable();
    }

    private void createTable() {
        //create table
        Table table = new Table();
        table.top();
        table.setFillParent(true);
        //table.debug();

        //story
        Label storyHeader = new Label("Your Story", StyleManager.generateLabelStyle(40,Color.BLACK));
        Label story = new Label("In a not too distant dystopian future, Earth has been submerged in water. The battle for dry land has plunged the world into war, famine and financial chaos. The last vestiges of law in York attempt to restrain the pirate rebels from disrupting order.\n" +
                "\n" +
                "You are a lowly pirate, cast into the seas as a child to fend for yourself after being separated from your family. You are equipped  with just a floating barrel and a musket short of ammo. Whilst traveling ship to ship you begin forgoing morales for meals. You have seen your share of woe - families butchered, elderly thrown into the waves, witnessed scurvy turn the strongest of men into food for the sharks. Meanwhile, the rich indulged a lavish life on land. Their navy spend their days keeping the outsiders out and their chosen few safe.\n" +
                "\n" +
                "But you survived... and now you want justice. You aim to unite the pirates, to join the tribes with the common motive of revenge. To provide a place for your children and family to live a life without pain, a life without worry - a life of peace.\n", StyleManager.generateLabelStyle(25, Color.GRAY));
        story.setWrap(true);
        story.setAlignment(Align.center);

        //goals
        Label goalHeader = new Label("Your Mission", StyleManager.generateLabelStyle(40,Color.BLACK));
        Label goal = new Label("Muster your crew and venture into the open waters in order to defeat the scourge of the seas." +
                "\n" +
                "You must train and improve your crew in order to defeat and gain alliance with the five colleges around the map.", StyleManager.generateLabelStyle(30, Color.GRAY));
        goal.setWrap(true);
        goal.setAlignment(Align.center);

        //controls
        Label controlsHeader = new Label("Controls", StyleManager.generateLabelStyle(35,Color.BLACK));
        Label controls = new Label("WASD - Movement" +
                "\n" +
                "Left Click - Fire Cannons" +
                "\n" +
                "1-6 - Weapon Selection" +
                "\n" +
                "M - Zoom Out", StyleManager.generateLabelStyle(25,Color.BLACK));
        controls.setAlignment(Align.center);

        //start game/continue button
        TextButton toMenu = new TextButton("Continue..",
                StyleManager.generateTBStyle(25, Color.GOLD, Color.GRAY));
        toMenu.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(returnScreen);
            }
        });

        //populate table
        table.add(storyHeader).padTop(Value.percentHeight(0.05f, table)).expandX();
        table.row();
        table.add(story)
                .padTop(Value.percentHeight(0.03f, table))
                .width(Value.percentWidth(0.8f, table))
                .expandX();
        table.row();

        table.add(goalHeader).padTop(Value.percentHeight(0.05f, table)).expandX();
        table.row();
        table.add(goal)
                .padTop(Value.percentHeight(0.03f, table))
                .width(Value.percentWidth(0.8f, table))
                .expandX();
        table.row();

        table.add(controlsHeader).padTop(Value.percentHeight(0.05f, table)).expandX();
        table.row();
        table.add(controls)
                .padTop(Value.percentHeight(0.03f, table))
                .width(Value.percentWidth(0.8f, table))
                .expandX();
        table.row();

        table.add(toMenu)
                .padTop(Value.percentHeight(0.05f, table))
                .expandX();
        table.row();

        //add table to a scroll pane.
        //On smaller screens this will allow text to be scrolled if it flows over page
        ScrollPane scrollPane = new ScrollPane(table);
        scrollPane.setFillParent(true);
        stage.addActor(scrollPane);
        stage.setScrollFocus(scrollPane);
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

    /***
     * Draw screen's background.
     */
    private void drawMenuBackground() {
        //sets background texture
        stage.getBatch().begin();
        Texture texture = FileManager.departmentScreenBG;
        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        stage.getBatch().draw(texture, 0, 0, stage.getWidth(), stage.getHeight());
        stage.getBatch().end();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
