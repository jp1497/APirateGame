package uk.ac.york.sepr4.object.building;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import lombok.Data;
import uk.ac.york.sepr4.GameInstance;
import uk.ac.york.sepr4.object.crew.CrewMember;
import uk.ac.york.sepr4.object.entity.Player;
import uk.ac.york.sepr4.object.entity.npc.NPCBoat;
import uk.ac.york.sepr4.object.entity.npc.NPCBuilder;
import java.util.Optional;
import java.util.Random;

@Data
public class BuildingManager {

    private Array<College> colleges = new Array<>();
    private Array<Department> departments = new Array<>();
    private Array<MinigameBuilding> taverns = new Array<>();

    private GameInstance gameInstance;

    /***
     * This class handles instances of buildings (Colleges and Departments)
     *
     * It is responsible for loading from file and making sure the map object relating to this building is present.
     * There is a method which arranges spawning of college enemies.
     * @param gameInstance
     */
    public BuildingManager(GameInstance gameInstance) {
        this.gameInstance = gameInstance;

        if(gameInstance.getPirateMap().isObjectsEnabled()) {
            Json json = new Json();
            loadBuildings(json.fromJson(Array.class, College.class, Gdx.files.internal("data/colleges.json")));
            loadBuildings(json.fromJson(Array.class, Department.class, Gdx.files.internal("data/departments.json")));
            loadBuildings(json.fromJson(Array.class, MinigameBuilding.class, Gdx.files.internal("data/minigame.json")));
            Gdx.app.log("BuildingManager",
                    "Loaded "+colleges.size+" colleges and "+departments.size+" departments!");

        } else {
            Gdx.app.error("Building Manager", "Objects not enabled, not loading buildings!");
        }
    }

    /***
     * Check whether the college boss should spawn.
     */
    public void checkBossSpawn() {
        for(College college : colleges) {
            //check if boss already spawned or college npc kill threshold reached
            if(!college.isBossSpawned() && college.getBossSpawnThreshold() == 0) {
                //TODO: Add collision check for boss spawn
                Player player = gameInstance.getEntityManager().getOrCreatePlayer();
                if (college.getBuildingZone().contains(player.getRectBounds())) {
                    Gdx.app.debug("BuildingManager", "Player entered college zone: " + college.getName());
                    Optional<NPCBoat> npcBoss = generateCollegeNPC(college, true);
                    if(npcBoss.isPresent()) {
                        college.setBossSpawned(true);
                        gameInstance.getEntityManager().addNPC(npcBoss.get());
                    }

                }
            }
        }
    }

    /***
     * Generate random spawn point for College NPCs.
     * @param college College to get spawn point in
     * @param size range to check in
     * @return Optional spawn point if valid point found in certain no. of attempts.
     */
    private Optional<Vector2> getValidRandomSpawn(College college, float size) {
        int attempts = 0;
        while (attempts<10) {
            Vector2 test = college.getRandomSpawnVector();
            Rectangle rectangle = new Rectangle(test.x-(size/2), test.y-(size/2), size, size);
            if(!gameInstance.getPirateMap().isColliding(rectangle)
                    && !gameInstance.getEntityManager().isOccupied(rectangle)) {
                return Optional.of(test);
            }
            attempts++;
        }
        return Optional.empty();
    }

    /**
     * Generate an NPCBoat which has the appropriate position and difficulty for a college
     * @param college College for which the NPCBoat is being generated
     * @param boss Whether the generated npc is a boss
     * @return       An NPCBoat with correct parameters
     */
    public Optional<NPCBoat> generateCollegeNPC(College college, boolean boss) {
        Random random = new Random();
        if(random.nextDouble() <= college.getSpawnChance()){
            Optional<Vector2> pos = getValidRandomSpawn(college, 250f);
            if(pos.isPresent()) {
                NPCBoat boat = new NPCBuilder().generateRandomEnemyBoat( pos.get(), Optional.of(college),
                         boss ? college.getBossDifficulty() : college.getEnemyDifficulty(), boss);
                return Optional.of(boat);
            }
        }
        return Optional.empty();
    }



    //TODO: Make generic method and remove duplicate code

    /***
     * Load buildings (College, Departments, MinigameBuildings) into stores.
     * Check if they are initialized correctly (exist on the map too).
     * @param loading list of buildings to load
     */
    private void loadBuildings(Array<Building> loading) {
        for(Building building : loading) {
            if (building.load(gameInstance.getPirateMap())) {
                if(building instanceof College) {
                    //check if crew member id is defined
                    College college = (College) building;
                    Optional<CrewMember> optionalCrewMember =
                            gameInstance.getCrewBank().getCrewFromID(college.getCrewMemberId());
                    if(optionalCrewMember.isPresent()) {
                        college.setCrewMember(optionalCrewMember.get());
                        colleges.add(college);
                    } else {
                        Gdx.app.error("BuildingManager", "Failed to load " + building.getName() + ": Crew Member ID not valid!");
                    }                } else if (building instanceof Department) {
                    //check if crew member id is defined
                    Department department = (Department) building;
                    Optional<CrewMember> optionalCrewMember =
                            gameInstance.getCrewBank().getCrewFromID(department.getCrewMemberId());
                    if(optionalCrewMember.isPresent()) {
                        department.setCrewMember(optionalCrewMember.get());
                        departments.add(department);
                    } else {
                        Gdx.app.error("BuildingManager", "Failed to load " + building.getName() + ": Crew Member ID not valid!");
                    }
                } else if (building instanceof MinigameBuilding) {
                    taverns.add((MinigameBuilding) building);
                }
                Gdx.app.debug("BuildingManager", "Loaded " + building.getName());
            } else {
                Gdx.app.error("BuildingManager", "Failed to load " + building.getName());
            }

        }
    }
}
