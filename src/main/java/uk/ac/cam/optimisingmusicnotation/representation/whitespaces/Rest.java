package uk.ac.cam.optimisingmusicnotation.representation.whitespaces;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

public class Rest implements Whitespace {

    private final MusicalPosition startMusicalPosition;
    private final MusicalPosition endMusicalPosition;

    public Rest(MusicalPosition startMusicalPosition, MusicalPosition endMusicalPosition) {
        this.startMusicalPosition = startMusicalPosition;
        this.endMusicalPosition = endMusicalPosition;
    }

    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Line line, RenderingConfiguration config) {
        canvas.drawWhitespace(canvas.getAnchor(startMusicalPosition), 0, (float).5, canvas.getAnchor(endMusicalPosition), 0, (float)(-4.5));
    }
}
