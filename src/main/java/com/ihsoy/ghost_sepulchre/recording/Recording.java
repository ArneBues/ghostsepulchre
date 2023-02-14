package com.ihsoy.ghost_sepulchre.recording;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

import java.util.ArrayList;
@Slf4j
public class Recording {
    @Getter
    private final ArrayList<RecordingTile> points;
    @Getter
    private final RecordingTile startPoint;

    @Getter
    private long runTime = -1;
    private long start = -1;
    public Recording(LocalPoint startPoint, Client client) {
        points = new ArrayList<>();
        final WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, startPoint);

       this.startPoint = new RecordingTile(worldPoint.getRegionID(), worldPoint.getRegionX(), worldPoint.getRegionY(), worldPoint.getPlane());
       log.info("Start Point: {} Hash: {}", this.startPoint, this.startPoint.hashCode());
    }

    public void startRecording() {
        if(runTime != -1) throw new RuntimeException("Recording has already been finished");
        if(start != -1) throw new RuntimeException("Recording can only be started once");
        start = System.nanoTime();
    }

    public void stopRecording() {
        if(runTime != -1) throw new RuntimeException("Recording has already been finished");
        if(start == -1) throw new RuntimeException("Recording was not started");

        runTime = System.nanoTime() - start;
    }

    public void addCurrentTile(Client client) {
        final LocalPoint lp = client.getLocalPlayer().getLocalLocation();
        final WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, lp);

        RecordingTile point = new RecordingTile(worldPoint.getRegionID(), worldPoint.getRegionX(), worldPoint.getRegionY(), worldPoint.getPlane());
        log.debug("Adding Point: {} - {}", point, worldPoint);

        points.add(point);
    }

    public Playback createPlayback() {
        return new Playback(this);
    }

    /**
     * Compares the runTime of two recordings.
     * Three-way comparison between this and recording
     * similar to the spaceship operator in c++
     * this <=> recording
     * @param recording rhs
     * @return  this < recording   => -1
     *          this > recording   =>  1
     *          this == recording  =>  0
     */
    public int compare(Recording recording) {
        if(runTime == -1 || recording.runTime == -1) throw new RuntimeException("Both recording have be to completed to be recorded compared");
        return Long.compare(runTime, recording.runTime);
    }

    public int count() {
        return points.size();
    }

    @Override
    public String toString() {
        return startPoint.toString() + " Size: " + points.size();
    }
}
