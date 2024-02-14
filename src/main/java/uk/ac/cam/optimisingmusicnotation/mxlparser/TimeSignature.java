package uk.ac.cam.optimisingmusicnotation.mxlparser;

public class TimeSignature {
    int beatNum;
    int beatType;

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
