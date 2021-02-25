package uk.ac.york.sepr4.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import uk.ac.york.sepr4.GameInstance;
import uk.ac.york.sepr4.io.FileManager;
import uk.ac.york.sepr4.object.building.Department;
import uk.ac.york.sepr4.object.crew.CrewMember;
import uk.ac.york.sepr4.object.entity.Player;
import uk.ac.york.sepr4.utils.StyleManager;

public class DepartmentScreen extends PirateScreen {

    private Department department;
    private GameInstance gameInstance;

    private TextButton upgrade, repair;

    public DepartmentScreen(GameInstance gameInstance, Department department) {
        super(gameInstance, new Stage(new ScreenViewport()), FileManager.departmentScreenBG);
        this.gameInstance = gameInstance;
        this.department = department;

        //make stats (gold) visible on this screen
        setEnableStatsHUD(true);

        createShopMenu();
    }

    @Override
    public void renderInner(float delta) {
        updateTextButtons();
    }

    /***
     * Create Shop Menu.
     * Player can repair ship if damaged or upgrade crew member.
     */
    private void createShopMenu() {
        Player player = gameInstance.getEntityManager().getOrCreatePlayer();

        Table table = new Table();
        table.top();
        table.setFillParent(true);

        //WELCOME MESSAGE
        Label welcome = new Label("Welcome to the "+department.getName()+" Department!",
                StyleManager.generateLabelStyle(50, Color.GOLD));

        //REPAIR BUTTON
        //label to display if player's boat has full health
        Label noRepair = new Label("Your ship does not need repair!", StyleManager.generateLabelStyle(30, Color.BLACK));

        //button updated regularly in render
        repair = new TextButton("", StyleManager.generateTBStyle(40, Color.BLACK, Color.GRAY));
        repair.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent ev, float x, float y) {
                if(player.getBalance()>=getHealCost()) {
                    //has enough gold
                    player.deductBalance(getHealCost());
                    player.setHealth(player.getMaxHealth());
                }
            }
        });

        //UPDATE BUTTON
        //label to display if allied college not defeat (crew member not unlocked)
        Label noUpgrade = new Label("You have not unlocked this crew member!", StyleManager.generateLabelStyle(40, Color.BLACK));

        //Maximum level label
        Label maxUpgrade = new Label("This crew member is at it's maximum level!", StyleManager.generateLabelStyle(40, Color.BLACK));


        //button updated regularly in render
        CrewMember crewMember = department.getCrewMember();
        upgrade = new TextButton("",
                StyleManager.generateTBStyle(30, Color.BLACK, Color.GRAY));
        upgrade.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent ev, float x, float y) {
                if((player.getBalance()>=crewMember.getUpgradeCost())
                        && crewMember.canUpgrade()) {
                    //has enough gold and not maximum level
                    player.deductBalance(crewMember.getUpgradeCost());
                    crewMember.upgrade();
                }
            }
        });

        //EXIT BUTTON
        TextButton exit = new TextButton("Exit!", StyleManager.generateTBStyle(25, Color.RED, Color.GRAY));
        exit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent ev, float x, float y) {
                gameInstance.fadeSwitchScreen(gameInstance.getSailScreen());
            }
        });

        //POPULATE TABLE
        table.add(welcome).padTop(Value.percentHeight(0.35f, table)).expandX();
        table.row();
        if(player.getHealth().equals(player.getMaxHealth())) {
            table.add(noRepair).padTop(Value.percentHeight(0.05f, table)).expandX();
        } else {
            table.add(repair).padTop(Value.percentHeight(0.05f, table)).expandX();
        }
        table.row();
        if(player.getCrewMembers().contains(crewMember)) {
            //has unlocked crew member
            if(crewMember.canUpgrade()) {
                table.add(upgrade).padTop(Value.percentHeight(0.02f, table)).expandX();
            } else {
                table.add(maxUpgrade).padTop(Value.percentHeight(0.02f, table)).expandX();
            }
        } else {
            table.add(noUpgrade).padTop(Value.percentHeight(0.02f, table)).expandX();
        }
        table.row();
        table.add(exit).padTop(Value.percentHeight(0.05f, table)).expandX();

        getStage().addActor(table);
    }

    /***
     * Update repair and upgrade cost.
     */
    private void updateTextButtons() {
        CrewMember crewMember = department.getCrewMember();
        repair.setText("Click to repair your ship for "+getHealCost()+ " gold!");
        upgrade.setText("Click to upgrade " + crewMember.getName() + " for "+crewMember.getUpgradeCost()+" gold!");
    }

    private Integer getHealCost() {
        Player player = gameInstance.getEntityManager().getOrCreatePlayer();
        return (int)Math.round(department.getHealCost()*(player.getMaxHealth()-player.getHealth()));
    }
}
