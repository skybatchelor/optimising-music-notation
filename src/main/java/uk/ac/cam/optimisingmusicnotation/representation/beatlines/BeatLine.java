package uk.ac.cam.optimisingmusicnotation.representation.beatlines;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

import java.awt.*;

public class BeatLine implements PulseLine {
    @Override
    public MusicalPosition getMusicalPosition() {
        return musicalPosition;
    }

    private final MusicalPosition musicalPosition;

    private final int beatWeight;

    public BeatLine(MusicalPosition musicalPosition, int beatWeight) {
        this.musicalPosition = musicalPosition;
        this.beatWeight = beatWeight;
    }


    public <Anchor> void drawAboveStave(MusicCanvas<Anchor> canvas) {
        Anchor startAnchor = canvas.getAnchor(musicalPosition);
        switch (beatWeight) {
            case 1 -> canvas.drawLine(startAnchor,0f, RenderingConfiguration.pulseLineHeight,0f,0.25f,
                    RenderingConfiguration.beatLineWidth, RenderingConfiguration.greyColor);
            case 2 -> canvas.drawLine(startAnchor,0f,RenderingConfiguration.pulseLineHeight,0f,0.25f,
                    RenderingConfiguration.subBeatLineWidth, RenderingConfiguration.greyColor);
            default -> canvas.drawLine(startAnchor,0f,RenderingConfiguration.pulseLineHeight,0f,0.25f,
                    RenderingConfiguration.subBeatLineWidth / (beatWeight + 1), RenderingConfiguration.greyColor);
        }
    }

    public <Anchor> void drawFull(MusicCanvas<Anchor> canvas) {
        Anchor startAnchor = canvas.getAnchor(musicalPosition);
        switch (beatWeight) {
            case 1 -> canvas.drawLine(startAnchor,0f,0.5f,0f,-4f,RenderingConfiguration.beatLineWidth, RenderingConfiguration.greyColor);
            case 2 -> canvas.drawLine(startAnchor,0f,0.5f,0f,-4f,RenderingConfiguration.subBeatLineWidth, RenderingConfiguration.greyColor);
            default -> canvas.drawLine(startAnchor,0f,0.5f,0f,-4f,RenderingConfiguration.subBeatLineWidth / (beatWeight + 1), RenderingConfiguration.greyColor);
        }
    }
}
