package uk.ac.cam.optimisingmusicnotation.rendering;

import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch;

import java.awt.*;
import java.io.IOException;

public interface MusicCanvas<Anchor> {

    Anchor getAnchor(MusicalPosition musicalPosition);
    Anchor getAnchor(MusicalPosition musicalPosition, Pitch pitch);
    Anchor offsetAnchor(Anchor anchor, float x, float y);
    Anchor interpolateAnchors(Anchor anchor1, Anchor anchor2, float t);
    Anchor topLeftAnchor();
    Anchor topCentreAnchor();
    Anchor topRightAnchor();

    boolean isAnchorBelow(Anchor anchor1, Anchor anchor2);

    void addLine();
    void reserveHeight(float height);

    void drawCircle(Anchor anchor, float x, float y, float r);
    void drawCircle(Anchor anchor, float x, float y, float r, boolean fill);

    void drawLine(Anchor anchor, float x1, float y1, float x2, float y2, float lineWidth);
    void drawLine(Anchor anchor, float x1, float y1, float x2, float y2, float lineWidth, Color color);
    void drawLine(Anchor anchor1,  float x1, float y1, Anchor anchor2, float x2, float y2, float lineWidth);
    void drawLine(Anchor anchor1,  float x1, float y1, Anchor anchor2, float x2, float y2, float lineWidth, Color color);

    void drawWhitespace(Anchor topLeftAnchor, float topLeftX, float topLeftY, float width, float height);
    void drawWhitespace(Anchor topLeftAnchor, float topLeftX, float topLeftY,
                        Anchor bottomRightAnchor, float bottomRightX, float bottomRightY);

    void drawImage(String fileName, Anchor topLeftAnchor, float topLeftX, float topLeftY, float width, float height)
            throws IOException;
    void drawImage(String fileName, Anchor topLeftAnchor, float topLeftX, float topLeftY,
                   Anchor bottomRightAnchor, float bottomRightX, float bottomRightY)
            throws IOException;

    void drawEllipse(Anchor centre, float x, float y, float rx, float ry, boolean fill);

    void drawBeam(Anchor left, float leftX, float leftY, Anchor right, float rightX, float rightY, float height);
    void drawBeam(Anchor anchor, float x1, float y1, float x2, float y2, float height);

    void drawCurve(Anchor start, float startX, float startY, Anchor end, float endX, float endY, float lineWidth, boolean up);

    void drawText(String fileName, String text, float fontSize, TextAlignment alignment,
                  Anchor topLeftAnchor, float topLeftX, float topLeftY, float width, float height)
            throws IOException;
}