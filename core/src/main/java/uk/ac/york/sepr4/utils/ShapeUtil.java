package uk.ac.york.sepr4.utils;


import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import uk.ac.york.sepr4.object.entity.LivingEntity;

import java.util.Optional;
import java.util.Random;

public class ShapeUtil {

    /***
     * Checks whether a polygon and rectangle overlap.
     * Used for checking collisions.
     * @param polygon
     * @param rectangle
     * @return true if polygon and rectangle overlap
     */
    public static boolean overlap(Polygon polygon, Rectangle rectangle) {
        float[] points = polygon.getTransformedVertices();
        for(int i=0;i<points.length;i+=2) {
            float x = points[i], y = points[i+1];
            if(rectangle.contains(x, y)){
                return true;
            }
        }
        return false;
    }

    //get random coordinate within polygon
    public static Optional<Vector2> getRandomPosition(Polygon polygon) {
        Integer attempts = 25;
        Rectangle rectangle = polygon.getBoundingRectangle();
        Random random = new Random();

        while(attempts > 0) {
            Vector2 pos = new Vector2((int)(rectangle.x + (rectangle.width*random.nextDouble())),
                    (int)(rectangle.y + (rectangle.height*random.nextDouble())));
            if(polygon.contains(pos)) {
                return Optional.of(pos);
            }

            attempts--;
        }
        return Optional.empty();
    }


}
