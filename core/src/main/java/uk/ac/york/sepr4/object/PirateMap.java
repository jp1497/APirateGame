package uk.ac.york.sepr4.object;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import lombok.Getter;
import uk.ac.york.sepr4.utils.ShapeUtil;

import java.util.*;

public class PirateMap {

    @Getter
    private TiledMap tiledMap;

    private final String objectLayerName = "objects";
    private final String spawnPointObject = "spawn";

    private MapLayer objectLayer;

    @Getter
    private Vector2 spawnPoint;

    @Getter
    private boolean objectsEnabled;

    @Getter
    private List<Polygon> collisionObjects = new ArrayList<>();
    @Getter
    private HashMap<Polygon, Integer> spawnZones = new HashMap<>();



    public PirateMap(TiledMap tiledMap) {
        this.tiledMap = tiledMap;

        if (checkObjectLayer()) {
            setCollisionObjects();
            setSpawnZones();
            this.objectsEnabled = true;
        } else {
            Gdx.app.error("Pirate Map", "Map does NOT contain object layer!");
            this.objectsEnabled = false;
        }

    }

    public Vector2 getSpawnPoint() {
        if (isObjectsEnabled()) {
            return spawnPoint;
        } else {
            return new Vector2(50, 50);
        }
    }

    public boolean isColliding(Rectangle rectangle) {
        for(Polygon polygon : collisionObjects) {
            if(ShapeUtil.overlap(polygon, rectangle)){
                return true;
            }
        }
        return false;
    }

    /***
     * Generate spawn zones from map objects. Parse difficulty from object name.
     * These will areas will be used to spawn NPCs (krakens, boats).
     */
    private void setSpawnZones() {
        for(MapObject objects : objectLayer.getObjects()){
            if(objects.getName() != null) {
                if (objects.getName().contains("npc_spawn")) {
                    //is an npc_spawn object
                    if (objects instanceof PolygonMapObject) {
                        String name = objects.getName();
                        name = name.replace("npc_spawn", "");
                        Integer difficulty = Integer.valueOf(name);
                        if (difficulty != null) {
                            //parse difficulty from name
                            PolygonMapObject polygonMapObject = (PolygonMapObject) objects;
                            Polygon polygon = polygonMapObject.getPolygon();
                            spawnZones.put(convertTiledPolygonToMap(polygon, 0, 0), difficulty);
                        }
                    }
                }
            }
        }
        Gdx.app.log("PirateMap", "Loaded "+spawnZones.size()+" spawn zones!");
    }

    /***
     * Generate objects from individual tiles (based on respective tile's object).
     *
     * This function provides functionality that should really be in LibGDX's TiledMap package.
     */
    private void setCollisionObjects() {
        for (MapLayer mapLayer : tiledMap.getLayers()) {

            if (mapLayer instanceof TiledMapTileLayer) {
                TiledMapTileLayer tileLayer = (TiledMapTileLayer) mapLayer;

                //scan across
                for (int x = 0; x <= tileLayer.getWidth(); x++) {
                    //scan up
                    for (int y = 0; y <= tileLayer.getHeight(); y++) {
                        TiledMapTileLayer.Cell cell = tileLayer.getCell(x, y);
                        if (cell != null) {
                            TiledMapTile tile = tileLayer.getCell(x, y).getTile();
                            if (tile.getObjects() != null) {

                                Iterator<MapObject> iterator = tile.getObjects().iterator();
                                while (iterator.hasNext()) {
                                    MapObject mapObject = iterator.next();
                                    if (mapObject instanceof PolygonMapObject) {
                                        PolygonMapObject polygonMapObject = (PolygonMapObject) mapObject;
                                        Polygon oldPoly = polygonMapObject.getPolygon();
                                        collisionObjects.add(convertTiledPolygonToMap(oldPoly, x, y));
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
        Gdx.app.log("PirateMap", "Loaded " + this.collisionObjects.size() + " collision objects!");
    }

    /***
     * Convert TiledPolygon to Polygon with correct relative coordinate.
     * @param tiledPolyon input polygon
     * @param x x offset
     * @param y y offset
     * @return Polygon with respect to map coordinates
     */
    private Polygon convertTiledPolygonToMap(Polygon tiledPolyon, Integer x, Integer y) {
        Polygon polygon = new Polygon();
        polygon.setVertices(tiledPolyon.getVertices());
        polygon.setOrigin(tiledPolyon.getOriginX(), tiledPolyon.getOriginY());
        polygon.setPosition((x+(tiledPolyon.getX()/64))*32f, (y+(tiledPolyon.getY())/64)*32f);
        //TODO: LibGDX fault. LoadObject does not correctly get tile rotation.
        //Rotation value is always 0 (even on rotated tiles).
        //Simple replace with non-rotated tiles in map editor.
        //polygon.setRotation(polygonMapObject.getPolygon().getRotation());
        polygon.setScale(1/2f, 1/2f);
        return polygon;
    }

    //check if map contains objectlayer
    private boolean checkObjectLayer() {
        this.objectLayer = tiledMap.getLayers().get(objectLayerName);
        if (this.objectLayer != null) {
            return setSpawnObject();
        }
        return false;
    }

    /***
     * Gets a map (not collision) object with the specified name.
     * @param objectName
     * @return
     */
    public Optional<RectangleMapObject> getMapObject(String objectName) {
        try {
            MapObject mapObject = objectLayer.getObjects().get(objectName);
            if (mapObject instanceof RectangleMapObject) {
                return Optional.of((RectangleMapObject) mapObject);
            }
        } catch (NullPointerException e) {}
        return Optional.empty();
    }

    //set spawn object
    private boolean setSpawnObject() {
        MapObject mapObject = objectLayer.getObjects().get(spawnPointObject);
        if (mapObject != null && mapObject instanceof RectangleMapObject) {
            RectangleMapObject object = (RectangleMapObject) mapObject;
            this.spawnPoint = scaleTiledVectorToMap(new Vector2(object.getRectangle().x, object.getRectangle().y));
            return true;
        }
        return false;
    }

    //scale tiledvector to real vector
    public Vector2 scaleTiledVectorToMap(Vector2 tiledVector) {
        return tiledVector.scl(1 / 2f);
    }

}
