package uk.ac.york.sepr4.object.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import lombok.Data;
import uk.ac.york.sepr4.hud.HealthBar;
import uk.ac.york.sepr4.io.FileManager;
import uk.ac.york.sepr4.object.building.College;
import uk.ac.york.sepr4.object.crew.CrewMember;
import uk.ac.york.sepr4.object.item.Reward;
import uk.ac.york.sepr4.screen.SailScreen;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class Player extends LivingEntity implements InputProcessor {

    private Integer balance = 0, xp = 0, level = 1;
    private List<College> captured = new ArrayList<>();
    private boolean turningLeft, turningRight;

    private List<CrewMember> crewMembers = new ArrayList<>();
    private Optional<CrewMember> selectedCrewMember = Optional.empty();

    public final float DEFAULT_ZOOM = 2.0f, MAP_ZOOM = 5f;

    public Player(Vector2 pos) {
        super(FileManager.PLAYER, pos);
        //face up
        setAngle((float)Math.PI);
    }

    /***
     * Player update/act method
     * @param deltaTime time since last render
     */
    @Override
    public void act(float deltaTime) {
        if(!isDying() && !isDead()) {
            float angle = getAngle();
            float angularSpeed = 0;
            //Changed for Assessment 3: improved responsiveness on turning functions
            if (turningLeft) {
                angularSpeed += getTurningSpeed();
            }
            if (turningRight) {
                angularSpeed -= getTurningSpeed();
            }
            //change angle based on turning/angular speed
            angle += ((angularSpeed * deltaTime) * (getSpeed() / getMaxSpeed())) % (float) (2 * Math.PI);
            setAngle(angle);
            decrementCrewCooldown(deltaTime);
            super.act(deltaTime);
        }
    }

    @Override
    public boolean fire(float angle) {
        if(!selectedCrewMember.isPresent()) {
            //normal fire
            super.fire(angle);
        } else {
            CrewMember crewMember = selectedCrewMember.get();
            crewMember.fire(angle);
        }

        return false;
    }

    public void addCrewMember(CrewMember crewMember) {
        crewMembers.add(crewMember);
    }

    //decrement cooldown for crewmembers (abilities)
    public void decrementCrewCooldown(float delta) {
        crewMembers.forEach(crewMember -> crewMember.decrementCooldown(delta));
    }

    public void capture(College college) {
        captured.add(college);
        Gdx.app.debug("Player", "Captured "+college.getName());
    }

    //Changed for Assessment 3: removed unused functions returning level progress
    /**
     * Compute whether the player will level up and give rewards if true
     * @return The level of the player
     */
    public Integer getLevel() {
        if (xp >= (level+1)*10) {
            level += 1;
            xp = 0;
            setMaxHealth(getMaxHealth() + 5);
            setHealth(getMaxHealth());
            updateHealthBar();
            setMaxSpeed(getMaxSpeed() + 20);
            setDamage(getDamage() + 0.1);
        }
        return level;
    }

    public void issueReward(Reward reward) {
        addBalance(reward.getGold());
        addXP(reward.getXp());
    }

    public void addBalance(Integer val) {
        balance+=val;
    }
    public void addXP(Integer val) {
        xp+=val;
    }


    //Added for Assessment 3: Allow interaction with shops
    public boolean deductBalance(int deduction) {
        if(deduction <= balance) {
            balance -= deduction;
            return true;
        }
        return false;
    }

    public void updateHealthBar(){
        setHealthBar(new HealthBar(this));
    }
    //Methods below for taking keyboard input from player.
    @Override
    public boolean keyDown(int keycode) {
        // do nothing if paused

        if(keycode == Input.Keys.W) {
            setAccelerating(true);
            return true;
        }

        if(keycode == Input.Keys.S) {
            setBraking(true);
            return true;
        }

        if(keycode == Input.Keys.A) {
            // Assessment 3 - changed to make turning more responsive
            turningLeft = true;
            return true;
        }

        if(keycode == Input.Keys.D) {
            // Assessment 3 - changed to make turning more responsive
            turningRight = true;
            return true;
        }
        if(keycode == Input.Keys.M) {
            //minimap
            SailScreen.getInstance().getOrthographicCamera().zoom = MAP_ZOOM;
            return true;
        }

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if(keycode == Input.Keys.W) {
            setAccelerating(false);
            return true;
        }

        if(keycode == Input.Keys.S) {
            setBraking(false);
            return true;
        }

        if(keycode == Input.Keys.A) {
            // Assessment 3 - changed to make turning more responsive
            turningLeft = false;
            return true;
        }

        if(keycode == Input.Keys.D) {
            // Assessment 3 - changed to make turning more responsive
            turningRight = false;
            return true;
        }
        if(keycode == Input.Keys.M) {
            //minimap
            SailScreen.getInstance().getOrthographicCamera().zoom = DEFAULT_ZOOM;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
