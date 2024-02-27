package uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.rendering.TextAlignment;
import uk.ac.cam.optimisingmusicnotation.representation.properties.ChordAnchors;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType;

import java.io.IOException;
import java.util.Map;

public class TempoMarking extends MusicGroup {

    private MusicalPosition position;

    private NoteType leftItem;
    private int leftDots;

    private String rightText;
    private NoteType rightItem;
    private int rightDots;

    public TempoMarking(MusicalPosition position, NoteType leftItem, int leftDots, String rightText) {
        this.position = position;
        this.leftItem = leftItem;
        this.leftDots = leftDots;
        this.rightText = rightText;
        rightItem = NoteType.MAXIMA;
        rightDots = 0;
    }

    public TempoMarking(MusicalPosition position, NoteType leftItem, int leftDots, NoteType rightItem, int rightDots) {
        this.position = position;
        this.leftItem = leftItem;
        this.leftDots = leftDots;
        this.rightText = null;
        this.rightItem = rightItem;
        this.rightDots = rightDots;
    }

    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap) {
        Anchor anchor = canvas.getAnchor(position, new Pitch(25, 0, 0));
        Chord.draw(canvas, anchor, leftItem, leftDots, RenderingConfiguration.tempoNoteTimeScale, RenderingConfiguration.tempoNoteScale);
        if (rightText != null) {
            float width = rightText.length() * 1.5f + 1.5f;
            try {
                canvas.drawText(RenderingConfiguration.defaultFontFilePath, "=" + rightText,10f, TextAlignment.LEFT, anchor,
                        0.75f, 2.5f, width, 5f);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
