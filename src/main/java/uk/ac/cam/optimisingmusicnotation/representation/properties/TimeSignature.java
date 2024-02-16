package uk.ac.cam.optimisingmusicnotation.representation.properties;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;

public class TimeSignature {
    private int beatNum;
    private int beatType;
    public int getBeatNum() {
        return beatNum;
    }
    public int getBeatType() {
        return beatType;
    }

    public TimeSignature() {
        beatNum = 0;
        beatType = 0;
    }

    public TimeSignature(int beatNum, int beatType) {
        this.beatNum = beatNum;
        this.beatType = beatType;
    }

    public void setBeatNum(int beatNum) { this.beatNum = beatNum; }

    public void setBeatType(int beatType) { this.beatType = beatType; }

}
