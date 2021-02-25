package uk.ac.york.sepr4.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import lombok.Getter;
import uk.ac.york.sepr4.GameInstance;
import uk.ac.york.sepr4.io.FileManager;
import uk.ac.york.sepr4.object.building.College;
import uk.ac.york.sepr4.object.crew.CrewMember;
import uk.ac.york.sepr4.object.entity.Player;
import uk.ac.york.sepr4.utils.StyleManager;

public class PauseHUD {
    private GameInstance gameInstance;
    @Getter
    private Stage stage;
    /***
     * Class responsible for storing and updating PauseHUD variables.
     * Creates table which is drawn to the stage!
     * @param gameInstance from which to get PauseHUD variables.
     */
    public PauseHUD(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
        // Local widths and heights.

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        stage = new Stage(new FitViewport(w, h, new OrthographicCamera()));

        createTable();
        createControlsTable();
        createCrewTable();
    }

    /***
     * Create display for controls table.
     * The controls display appears mid-left on the PauseHUD.
     */
    private void createControlsTable() {
        Table controlsTable = new Table();
        controlsTable.top();
        controlsTable.setFillParent(true);
        controlsTable.padRight(Value.percentWidth(0.5f, controlsTable))
                .padLeft(Value.percentWidth(0.16f, controlsTable));

        Label controlsHeader = new Label("Controls", StyleManager.generateLabelStyle(35, Color.NAVY));
        controlsTable.add(controlsHeader).padTop(Value.percentHeight(0.22f, controlsTable)).expandX();
        controlsTable.row();

        //create labels
        Label W = new Label("Accelerate : W", StyleManager.generateLabelStyle(25, Color.GOLD));
        Label A = new Label("Turn Left : A", StyleManager.generateLabelStyle(25, Color.GOLD));
        Label S = new Label("Slow down : S", StyleManager.generateLabelStyle(25, Color.GOLD));
        Label D = new Label("Turn Right : D", StyleManager.generateLabelStyle(25, Color.GOLD));
        Label M = new Label("Open Minimap : M", StyleManager.generateLabelStyle(25, Color.GOLD));

        //add labels to table
        controlsTable.add(W).padTop(Value.percentHeight(0.02f, controlsTable)).expandX();
        controlsTable.row();
        controlsTable.add(A).padTop(Value.percentHeight(0.02f, controlsTable)).expandX();
        controlsTable.row();
        controlsTable.add(S).padTop(Value.percentHeight(0.02f, controlsTable)).expandX();
        controlsTable.row();
        controlsTable.add(D).padTop(Value.percentHeight(0.02f, controlsTable)).expandX();
        controlsTable.row();
        controlsTable.add(M).padTop(Value.percentHeight(0.02f, controlsTable)).expandX();

        stage.addActor(controlsTable);
    }

    /***
     * Create display for crew table.
     * The crew table appears mid-right and shows the player which upgrades they have and what level they are.
     */
    private void createCrewTable() {
        Player player = gameInstance.getEntityManager().getOrCreatePlayer();
        Table crewTable = new Table();
        crewTable.top();
        crewTable.setFillParent(true);

        //setup table
        crewTable.padLeft(Value.percentWidth(0.5f, crewTable))
                        .padRight(Value.percentWidth(0.16f, crewTable));

        Label crewHeader = new Label("Crew Members", StyleManager.generateLabelStyle(35, Color.NAVY));
        crewTable.add(crewHeader).padTop(Value.percentHeight(0.22f, crewTable)).expandX();

        //programmatically add crew member label
        for(CrewMember crew : player.getCrewMembers()) {
            Label crewLabel = new Label(crew.getName()+" : "+crew.getLevel()+"/"+crew.getMaxLevel(),
                    StyleManager.generateLabelStyle(25, Color.GOLD));
            crewTable.row();
            crewTable.add(crewLabel).padTop(Value.percentHeight(0.02f, crewTable)).expandX();
        }

        stage.addActor(crewTable);
    }

    /***
     * Create main PauseHUD table.
     * Has quit button, pause label and objective table (colleges captured)
     */
    private void createTable() {
        Player player = gameInstance.getEntityManager().getOrCreatePlayer();
        //define a table used to organize our sailHud's labels
        Table table = new Table();
        //Top-Align table
        table.top();
        //make the table fill the entire stage
        table.setFillParent(true);

        //Added for Assessment 3: Menu button
        TextButton btnMenu = new TextButton("Quit", StyleManager.generateTBStyle(30, Color.RED, Color.GRAY));
        btnMenu.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameInstance.switchScreen(gameInstance.getGame().getMenuScreen());
            }
        });

        //Assessment 3: print pause during paused state
        Label pausedLabel = new Label("PAUSED", StyleManager.generateLabelStyle(50, Color.BLACK));

        table.add(pausedLabel)
                .expandX()
                .padTop(Value.percentHeight(0.23f, table));
        table.row();
        table.add(btnMenu).expandX();
        table.row();

        //College/Goal Tracker
        Label collegesHeader = new Label("Colleges", StyleManager.generateLabelStyle(35, Color.BLACK));
        table.add(collegesHeader).padTop(Value.percentHeight(0.11f, table)).expandX();

        for(College college : gameInstance.getBuildingManager().getColleges()) {
            boolean isCaptured = player.getCaptured().contains(college);
            Label collegeLabel = new Label(college.getName(), StyleManager.generateLabelStyle(25, (isCaptured ? Color.GREEN : Color.RED)));
            table.row();
            table.add(collegeLabel).padTop(Value.percentHeight(0.02f, table)).expandX();
        }

        stage.addActor(table);

    }

    /***
     * Draw pause HUD's background overlay.
     */
    private void drawPauseOverlay() {
        //sets background texture
        stage.getBatch().begin();
        Texture texture = FileManager.pauseScreenBG;
        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        stage.getBatch().draw(texture, 0, 0, stage.getWidth(), stage.getHeight());
        stage.getBatch().end();
    }

    /***
     * Update label values - called during stage render
     */
    public void update() {
        stage.clear();

        createTable();
        createControlsTable();
        createCrewTable();
        drawPauseOverlay();

        stage.act();
        stage.draw();
    }

}
