package uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.ChordAnchors;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;

import java.util.List;
import java.util.Map;

public class ImageAnnotation extends MusicGroup {
    private final String filePath;
    private final MusicalPosition musicalPosition;
    private final boolean aboveStave;
    private final float width;
    private final float height;
    private final float offset;

    public ImageAnnotation(List<Chord> chords, String filePath, MusicalPosition musicalPosition, boolean aboveStave, float width, float height, float offset) {
        super(chords);
        this.filePath = filePath;
        this.musicalPosition = musicalPosition;
        this.aboveStave = aboveStave;
        this.width = width;
        this.height = height;
        this.offset = offset;
    }

    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap) {
        if (aboveStave) {
            Anchor anchor = canvas.getAnchor(musicalPosition);
            Anchor highestAnchor = canvas.getMinAnchor(chords.stream().map((chord) -> chordAnchorsMap.get(chord).getHighestAnchor(canvas, chord)).toList(), anchor, canvas::isAnchorAbove);
            anchor = canvas.getTakeXTakeYAnchor(canvas.getAnchor(musicalPosition), highestAnchor);
            try{
                canvas.drawImage(filePath, anchor,-width/2, offset + height, width, height);
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            Anchor anchor = canvas.getLowestStaveLineAnchor(musicalPosition);
            Anchor lowestAnchor = canvas.getMinAnchor(chords.stream().map((chord) -> chordAnchorsMap.get(chord).getLowestAnchor(canvas, chord)).toList(), anchor, canvas::isAnchorBelow);
            anchor = canvas.getTakeXTakeYAnchor(canvas.getAnchor(musicalPosition), lowestAnchor);
            try{
                canvas.drawImage(filePath, anchor,-width/2, -offset, width, height);
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
