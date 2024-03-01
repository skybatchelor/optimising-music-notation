package uk.ac.cam.optimisingmusicnotation.representation.whitespaces;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;

public interface Whitespace {
    default float getStartCrotchets() {
        return getStartMusicalPosition().crotchetsIntoLine();
    }

    default float getEndCrotchets() {
        return getEndMusicalPosition().crotchetsIntoLine();
    }

    <Anchor> void draw(MusicCanvas<Anchor> canvas, Line line);

    MusicalPosition getStartMusicalPosition();
    MusicalPosition getEndMusicalPosition();
}
