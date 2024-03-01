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

    default Whitespace moveToNextLine(Line line) {
        return new Rest(new MusicalPosition(line, getStartMusicalPosition().crotchetsIntoLine() - getStartMusicalPosition().line().getLengthInCrotchets()),
                new MusicalPosition(line, getEndMusicalPosition().crotchetsIntoLine() - getEndMusicalPosition().line().getLengthInCrotchets()));
    }

    default Whitespace moveToPrevLine(Line line) {
        return new Rest(new MusicalPosition(line, getStartMusicalPosition().crotchetsIntoLine() + line.getLengthInCrotchets()),
                new MusicalPosition(line, getEndMusicalPosition().crotchetsIntoLine() + line.getLengthInCrotchets()));
    }

    <Anchor> void draw(MusicCanvas<Anchor> canvas, Line line);

    MusicalPosition getStartMusicalPosition();
    MusicalPosition getEndMusicalPosition();
}
