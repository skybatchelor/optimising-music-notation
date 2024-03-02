package uk.ac.cam.optimisingmusicnotation.representation.beatlines;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;

public interface PulseLine {
    MusicalPosition getMusicalPosition();
    <Anchor> void drawAroundStave(MusicCanvas<Anchor> canvas, boolean extendUp, boolean extendDown);
    <Anchor> void drawFull(MusicCanvas<Anchor> canvas);
}
