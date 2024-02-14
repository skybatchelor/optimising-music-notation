package uk.ac.cam.optimisingmusicnotation.representation.properties;

public enum ClefSign {
    G(8,2,3),
    F(3,4,2),
    C(4,3,2),
    PERCUSSION(2,3,1),
    TAB(4,3,2);
    public final int height;
    public final int defaultLinesFromBottomOfStave;
    public final int lineDistanceFromBottomOfClef;
    ClefSign(int height,int defaultLinesFromBottomOfStave,int lineDistanceFromBottomOfClef) {
        this.height = height;
        this.defaultLinesFromBottomOfStave = defaultLinesFromBottomOfStave;
        this.lineDistanceFromBottomOfClef = lineDistanceFromBottomOfClef;
    }
}
