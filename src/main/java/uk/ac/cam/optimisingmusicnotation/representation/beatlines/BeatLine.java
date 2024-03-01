package uk.ac.cam.optimisingmusicnotation.representation.beatlines;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch;
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


    public <Anchor> void drawAroundStave(MusicCanvas<Anchor> canvas, boolean extendUp, boolean extendDown) {
        Anchor startAnchor = canvas.getAnchor(musicalPosition);
        Anchor endAnchor;
        int lineNumber = musicalPosition.line().getLineNumber();
        Anchor defaultEndAnchor = canvas.offsetAnchor(startAnchor, 0f, RenderingConfiguration.pulseLineHeight);
        if (extendUp && lineNumber > 0) {
            endAnchor = canvas.getTakeXTakeYAnchor(startAnchor, canvas.getTrueBottomAnchor(lineNumber - 1));
            if (!canvas.areAnchorsOnSamePage(startAnchor, endAnchor)) {
                endAnchor = defaultEndAnchor;
            }
        } else {
            endAnchor = defaultEndAnchor;
        }

        switch (beatWeight) {
            case 1 -> canvas.drawLine(startAnchor,0f, 0.25f, endAnchor,0f, 0f,
                    RenderingConfiguration.beatLineWidth, new Color(0xCCCCCC), false);
            case 2 -> canvas.drawLine(startAnchor,0f, 0.25f, endAnchor,0f, 0f,
                    RenderingConfiguration.subBeatLineWidth, new Color(0xCCCCCC), false);
            default -> canvas.drawLine(startAnchor,0f, 0.25f, endAnchor,0f, 0f,
                    RenderingConfiguration.subBeatLineWidth / (beatWeight + 1), new Color(0xCCCCCC), false);
        }

        if (extendDown) {
            startAnchor = canvas.getAnchor(musicalPosition, new Pitch(0, 0, 0));
            switch (beatWeight) {
                case 1 -> canvas.drawLine(startAnchor,0f, -0.25f,0f, -10f,
                        RenderingConfiguration.beatLineWidth, new Color(0xCCCCCC), false);
                case 2 -> canvas.drawLine(startAnchor,0f, -0.25f,0f, -10f,
                        RenderingConfiguration.subBeatLineWidth, new Color(0xCCCCCC), false);
                default -> canvas.drawLine(startAnchor,0f, -0.25f,0f, -10f,
                        RenderingConfiguration.subBeatLineWidth / (beatWeight + 1), new Color(0xCCCCCC), false);
            }
        }
    }

    public <Anchor> void drawFull(MusicCanvas<Anchor> canvas) {
        Anchor startAnchor = canvas.getAnchor(musicalPosition);
        switch (beatWeight) {
            case 1 -> canvas.drawLine(startAnchor,0f,0.25f,0f,-4.25f,RenderingConfiguration.beatLineWidth, new Color(0xCCCCCC));
            case 2 -> canvas.drawLine(startAnchor,0f,0.25f,0f,-4.25f,RenderingConfiguration.subBeatLineWidth, new Color(0xCCCCCC));
            default -> canvas.drawLine(startAnchor,0f,0.25f,0f,-4.25f,RenderingConfiguration.subBeatLineWidth / (beatWeight + 1), new Color(0xCCCCCC));
        }
    }
}
