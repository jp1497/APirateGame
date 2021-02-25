package uk.ac.york.sepr4.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.utils.viewport.FitViewport;
import lombok.Getter;
import uk.ac.york.sepr4.GameInstance;
import uk.ac.york.sepr4.object.building.Building;
import uk.ac.york.sepr4.object.building.Department;
import uk.ac.york.sepr4.object.building.MinigameBuilding;
import uk.ac.york.sepr4.utils.StyleManager;

import java.util.Optional;

public class MessageHUD {

    private GameInstance gameInstance;

    @Getter
    private Stage stage;

    //Added for Assessment 3: Many labels and tables for the different features added in SailHUD
    private Label locationPromptLabel;

    @Getter
    private Table table;

    /***
     * Class responsible for storing and updating MessageHUD variables.
     * Creates table which is drawn to the stage!
     * @param gameInstance from which to get MessageHUD variables.
     */
    public MessageHUD(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
        // Local widths and heights.

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        stage = new Stage(new FitViewport(w, h, new OrthographicCamera()));

        createTable();
    }

    /***
     * Create display for message prompt
     */
    private void createTable() {
        //define a table used to organize our sailHud's labels
        table = new Table();
        //Top-Align table
        table.bottom();
        //make the table fill the entire stage
        table.setFillParent(true);

        // Assessment 3: Add the department prompt
        locationPromptLabel = new Label("", StyleManager.generateLabelStyle(20, Color.BLACK));
        table.add(locationPromptLabel).padBottom(Value.percentHeight(0.05f, table)).expandX();


        stage.addActor(table);
    }

    /***
     * Update label values - called during stage render
     */
    public void update() {
        //location overhead
        Optional<Building> loc = gameInstance.getEntityManager().getPlayerLocation();
        locationPromptLabel.setText("");
        if (loc.isPresent()) {
            Building building = loc.get();
            if(building instanceof MinigameBuilding || building instanceof Department) {
                locationPromptLabel.setText("Press E to enter " + building.getName());
            }
        }

        stage.act();
        stage.draw();
    }

}
