package uk.ac.cam.optimisingmusicnotation.representation.beatlines;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.rendering.TextAlignment;
import uk.ac.cam.optimisingmusicnotation.representation.Stave;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.properties.TimeSignature;

import java.io.IOException;

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

    @Override
    public <Anchor> void drawAroundStave(MusicCanvas<Anchor> canvas, Stave stave, boolean extendUp, boolean extendDown, float downLength, boolean drawLabel) {
        MusicalPosition stavedPosition = musicalPosition.getPositionWithStave(stave);
        Anchor startAnchor = canvas.getAnchor(stavedPosition);
        Anchor endAnchor;
        int lineNumber = stavedPosition.getIndex();
        Anchor defaultEndAnchor = canvas.offsetAnchor(startAnchor, 0f, RenderingConfiguration.pulseLineHeight);
        if (extendUp && lineNumber > 0) {
            endAnchor = canvas.getTakeXTakeYAnchor(startAnchor, canvas.getTrueBottomAnchor(lineNumber - 1));
            if (!canvas.areAnchorsOnSamePage(startAnchor, endAnchor)) {
                endAnchor = defaultEndAnchor;
            }
        } else {
            endAnchor = defaultEndAnchor;
        }
        canvas.drawLine(startAnchor,0f,0.25f, endAnchor, 0f, 0f,
                RenderingConfiguration.barLineWidth, RenderingConfiguration.greyColor, false);

        if (extendDown) {
            Anchor downStartAnchor = canvas.getAnchor(stavedPosition,
                    new Pitch(0, 0, 0));
            canvas.drawLine(downStartAnchor,0f, -0.25f,0f, -downLength,
                    RenderingConfiguration.barLineWidth, RenderingConfiguration.greyColor, false);
        }

        if (drawLabel) {
            float width = barName.length() * 1.5f;
            try {
                canvas.drawText(RenderingConfiguration.defaultFontFilePath, barName,7.5f, TextAlignment.LEFT, startAnchor,
                        timeSignature == null ? 0.5f : 1.0f, RenderingConfiguration.pulseLineHeight + 1f, width, 5f, RenderingConfiguration.greyColor);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public <Anchor> void drawFull(MusicCanvas<Anchor> canvas, Stave stave) {
        Anchor startAnchor = canvas.getAnchor(musicalPosition.getPositionWithStave(stave));
        canvas.drawLine(startAnchor,0f,0.25f,0f,-4.25f,RenderingConfiguration.barLineWidth, RenderingConfiguration.greyColor);
    }
}
