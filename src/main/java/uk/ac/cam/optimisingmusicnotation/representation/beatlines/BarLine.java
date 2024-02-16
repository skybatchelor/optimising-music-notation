package uk.ac.cam.optimisingmusicnotation.representation.beatlines;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

public class BarLine implements PulseLine {
    @Override
    public MusicalPosition getMusicalPosition() {
        return musicalPosition;
    }

    private final MusicalPosition musicalPosition;
    private final String barName;

    public BarLine(MusicalPosition musicalPosition, String barName) {
        this.musicalPosition = musicalPosition;
        this.barName = barName;
    }

    public <Anchor> void drawAboveStave(MusicCanvas<Anchor> canvas, RenderingConfiguration config) {
        Anchor startAnchor = canvas.getAnchor(musicalPosition);
        canvas.drawLine(startAnchor,0f,2f,0f,0f,config.pulseLineWidth());
    }

    public <Anchor> void drawFull(MusicCanvas<Anchor> canvas, RenderingConfiguration config) {
        Anchor startAnchor = canvas.getAnchor(musicalPosition);
        canvas.drawLine(startAnchor,0f,2f,0f,-4f,config.pulseLineWidth());
    }
}
