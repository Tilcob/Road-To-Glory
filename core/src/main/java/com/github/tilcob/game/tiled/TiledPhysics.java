package com.github.tilcob.game.tiled;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.tilcob.game.config.Constants;

public class TiledPhysics {

    public static FixtureDef fixtureDefOf(MapObject object, Vector2 scaling, Vector2 relativeTo) {
        if (object instanceof RectangleMapObject rectMapObj) {
            return rectangleFixtureDef(rectMapObj, scaling, relativeTo);
        } else if (object instanceof EllipseMapObject ellipseMapObj) {
            return ellipseFixtureDef(ellipseMapObj, scaling, relativeTo);
        } else if (object instanceof PolygonMapObject polygonMapObject) {
            Polygon polygon = polygonMapObject.getPolygon();
            float offsetX = polygon.getX() * Constants.UNIT_SCALE;
            float offsetY = polygon.getY() * Constants.UNIT_SCALE;
            return polygonFixtureDef(polygonMapObject, polygon.getVertices(), offsetX, offsetY, scaling, relativeTo);
        } else if (object instanceof PolylineMapObject polylineMapObject) {
            Polyline polyline = polylineMapObject.getPolyline();
            float offsetX = polyline.getX() * Constants.UNIT_SCALE;
            float offsetY = polyline.getY() * Constants.UNIT_SCALE;
            return polygonFixtureDef(polylineMapObject, polyline.getVertices(), offsetX, offsetY, scaling, relativeTo);
        } else {
            throw new GdxRuntimeException("Unsupported map object: " + object);
        }
    }

    private static FixtureDef polygonFixtureDef(MapObject mapObject,
                                                float[] polyVertices,
                                                float offsetX,
                                                float offsetY,
                                                Vector2 scaling,
                                                Vector2 relativeTo) {
        offsetX = (offsetX * scaling.x) - relativeTo.x;
        offsetY = (offsetY * scaling.y) - relativeTo.y;
        float[] vertices = new float[polyVertices.length];

        for (int i = 0; i < polyVertices.length; i+=2) {
            vertices[i] = offsetX + polyVertices[i] * Constants.UNIT_SCALE * scaling.x;
            vertices[i + 1] = offsetY + polyVertices[i + 1] * Constants.UNIT_SCALE * scaling.y;
        }

        ChainShape shape = new ChainShape();
        if (mapObject instanceof PolygonMapObject) {
            shape.createLoop(vertices);
        } else {
            shape.createChain(vertices);
        }

        return fixtureDefOfMapObjectAndShape(mapObject, shape);
    }

    private static FixtureDef ellipseFixtureDef(EllipseMapObject mapObject, Vector2 scaling, Vector2 relativeTo) {
        Ellipse ellipse = mapObject.getEllipse();
        float x = ellipse.x;
        float y = ellipse.y;
        float width = ellipse.width;
        float height = ellipse.height;

        float ellipseX = x * Constants.UNIT_SCALE * scaling.x - relativeTo.x;
        float ellipseY = y * Constants.UNIT_SCALE * scaling.y - relativeTo.y;
        float ellipseWidth = width * Constants.UNIT_SCALE * scaling.x * .5f;
        float ellipseHeight = height * Constants.UNIT_SCALE * scaling.y * .5f;

        if (MathUtils.isEqual(ellipseWidth, ellipseHeight, .1f)) {
            // width and height are equal -> circle
            CircleShape shape = new CircleShape();
            shape.setPosition(new Vector2(ellipseX + ellipseWidth, ellipseY + ellipseHeight));
            shape.setRadius(ellipseWidth);
            return fixtureDefOfMapObjectAndShape(mapObject, shape);
        }
        final int numVertices = Constants.MAX_NUM_OF_VERTICES;
        float angleStep = MathUtils.PI2 / numVertices;
        Vector2[] vertices = new Vector2[numVertices];

        for (int i = 0; i < numVertices; i++) {
            float angle = i * angleStep;
            float offestX = ellipseWidth * MathUtils.cos(angle);
            float offestY = ellipseHeight * MathUtils.sin(angle);
            vertices[i] = new Vector2(ellipseX + ellipseWidth + offestX, ellipseY + ellipseHeight + offestY);
        }

        PolygonShape shape = new PolygonShape();
        shape.set(vertices);
        return fixtureDefOfMapObjectAndShape(mapObject, shape);
    }

    private static FixtureDef rectangleFixtureDef(RectangleMapObject rectMapObj, Vector2 scaling, Vector2 relativeTo) {
        Rectangle rectangle = rectMapObj.getRectangle();
        float x = rectangle.x;
        float y = rectangle.y;
        float width = rectangle.width;
        float height = rectangle.height;

        float boxX = x * Constants.UNIT_SCALE * scaling.x - relativeTo.x;
        float boxY = y * Constants.UNIT_SCALE * scaling.y - relativeTo.y;
        float boxWidth = width * Constants.UNIT_SCALE * scaling.x * .5f;
        float boxHeight = height * Constants.UNIT_SCALE * scaling.y * .5f;

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(boxWidth, boxHeight, new Vector2(boxX + boxWidth, boxY + boxHeight), 0);

        return fixtureDefOfMapObjectAndShape(rectMapObj, shape);
    }

    private static FixtureDef fixtureDefOfMapObjectAndShape(MapObject rectMapObj, Shape shape) {
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = rectMapObj.getProperties().get(Constants.FRICTION, 0f, Float.class);
        fixtureDef.restitution = rectMapObj.getProperties().get(Constants.RESTITUTION, 0f, Float.class);
        fixtureDef.density = rectMapObj.getProperties().get(Constants.DENSITY, 0f, Float.class);
        fixtureDef.isSensor = rectMapObj.getProperties().get(Constants.SENSOR, false, Boolean.class);

        return fixtureDef;
    }
}
