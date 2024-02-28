package uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.rendering.TextAlignment;
import uk.ac.cam.optimisingmusicnotation.representation.properties.ChordAnchors;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TextAnnotation extends MusicGroup {
    private final String text;
    private final MusicalPosition musicalPosition;
    private final boolean aboveStave;

    public TextAnnotation(List<Chord> chords, String text, MusicalPosition musicalPosition, boolean aboveStave) {
        super(chords);
        this.text = text;
        this.musicalPosition = musicalPosition;
        this.aboveStave = aboveStave;
    }

    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap) {
        if (aboveStave) {
            Anchor anchor = canvas.getAnchor(musicalPosition);
            Anchor highestAnchor = canvas.getMinAnchor(chords.stream().map((chord) -> chordAnchorsMap.get(chord).getHighestAnchor(canvas, chord)).toList(), anchor, canvas::isAnchorAbove);
            anchor = canvas.getTakeXTakeYAnchor(canvas.getAnchor(musicalPosition), highestAnchor);
            float width = text.length() * 1.5f;
            try {
                canvas.drawText(RenderingConfiguration.defaultFontFilePath, text,10f, TextAlignment.LEFT, anchor,
                        0, RenderingConfiguration.staveTextHeight, width, RenderingConfiguration.staveTextHeight);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            Anchor anchor = canvas.getLowestStaveLineAnchor(musicalPosition);
            Anchor lowestAnchor = canvas.getMinAnchor(chords.stream().map((chord) -> chordAnchorsMap.get(chord).getLowestAnchor(canvas, chord)).toList(), anchor, canvas::isAnchorBelow);
            anchor = canvas.getTakeXTakeYAnchor(canvas.getAnchor(musicalPosition), lowestAnchor);
            float width = text.length() * 1.5f;
            try {
                canvas.drawText(RenderingConfiguration.defaultFontFilePath, text,10f, TextAlignment.LEFT, anchor,
                        0, 0, width, RenderingConfiguration.staveTextHeight);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
