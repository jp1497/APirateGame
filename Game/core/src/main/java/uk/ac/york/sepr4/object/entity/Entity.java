package uk.ac.york.sepr4.object.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import lombok.Data;

@Data
public abstract class Entity extends Actor {

    private float angle, speed, alpha = 1;
    private Texture texture;

    public Entity(Texture texture, Vector2 pos) {
        this.texture = texture;

        // Stops texture glitches when moving
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        // Required to display sprite
        setSize(getTexture().getWidth(), getTexture().getHeight());
        // Set position
        setX(pos.x);
        setY(pos.y);
    }

    /***
     * Draw entity to the stage.
     */
    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        float alpha = this.alpha;

        batch.setColor(getColor().r, getColor().g, getColor().b,
                alpha * getColor().a * parentAlpha);

        float angleDegrees = getAngle() * 360 / 2 / 3.14f;
        batch.draw(getTexture(), getX(), getY(), getWidth() / 2, getHeight() / 2,
                getWidth(), getHeight(), 1, 1, angleDegrees, 0, 0,
                getTexture().getWidth(), getTexture().getHeight(), false, false);
    }

    /***
     * Run actor's moving methods.
     */
    @Override
    public void act(float deltaTime) {
        //Assessment 3 do nothing if paused

        super.act(deltaTime);
        setY((float) (getY()-(getSpeed()*deltaTime*Math.cos(getAngle()))));
        setX((float) (getX()+(getSpeed()*deltaTime*Math.sin(getAngle()))));
    }

    //UTILITY METHODS
    public float getAngleTowardsEntity(Entity entity) {
        double d_angle = Math.atan(((entity.getCentre().y - getCentre().y) / (entity.getCentre().x - getCentre().x)));
        if (entity.getCentre().x < getCentre().x) {
            d_angle += Math.PI;
        }
        return (float) (d_angle + Math.PI / 2);
    }

    public Vector2 getCentre() {
        return new Vector2(getX() + (getTexture().getWidth() / 2f), getY() + (getTexture().getHeight() / 2f));
    }

    public double distanceFrom(Entity entity) {
        return (float) Math.sqrt(Math.pow((entity.getCentre().x - getCentre().x), 2) + Math.pow((entity.getCentre().y - getCentre().y), 2));
    }
    public double distanceFrom(Vector2 pos) {
        return (float) Math.sqrt(Math.pow((pos.x - getCentre().x), 2) + Math.pow((pos.y - getCentre().y), 2));
    }

    public Rectangle getRectBounds() {
        return new Rectangle(getX(), getY(), getWidth(), getHeight());
    }

}
