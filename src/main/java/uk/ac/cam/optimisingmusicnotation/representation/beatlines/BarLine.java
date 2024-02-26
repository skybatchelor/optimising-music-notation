package uk.ac.cam.optimisingmusicnotation.representation.beatlines;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.properties.TimeSignature;

import java.awt.*;

public class BarLine implements PulseLine {
    @Override
    public MusicalPosition getMusicalPosition() {
        return musicalPosition;
    }
    private final MusicalPosition musicalPosition;
    private final String barName;

    public TimeSignature getTimeSignature() {
        return timeSignature;
    }

    private final TimeSignature timeSignature;

    public BarLine(MusicalPosition musicalPosition, String barName, TimeSignature timeSignature) {
        this.musicalPosition = musicalPosition;
        this.barName = barName;
        this.timeSignature = timeSignature;
    }

    public <Anchor> void drawAboveStave(MusicCanvas<Anchor> canvas) {
        Anchor startAnchor = canvas.getAnchor(musicalPosition);
        canvas.drawLine(startAnchor,0f,RenderingConfiguration.pulseLineHeight,0f,0.25f,
                RenderingConfiguration.barLineWidth, new Color(0xCCCCCC));
    }

    public <Anchor> void drawFull(MusicCanvas<Anchor> canvas) {
        Anchor startAnchor = canvas.getAnchor(musicalPosition);
        canvas.drawLine(startAnchor,0f,0.5f,0f,-4f,RenderingConfiguration.barLineWidth, new Color(0xCCCCCC));
    }
}
