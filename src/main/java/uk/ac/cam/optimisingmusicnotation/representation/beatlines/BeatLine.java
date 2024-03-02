package uk.ac.cam.optimisingmusicnotation.representation.beatlines;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.Stave;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

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

        switch (beatWeight) {
            case 1 -> canvas.drawLine(startAnchor,0f, 0.25f, endAnchor,0f, 0f,
                    RenderingConfiguration.beatLineWidth, RenderingConfiguration.greyColor, false);
            case 2 -> canvas.drawLine(startAnchor,0f, 0.25f, endAnchor,0f, 0f,
                    RenderingConfiguration.subBeatLineWidth, RenderingConfiguration.greyColor, false);
            default -> canvas.drawLine(startAnchor,0f, 0.25f, endAnchor,0f, 0f,
                    RenderingConfiguration.subBeatLineWidth / (beatWeight + 1), RenderingConfiguration.greyColor, false);
        }

        if (extendDown) {
            startAnchor = canvas.getAnchor(stavedPosition, new Pitch(0, 0, 0));
            switch (beatWeight) {
                case 1 -> canvas.drawLine(startAnchor,0f, -0.25f,0f, -downLength,
                        RenderingConfiguration.beatLineWidth, RenderingConfiguration.greyColor, false);
                case 2 -> canvas.drawLine(startAnchor,0f, -0.25f,0f, -downLength,
                        RenderingConfiguration.subBeatLineWidth, RenderingConfiguration.greyColor, false);
                default -> canvas.drawLine(startAnchor,0f, -0.25f,0f, -downLength,
                        RenderingConfiguration.subBeatLineWidth / (beatWeight + 1), RenderingConfiguration.greyColor, false);
            }
        }
    }

    @Override
    public <Anchor> void drawFull(MusicCanvas<Anchor> canvas, Stave stave) {
        Anchor startAnchor = canvas.getAnchor(musicalPosition.getPositionWithStave(stave));
        switch (beatWeight) {
            case 1 -> canvas.drawLine(startAnchor,0f,0.25f,0f,-4.25f,RenderingConfiguration.beatLineWidth, RenderingConfiguration.greyColor);
            case 2 -> canvas.drawLine(startAnchor,0f,0.25f,0f,-4.25f,RenderingConfiguration.subBeatLineWidth, RenderingConfiguration.greyColor);
            default -> canvas.drawLine(startAnchor,0f,0.25f,0f,-4.25f,RenderingConfiguration.subBeatLineWidth / (beatWeight + 1), RenderingConfiguration.greyColor);
        }
    }
}
