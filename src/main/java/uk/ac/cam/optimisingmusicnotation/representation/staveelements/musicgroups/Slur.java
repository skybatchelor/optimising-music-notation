package uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.Stave;
import uk.ac.cam.optimisingmusicnotation.representation.properties.ChordAnchors;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;

import java.util.List;
import java.util.Map;

/**
 * A slur across several notes.
 */
public class Slur extends MusicGroup {
    private final Chord firstChord;
    private final Chord lastChord;
    private final Line line;
    private final Stave stave;

    public Slur(List<Chord> chords, Chord firstChord, Chord lastChord, Line line, Stave stave) {
        super(chords);
        this.firstChord = firstChord;
        this.lastChord = lastChord;
        this.line = line;
        this.stave = stave;
    }

    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap) {
        float absoluteYOffset = 1.5f; // set vertical offset from Notehead here
        float signedYOffset = RenderingConfiguration.upwardStems ? -absoluteYOffset : absoluteYOffset;
        float startNoteheadOffset = 0;
        float endNoteheadOffset = 0;

        // default anchors are at the start and end of the line; designed for slurs across the line
        Anchor startAnchor = canvas.getAnchor(new MusicalPosition(line, stave, 0));
        Anchor endAnchor = canvas.getAnchor(new MusicalPosition(line, stave, line.getLengthInCrotchets()));


        ChordAnchors<Anchor> firstChordAnchors = chordAnchorsMap.get(firstChord);
        if (firstChordAnchors != null) {
            startAnchor = firstChordAnchors.notehead();
            startNoteheadOffset = firstChordAnchors.noteheadOffset();
        }

        ChordAnchors<Anchor> lastChordAnchors = chordAnchorsMap.get(lastChord);
        if (lastChordAnchors != null) {
            endAnchor = lastChordAnchors.notehead();
            endNoteheadOffset = lastChordAnchors.noteheadOffset();
        }

        canvas.drawCurve(startAnchor, 0, signedYOffset + startNoteheadOffset, endAnchor, 0, signedYOffset + endNoteheadOffset, .2f, !RenderingConfiguration.upwardStems);
    }
}
