package uk.ac.cam.optimisingmusicnotation.representation;

import uk.ac.cam.optimisingmusicnotation.Canvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

public interface Drawable {
    void Draw(Canvas canvas, RenderingConfiguration config);
}
