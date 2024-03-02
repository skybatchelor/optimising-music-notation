package uk.ac.cam.optimisingmusicnotation.representation.beatlines;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.Stave;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;

public interface PulseLine {
    MusicalPosition getMusicalPosition();
    <Anchor> void drawAroundStave(MusicCanvas<Anchor> canvas, Stave stave, boolean extendUp, boolean extendDown, float downLength, boolean drawLabel);
    <Anchor> void drawFull(MusicCanvas<Anchor> canvas, Stave stave);
}
