package uk.ac.cam.optimisingmusicnotation.mxlparser;

public class Clef {
    public enum ClefType {
        C, F, G, PERCUSSION
    }

    ClefType clefType;
    int staveLine;
    int octaveOffset;

    public Clef(ClefType clefType, int staveLine, int octaveOffset) {
        this.clefType = clefType;
        this.staveLine = staveLine;
        this.octaveOffset = octaveOffset;
    }

    public ClefType getClefType() {
        return clefType;
    }

    public int getStaveLine() {
        return staveLine;
    }

    public int getOctaveOffset() {
        return octaveOffset;
    }
}
