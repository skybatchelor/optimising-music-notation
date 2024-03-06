package uk.ac.cam.optimisingmusicnotation.representation.whitespaces;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;

/**
 * An interface for generically representing whitespace on the score.
 */
public interface Whitespace {
    default float getStartCrotchets() {
        return getStartMusicalPosition().crotchetsIntoLine();
    }

    default float getEndCrotchets() {
        return getEndMusicalPosition().crotchetsIntoLine();
    }

    /**
     * Creates a copy of this whitespace on the next line
     * @param line the next line
     * @return the whitespace on the next line
     */
    default Whitespace moveToNextLine(Line line) {
        return new Rest(new MusicalPosition(line, getStartMusicalPosition().stave(), getStartMusicalPosition().crotchetsIntoLine() - getStartMusicalPosition().line().getLengthInCrotchets()),
                new MusicalPosition(line, getStartMusicalPosition().stave(), getEndMusicalPosition().crotchetsIntoLine() - getEndMusicalPosition().line().getLengthInCrotchets()));
    }

    /**
     * Creates a copy of this whitespace on the previous line
     * @param line the previous line
     * @return the whitespace on the previous line
     */
    default Whitespace moveToPrevLine(Line line) {
        return new Rest(new MusicalPosition(line, getStartMusicalPosition().stave(), getStartMusicalPosition().crotchetsIntoLine() + line.getLengthInCrotchets()),
                new MusicalPosition(line, getStartMusicalPosition().stave(), getEndMusicalPosition().crotchetsIntoLine() + line.getLengthInCrotchets()));
    }

    <Anchor> void draw(MusicCanvas<Anchor> canvas, Line line);

    MusicalPosition getStartMusicalPosition();
    MusicalPosition getEndMusicalPosition();
}
