package com.ihsoy.ghost_sepulchre.recording;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

import javax.inject.Inject;
import java.util.HashMap;
@Slf4j
public class RecordingHandler {

    private HashMap<String, Recording> recordingStorage;
    private final Client client;

    @Inject
    public RecordingHandler(Client client) {
        this.client = client;
        recordingStorage = new HashMap<>();
    }

    public Recording createRecording(LocalPoint startPoint) {
        return new Recording(startPoint, client);
    }

    public enum RecordResult {
        FASTER,
        SLOWER,
        FIRST
    }

    public RecordResult storeRecording(Recording recording) {
        RecordingTile startPoint = recording.getStartPoint();

        if(recordingStorage.containsKey(startPoint.toString())) {
            Recording stored = recordingStorage.get(startPoint.toString());
            if(recording.compare(stored) < 0) {
                recordingStorage.put(startPoint.toString(), recording);
                return RecordResult.FASTER;
            }
            return RecordResult.SLOWER;
        }

        recordingStorage.put(startPoint.toString(), recording);
        return RecordResult.FIRST;
    }

    public Recording findRecording(LocalPoint startPoint) {
        final WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, startPoint);
        RecordingTile point = new RecordingTile(worldPoint.getRegionID(), worldPoint.getRegionX(), worldPoint.getRegionY(), worldPoint.getPlane());
        recordingStorage.forEach((k,v) -> {
            log.info("Value: {} Size: {} Hash: {}", k, v.getPoints().size(), k.hashCode());
        });
        return recordingStorage.getOrDefault(point.toString(), null);
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(recordingStorage);
    }

    public void deleteRecordings() {
        recordingStorage = new HashMap<>();
    }

    public void loadJson(String json) {
        if(json == null || json.length() == 0) {
            return;
        }
        Gson gson = new Gson();
        try {
            recordingStorage = new HashMap<>();
            recordingStorage = gson.fromJson(json, recordingStorage.getClass());
        }
        catch (Exception e) {
            log.error("Could not load Recordings", e);
            //client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "<col=ef1020>Could not load recordings!</col>", null);
        }
    }

    public int recordingCount() {
        return recordingStorage.size();
    }
}
