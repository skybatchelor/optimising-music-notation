package uk.ac.cam.optimisingmusicnotation.rendering;

import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch;

import java.awt.*;

public interface MusicCanvas<Anchor> {

    Anchor getAnchor(MusicalPosition musicalPosition);
    Anchor getAnchor(MusicalPosition musicalPosition, Pitch pitch);

    void drawCircle(Anchor anchor, float x, float y, float r);
    void drawLine(Anchor anchor, float x1, float y1, float x2, float y2, float lineWidth);
    void drawLine(Anchor anchor, float x1, float y1, float x2, float y2, float lineWidth, Color color);
    void drawLine(Anchor anchor1,  float x1, float y1, Anchor anchor2, float x2, float y2, float lineWidth);
    void drawLine(Anchor anchor1,  float x1, float y1, Anchor anchor2, float x2, float y2, float lineWidth, Color color);
    void drawWhitespace(Anchor topLeftAnchor, float topLeftX, float topLeftY, float width, float height);
    void drawWhitespace(Anchor topLeftAnchor, float topLeftX, float topLeftY,
                        Anchor bottomRightAnchor, float bottomRightX, float bottomRightY);
    void drawImage(String fileName, Anchor topLeftAnchor, float topLeftX, float topLeftY, float width, float height);
    void drawImage(String fileName, Anchor topLeftAnchor, float topLeftX, float topLeftY,
                   Anchor bottomRightAnchor, float bottomRightX, float bottomRightY);
}