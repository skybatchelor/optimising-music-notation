package uk.ac.cam.optimisingmusicnotation.representation.beatlines;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.properties.TimeSignature;

public class BarLine implements PulseLine {
    @Override
    public MusicalPosition getMusicalPosition() {
        return musicalPosition;
    }
    private final MusicalPosition musicalPosition;
    private final String barName;
    private final TimeSignature timeSignature;

    public BarLine(MusicalPosition musicalPosition, String barName, TimeSignature timeSignature) {
        this.musicalPosition = musicalPosition;
        this.barName = barName;
        this.timeSignature = timeSignature;
    }

    public <Anchor> void drawAboveStave(MusicCanvas<Anchor> canvas) {
        Anchor startAnchor = canvas.getAnchor(musicalPosition);
        canvas.drawLine(startAnchor,0f,2f,0f,0f,RenderingConfiguration.pulseLineWidth);
    }

    public <Anchor> void drawFull(MusicCanvas<Anchor> canvas) {
        Anchor startAnchor = canvas.getAnchor(musicalPosition);
        canvas.drawLine(startAnchor,0f,2f,0f,-4f,RenderingConfiguration.pulseLineWidth);
    }
}
