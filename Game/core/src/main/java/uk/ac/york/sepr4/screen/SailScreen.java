package uk.ac.york.sepr4.screen;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import uk.ac.york.sepr4.GameInstance;
import uk.ac.york.sepr4.hud.HealthBar;
import uk.ac.york.sepr4.io.SailInputProcessor;
import uk.ac.york.sepr4.object.building.BuildingManager;
import uk.ac.york.sepr4.object.entity.EntityManager;
import uk.ac.york.sepr4.object.entity.LivingEntity;
import uk.ac.york.sepr4.object.entity.Player;
import uk.ac.york.sepr4.object.entity.npc.NPCEntity;
import uk.ac.york.sepr4.object.item.RewardManager;
import uk.ac.york.sepr4.object.projectile.Projectile;
import uk.ac.york.sepr4.utils.AIUtil;

/**
 * SailScreen is main game class. Holds data related to current player including the
 * {@link BuildingManager}, {@link RewardManager} and {@link EntityManager}
 * <p>
 * Responds to keyboard and mouse input by the player. InputMultiplexer used to combine input processing in both
 * this class (mouse clicks) and {@link Player} class (key press).
 */
public class SailScreen extends PirateScreen {

    private GameInstance gameInstance;

    private static SailScreen sailScreen;

    private ShapeRenderer shapeRenderer;

    private SailInputProcessor sailInputProcessor;

    public static SailScreen getInstance() {
        return sailScreen;
    }

    /**
     * SailScreen Constructor
     * Adds the player as an actor to the stage.
     *
     * @param gameInstance
     */
    public SailScreen(GameInstance gameInstance) {
        super(gameInstance, new Stage(new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight())));
        this.gameInstance = gameInstance;
        sailScreen = this;

        // Debug options (extra logging, collision shape renderer (viewing tile object map))
        if (gameInstance.getGame().DEBUG) {
            Gdx.app.setLogLevel(Application.LOG_DEBUG);
            shapeRenderer = new ShapeRenderer();
        }

        Player player = gameInstance.getEntityManager().getOrCreatePlayer();

        getOrthographicCamera().zoom = player.DEFAULT_ZOOM;

        //Set input processor and focus
        getInputMultiplexer().addProcessor(player);
        sailInputProcessor = new SailInputProcessor(gameInstance);
        getInputMultiplexer().addProcessor(sailInputProcessor);

        setEnableStatsHUD(true);
        setEnableMessageHUD(true);

        //Create and spawn player
        startGame();
    }

    private void startGame() {
        getStage().addActor(gameInstance.getEntityManager().getOrCreatePlayer());
    }

    /**
     * Method responsible for rendering the SailScreen on each frame. This clears the screen, updates the map and
     * visible entities, then calls the stage act. This causes actors (entities) on the stage to move (act).
     *
     * @param delta Time between last and current frame.
     */
    @Override
    public void renderInner(float delta) {
        //if player dead, go to main menu
        Player player = gameInstance.getEntityManager().getOrCreatePlayer();
        BuildingManager buildingManager = gameInstance.getBuildingManager();
        EntityManager entityManager = gameInstance.getEntityManager();

        //check if game over
        if (player.isDead()) {
            Gdx.app.debug("SailScreen", "Player Died!");
            gameInstance.fadeSwitchScreen(new EndScreen(gameInstance, false));
            return;
        }

        if (!player.isDying()) {
            //spawns/despawns entities, handles animations and projectiles
            entityManager.handleStageEntities(getStage(), delta);
        } else {
            //when the player is dying - only process animations
            entityManager.getAnimationManager().handleEffects(getStage(), delta);
        }
        if (gameInstance.getPirateMap().isObjectsEnabled()) {
            gameInstance.getEntityManager().spawnEnemies(delta);
            buildingManager.checkBossSpawn();
        }

        handleHealthBars();
        checkCollisions();

        // Update camera and focus on player.
        getBatch().setProjectionMatrix(getOrthographicCamera().combined);
        getOrthographicCamera().update();
        getOrthographicCamera().position.set(player.getX() + player.getWidth() / 2f, player.getY() + player.getHeight() / 2f, 0);
        gameInstance.getTiledMapRenderer().setView(getOrthographicCamera());
        gameInstance.getTiledMapRenderer().render();

        //DEBUG - Render outline around game objects/zones.
        if (gameInstance.getGame().DEBUG) {
            shapeRenderer.setProjectionMatrix(getBatch().getProjectionMatrix());
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.RED);

            for (Polygon polygonMapObject : gameInstance.getPirateMap().getCollisionObjects()) {
                shapeRenderer.polygon(polygonMapObject.getTransformedVertices());
            }
            shapeRenderer.setColor(Color.BLUE);
            for (Polygon polygonMapObject : gameInstance.getPirateMap().getSpawnZones().keySet()) {
                shapeRenderer.polygon(polygonMapObject.getTransformedVertices());
            }

            shapeRenderer.end();
        }
    }

    /**
     * Handles HealthBar elements for damaged actors.
     */
    private void handleHealthBars() {
        Player player = gameInstance.getEntityManager().getOrCreatePlayer();
        if (player.getHealth() < player.getMaxHealth()) {
            //if player has less than max health
            if (!getStage().getActors().contains(player.getHealthBar(), true)) {
                //if healthbar isnt on stage, add it
                getStage().addActor(player.getHealthBar());
            }
        }

        for (NPCEntity npcEntity : gameInstance.getEntityManager().getNpcList()) {
            if (npcEntity.getHealth() < npcEntity.getMaxHealth()) {
                //if entity has less than max health
                if (!getStage().getActors().contains(npcEntity.getHealthBar(), true)) {
                    //if healthbar isnt on stage, add it
                    getStage().addActor(npcEntity.getHealthBar());
                }
            }
        }
        Array<Actor> toRemove = new Array<>();
        for (Actor actors : getStage().getActors()) {
            if (actors instanceof HealthBar) {
                HealthBar healthBar = (HealthBar) actors;
                LivingEntity livingEntity = healthBar.getLivingEntity();
                if (livingEntity.getHealth() == livingEntity.getMaxHealth() || livingEntity.isDead() || livingEntity.isDying()) {
                    //if living entity has healed, is dead or is dying --> remove healthbar
                    toRemove.add(actors);
                }
            }
        }
        getStage().getActors().removeAll(toRemove, true);

    }

    /**
     * Checks whether actors have overlapped. In the instance where projectile and entity overlap, deal damage.
     */
    private void checkCollisions() {
        checkProjectileCollisions();
        checkLivingEntityCollisions();
    }

    /***
     * Nested loop through each entity against others and check if colliding (overlapping).
     */
    public void checkLivingEntityCollisions() {
        EntityManager entityManager = gameInstance.getEntityManager();
        //player/map collision check
        //TODO: Improve to make player a polygon - cant do without a lot of work
        for (LivingEntity lE : entityManager.getLivingEntities()) {
            //Between entity and map
            if (gameInstance.getPirateMap().isColliding(lE.getRectBounds())) {
                if (lE.getCollidedWithIsland() == 0) {
                    lE.collide(false, 0f);
                }
            }
            if (lE.getCollidedWithIsland() >= 1) {
                lE.setCollidedWithIsland(lE.getCollidedWithIsland() - 1);
            }

            //between living entities themselves
            for (LivingEntity lE2 : entityManager.getLivingEntities()) {
                if (!lE.equals(lE2)) {
                    if(!lE.isDying() && !lE2.isDying()) {
                        if (lE.getRectBounds().overlaps(lE2.getRectBounds())) {
                            if (lE.getColliedWithBoat() == 0) {
                                lE.collide(true, AIUtil.normalizeAngle((float) (lE.getAngleTowardsEntity(lE2) - Math.PI)));
                            }
                            //Gdx.app.log("gs", ""+lE.getColliedWithBoat());
                        }
                    }
                }
                if (lE.getColliedWithBoat() >= 1) {
                    lE.setColliedWithBoat(lE.getColliedWithBoat() - 1);
                }
            }
        }
    }

    /***
     * Loop through all projectiles and entities and check if colliding (overlapping).
     */
    private void checkProjectileCollisions() {
        EntityManager entityManager = gameInstance.getEntityManager();
        for (Projectile projectile : entityManager.getProjectileManager().getProjectileList()) {
            if(gameInstance.getPirateMap().isColliding(projectile.getRectBounds())) {
                //if projectile collides with map objects - remove
                projectile.setActive(false);
                return;
            }

            for (LivingEntity livingEntity : entityManager.getLivingEntities()) {
                if (projectile.getShooter() != livingEntity && projectile.getRectBounds().overlaps(livingEntity.getRectBounds())) {
                    //if bullet overlaps LE and shooter not LE
                    if (!(livingEntity.isDying() || livingEntity.isDead())) {
                        livingEntity.damage(projectile);
                        Gdx.app.debug("SailScreen", "LivingEntity damaged by projectile.");
                        //kill projectile
                        projectile.setActive(false);
                    }
                }
            }
        }
    }
}
