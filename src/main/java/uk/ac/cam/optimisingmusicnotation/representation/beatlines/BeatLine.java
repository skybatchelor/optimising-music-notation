package uk.ac.cam.optimisingmusicnotation.representation.beatlines;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

public class BeatLine implements PulseLine {
    @Override
    public MusicalPosition getMusicalPosition() {
        return musicalPosition;
    }

    private MusicalPosition musicalPosition;

    private int beatWeight;

    public BeatLine(MusicalPosition musicalPosition, int beatWeight) {
        this.musicalPosition = musicalPosition;
        this.beatWeight = beatWeight;
    }


    public <Anchor> void drawAboveStave(MusicCanvas<Anchor> canvas, RenderingConfiguration config) {
        Anchor startAnchor = canvas.getAnchor(musicalPosition);
        canvas.drawLine(startAnchor,0f,2f,0f,0f,RenderingConfiguration.pulseLineWidth);
    }

    public <Anchor> void drawFull(MusicCanvas<Anchor> canvas, RenderingConfiguration config) {
        Anchor startAnchor = canvas.getAnchor(musicalPosition);
        canvas.drawLine(startAnchor,0f,2f,0f,-4f,RenderingConfiguration.pulseLineWidth);
    }
}
