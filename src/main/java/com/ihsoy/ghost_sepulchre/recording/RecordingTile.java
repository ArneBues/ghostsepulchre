package com.ihsoy.ghost_sepulchre.recording;

import lombok.Value;

@Value
public class RecordingTile {
    private int regionId;
    private int regionX;
    private int regionY;
    private int z;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(regionId);
        sb.append(regionX);
        sb.append(regionY);
        sb.append(z);
        return sb.toString();
    }

}
