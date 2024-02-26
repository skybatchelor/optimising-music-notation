package uk.ac.cam.optimisingmusicnotation.representation.properties;

public enum ClefSign {
    G(8,2,3),
    F(3,6,2),
    C(4,4,2),
    PERCUSSION(2,6,1),
    TAB(4,6,2);
    public final float height;
    public final int defaultLinesFromBottomOfStave;
    public final int lineDistanceFromBottomOfClef;
    ClefSign(float height,int defaultLinesFromBottomOfStave,int lineDistanceFromBottomOfClef) {
        this.height = height;
        this.defaultLinesFromBottomOfStave = defaultLinesFromBottomOfStave;
        this.lineDistanceFromBottomOfClef = lineDistanceFromBottomOfClef;
    }
}
