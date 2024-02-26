package uk.ac.cam.optimisingmusicnotation.representation.whitespaces;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;

public class ArtisticWhitespace implements Whitespace {
    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Line line) {

    }

    @Override
    public MusicalPosition getStartMusicalPosition() {
        return null;
    }

    @Override
    public MusicalPosition getEndMusicalPosition() {
        return null;
    }
}
