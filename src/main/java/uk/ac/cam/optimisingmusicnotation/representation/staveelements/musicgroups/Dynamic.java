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

public class Dynamic extends MusicGroup {
    private String text;
    private MusicalPosition musicalPosition;

    public Dynamic (List<Chord> chords, String text, MusicalPosition musicalPosition) {
        super(chords);
        this.text = text;
        this.musicalPosition = musicalPosition;
    }

    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap) {
        // TODO: actually render properly (I have once again botched something together)
        Anchor anchor = canvas.getAnchor(musicalPosition);
        float width = text.length() * 1.5f;
        try {
            canvas.drawText(RenderingConfiguration.fontFilePath, text,10f, TextAlignment.CENTRE, anchor,-width/2,width/2, width,5f);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
