package uk.ac.cam.optimisingmusicnotation.rendering;

import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch;

public interface MusicCanvas<Anchor> {

    Anchor getAnchor(MusicalPosition musicalPosition);
    Anchor getAnchor(MusicalPosition musicalPosition, Pitch pitch);

    void drawCircle(Anchor anchor, float x, float y, float r);
    void drawLine(Anchor anchor, float x1, float y1, float x2, float y2, float lineWidth);
    void drawLine(Anchor anchor1,  float x1, float y1, Anchor anchor2, float x2, float y2, float lineWidth);
}