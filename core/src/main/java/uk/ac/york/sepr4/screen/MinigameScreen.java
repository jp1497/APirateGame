package uk.ac.york.sepr4.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import lombok.Getter;
import lombok.Setter;
import uk.ac.york.sepr4.GameInstance;
import uk.ac.york.sepr4.io.FileManager;
import uk.ac.york.sepr4.io.MinigameInputProcessor;
import uk.ac.york.sepr4.object.entity.Player;
import uk.ac.york.sepr4.utils.StyleManager;

import java.util.Random;

public class MinigameScreen extends PirateScreen {

    private Table table, gameTable;
    @Getter
    private MinigameDifficulty difficulty;
    @Getter
    @Setter
    private boolean gameStarted = false, gameOver = false;

    private float enemyShootTimer, startCountdown;

    private Label gameText;
    private Image playerImage, enemyImage;

    public MinigameScreen(GameInstance gameInstance) {
        super(gameInstance, new Stage(new ScreenViewport()), FileManager.departmentScreenBG);

        getInputMultiplexer().addProcessor(new MinigameInputProcessor(this));

        setEnableStatsHUD(true);

        createMenu();
        displayMenu();
    }

    @Override
    public void renderInner(float delta) {
        if (gameOver) {
            //delay then reset game
            setGameStarted(false);
            startCountdown += delta;
            if (startCountdown > 3f) {
                resetGame();
            }
        }
        if (difficulty != null && gameStarted) {
            //minigame being played
            startCountdown -= delta;
            enemyShootTimer -= delta;
            gameText.setText("Prepare to shoot.. (Z)");
            if (startCountdown <= 0) {
                //change text - player can now shoot
                gameText.setText("SHOOT! (Z)");
                gameText.setColor(Color.RED);
                if (enemyShootTimer <= 0) {
                    enemyShoot();
                }
            }
        }

    }

    private void enemyShoot() {
        gameText.setText("You Lost!");
        setGameOver(true);
    }

    public void playerShoot() {
        if (startCountdown > 0) {
            //shot too early - loose!
            gameText.setText("You drew too early! You lost!");
            setGameOver(true);
        } else {
            giveReward();
            gameText.setText("You Won!");
            setGameOver(true);
        }
    }

    private void resetGame() {
        difficulty = null;
        startCountdown = 0;
        enemyShootTimer = 0;
        gameTable = null;
        setGameStarted(false);
        setGameOver(false);
        displayMenu();
    }

    private void giveReward() {
        getGameInstance().getEntityManager().getOrCreatePlayer().addBalance(difficulty.getReward());
    }

    private void createMenu() {
        //table setup
        table = new Table();
        table.top();
        table.setFillParent(true);

        //intro text
        Label minigameText1 = new Label("How difficult do you want your minigame to be?", StyleManager.generateLabelStyle(45, Color.GRAY));
        Label minigameText2 = new Label("Higher difficulty means higher rewards!", StyleManager.generateLabelStyle(40, Color.GRAY));
        Label instructionText = new Label("Wait for the signal, then use the Z key to shoot before your opponent does.", StyleManager.generateLabelStyle(30, Color.GRAY));

        //minigame buttons
        TextButton quitMinigame = new TextButton("Exit Minigame", StyleManager.generateTBStyle(30, Color.RED, Color.GRAY));
        quitMinigame.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent ev, float x, float y) {
                getGameInstance().fadeSwitchScreen(getGameInstance().getSailScreen(), true);
            }
        });
        TextButton easyMinigame = new TextButton("Easy (1 gold)", StyleManager.generateTBStyle(35, Color.GREEN, Color.GRAY));
        easyMinigame.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent ev, float x, float y) {
                startGame(MinigameDifficulty.EASY);
            }
        });
        TextButton medMinigame = new TextButton("Medium (10 gold)", StyleManager.generateTBStyle(35, Color.YELLOW, Color.GRAY));
        medMinigame.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent ev, float x, float y) {
                startGame(MinigameDifficulty.MEDIUM);
            }
        });
        TextButton hardMinigame = new TextButton("Hard (20 gold)", StyleManager.generateTBStyle(35, Color.RED, Color.GRAY));
        hardMinigame.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent ev, float x, float y) {
                startGame(MinigameDifficulty.HARD);
            }
        });
        TextButton veryHardMinigame = new TextButton("Very Hard (50 gold)", StyleManager.generateTBStyle(35, Color.BLACK, Color.GRAY));
        veryHardMinigame.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent ev, float x, float y) {
                startGame(MinigameDifficulty.VERY_HARD);
            }
        });

        //populate table
        table.add(minigameText1).padTop(Value.percentHeight(0.20f, table)).expandX();
        table.row();
        table.add(minigameText2);
        table.row();
        table.add(instructionText).padTop(Value.percentHeight(0.02f, table));
        table.row();
        table.add(easyMinigame).padTop(Value.percentHeight(0.02f, table));
        table.row();
        table.add(medMinigame).padTop(Value.percentHeight(0.02f, table));
        table.row();
        table.add(hardMinigame).padTop(Value.percentHeight(0.02f, table));
        table.row();
        table.add(veryHardMinigame).padTop(Value.percentHeight(0.02f, table));
        table.row();
        table.add(quitMinigame).padTop(Value.percentHeight(0.05f, table));

    }

    private void displayMenu() {
        getStage().clear();
        getStage().addActor(table);
    }

    /***
     * Player has selected to start game on specified difficulty
     * @param difficulty selected difficulty
     */
    private void startGame(MinigameDifficulty difficulty) {
        Player player = getGameInstance().getEntityManager().getOrCreatePlayer();
        if (player.getBalance() >= difficulty.getCost()) {
            //if player has enough money
            player.deductBalance(difficulty.getCost());
            setCountdowns(difficulty);
            this.difficulty = difficulty;
            getStage().clear();
            createGameUI(difficulty);
        } else {
            //TODO: SET MESSAGE CANT AFFORD
        }
    }

    /***
     * Create UI for game itself
     * @param difficulty selected difficulty
     */
    private void createGameUI(MinigameDifficulty difficulty) {
        gameTable = new Table();
        gameTable.top();
        gameTable.setFillParent(true);
        gameText = new Label("Press SPACE when you're ready! Press Z to shoot!", StyleManager.generateLabelStyle(30, Color.GRAY));

        playerImage = new Image(FileManager.MINIGAME_PLAYER_1);
        enemyImage = new Image(difficulty.getEnemyHolstered());

        gameTable.add(playerImage).expandX().padTop(Value.percentHeight(0.20f, gameTable));
        gameTable.add();
        gameTable.add(enemyImage).expandX().padTop(Value.percentHeight(0.20f, gameTable));
        gameTable.row();
        gameTable.add();
        gameTable.add(gameText).expandX().padTop(Value.percentHeight(0.05f, gameTable));

        getStage().addActor(gameTable);
    }

    private void setCountdowns(MinigameDifficulty minigameDifficulty) {
        Random random = new Random();
        startCountdown = random.nextInt(3) + 1;
        enemyShootTimer = startCountdown + minigameDifficulty.getCountdown();
    }
}


@Getter
enum MinigameDifficulty {
    EASY(FileManager.MINIGAME_ENEMY_EASY_1, FileManager.MINIGAME_ENEMY_EASY_2, 1, 2, 0.4f),
    MEDIUM(FileManager.MINIGAME_ENEMY_MED_1, FileManager.MINIGAME_ENEMY_MED_2, 10, 20, 0.27f),
    HARD(FileManager.MINIGAME_ENEMY_HARD_1, FileManager.MINIGAME_ENEMY_HARD_2, 20, 40, 0.23f),
    VERY_HARD(FileManager.MINIGAME_ENEMY_VHARD_1, FileManager.MINIGAME_ENEMY_VHARD_2, 50, 100, 0.20f);

    private Texture enemyHolstered, enemyShooting;
    private Integer cost, reward;
    private float countdown;

    MinigameDifficulty(Texture enemyHolstered, Texture enemyShooting, Integer cost, Integer reward, float countdown) {
        this.enemyHolstered = enemyHolstered;
        this.enemyShooting = enemyShooting;
        this.cost = cost;
        this.reward = reward;
        this.countdown = countdown;
    }
}