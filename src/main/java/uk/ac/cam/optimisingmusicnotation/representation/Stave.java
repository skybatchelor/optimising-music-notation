package uk.ac.cam.optimisingmusicnotation.representation;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.*;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.StaveElement;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups.MusicGroup;
import uk.ac.cam.optimisingmusicnotation.representation.whitespaces.Whitespace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a single stave of music.
 */
public class Stave {
    private final List<StaveElement> staveElements;
    private final List<Whitespace> whitespaces;
    private final List<MusicGroup> musicGroups;
    private final Line line;

    /**
     * Gets the number of this stave in the given line
     * @return the stave number
     */
    public int getStaveNumber() {
        return staveNumber;
    }

    private final int staveNumber;

    public Stave(Line line, int staveNumber) {
        this.line = line;
        this.staveNumber = staveNumber;
        staveElements = new ArrayList<>();
        whitespaces = new ArrayList<>();
        musicGroups = new ArrayList<>();
    }

    public Stave(Line line, int staveNumber, List<StaveElement> staveElements, List<Whitespace> whitespaces, List<MusicGroup> musicGroups) {
        this.line = line;
        this.staveNumber = staveNumber;
        this.staveElements = staveElements;
        this.whitespaces = whitespaces;
        this.musicGroups = musicGroups;
    }

    /**
     * Adds a {@link Whitespace} to this stave.
     * @param whitespace the whitespace to add
     */
    public void addWhiteSpace(Whitespace whitespace) {
        whitespaces.add(whitespace);
    }

    /**
     * Adds a {@link StaveElement} to this stave.
     * @param staveElement the stave element to add
     */
    public void addStaveElement(StaveElement staveElement) {
        staveElements.add(staveElement);
    }

    /**
     * Adds a {@link MusicGroup} to this stave.
     * @param musicGroup the music group to add
     */
    public void addMusicGroup(MusicGroup musicGroup) {
        musicGroups.add(musicGroup);
    }

    /**
     * Draws the given stave. Works by drawing the whitespaces, then the stave elements then the music groups, and then the stave lines.
     * @param canvas the canvas rendering the score
     * @param line the line the stave is being rendered on
     * @param <Anchor> the anchor type used by the canvas
     */
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Line line) {
        for (Whitespace w : whitespaces) {
            w.draw(canvas, line);
        }
        Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap = new HashMap<>();
        for (StaveElement s : staveElements) {
            s.draw(canvas, chordAnchorsMap);
        }
        for (MusicGroup m : musicGroups) {
            m.draw(canvas, chordAnchorsMap);
        }
        drawStaveLines(canvas, line);
    }

    /**
     * Draws a {@link Clef} and a {@link KeySignature}.
     * @param canvas the canvas rendering the score
     * @param line the line the stave is one
     * @param clef the clef being rendered
     * @param keySignature the key signature being rendered
     * @param <Anchor> the anchor type used by the canvas
     */
    public <Anchor> void drawWithClefAndKeySig(MusicCanvas<Anchor> canvas, Line line, Clef clef, KeySignature keySignature) {
        drawClefAndKey(canvas, clef, keySignature);
        draw(canvas, line);
    }

    private <Anchor> void drawClefAndKey(MusicCanvas<Anchor> canvas, Clef clef, KeySignature keySignature) {
        drawPreStaveLines(canvas, line, keySignature.getAlterations().size());
        clef.draw(canvas, line, this, keySignature.getAlterations().size());
        keySignature.draw(canvas, line, this, clef);
    }

    private <Anchor> void drawStaveLines(MusicCanvas<Anchor> canvas, Line line){
        //PRECONDITION: all whitespaces are grouped, and in order, with no overlapping
        MusicalPosition endOfLastWhitespace = new MusicalPosition(line, this,0);
        MusicalPosition startOfNextWhitespace;
        for (Whitespace w: whitespaces) {
            startOfNextWhitespace = w.getStartMusicalPosition();
            drawStaveLines(canvas, endOfLastWhitespace, startOfNextWhitespace);
            endOfLastWhitespace = w.getEndMusicalPosition();
        }
        if (endOfLastWhitespace.crotchetsIntoLine() < line.getLengthInCrotchets()){
            drawStaveLines(canvas,endOfLastWhitespace,new MusicalPosition(line, this, line.getLengthInCrotchets()));
        }
    }

    private <Anchor> void drawStaveLines(MusicCanvas<Anchor> canvas, MusicalPosition start, MusicalPosition end){
        if (start.compareTo(end) != 0) {
            Anchor anchor1;
            Anchor anchor2;

            for (int i = 0; i < 10; i = i + 2) {
                anchor1 = canvas.getAnchor(start, new Pitch(i, 0, 0));
                anchor2 = canvas.getAnchor(end, new Pitch(i, 0, 0));
                canvas.drawLine(anchor1, -0.5f, 0, anchor2, 0.5f, 0, RenderingConfiguration.staveLineWidth);
            }
        }
    }

    public <Anchor> void drawPreStaveLines(MusicCanvas<Anchor> canvas, Line line, int numAlterations){
        Anchor anchor1;

        for (int i = 0; i < 10; i=i+2) {
            anchor1 = canvas.getLineStartAnchor(new MusicalPosition(line, this, 0),new Pitch(i,0, 0));
            canvas.drawLine(anchor1, -(6f+numAlterations), 0, -2f, 0, 0.1f);
        }
    }

    public Line getLine() {
        return line;
    }
}
