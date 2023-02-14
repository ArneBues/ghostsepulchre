package com.ihsoy.ghost_sepulchre.recording;

import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Playback {
    ArrayList<WorldPoint> points;
    private int tick_counter;
    @Getter
    private boolean done = false;
    @Getter
    private final Recording recording;

    public Playback(Recording rec) {
        recording = rec;
        tick_counter = 0;
    }

    public void translateToWorldPoint(Client client) {
        ArrayList<RecordingTile> tiles = recording.getPoints();
        List<WorldPoint> temp;
        if(tiles.isEmpty()) {
            temp = Collections.emptyList();
        }
        else {
            temp =  tiles.stream()
                    .map(tile -> WorldPoint.fromRegion(tile.getRegionId(), tile.getRegionX(), tile.getRegionY(), tile.getZ()))
                    .flatMap(worldPoint ->
                    {
                        final Collection<WorldPoint> localWorldPoints = WorldPoint.toLocalInstance(client, worldPoint);
                        return localWorldPoints.stream();
                    })
                    .collect(Collectors.toList());
        }
        points = new ArrayList<>(temp);
    }

    public WorldPoint getPoint() {
        if(done) return points.get(points.size() - 1);
        return points.get(tick_counter);
    }

    public void nextPoint() {
        tick_counter++;
        if(tick_counter >= points.size()) {
            done = true;
        }
    }

    public void rewind() {
        tick_counter = 0;
    }
}
