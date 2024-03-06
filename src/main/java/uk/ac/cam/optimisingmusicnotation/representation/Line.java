package uk.ac.cam.optimisingmusicnotation.representation;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.beatlines.BarLine;
import uk.ac.cam.optimisingmusicnotation.representation.beatlines.PulseLine;
import uk.ac.cam.optimisingmusicnotation.representation.properties.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a line of music, which might have multiple lines.
 * In general, any two points with the same horizontal position in a line should occur at the same time.
 */
public class Line {
  
    public List<Stave> getStaves() {
        return staves;
    }

    private final List<Stave> staves;
    public List<PulseLine> getPulseLines() {
        return pulseLines;
    }


    public void addPulseLine(PulseLine pulseLine) {
        pulseLines.add(pulseLine);
    }

    private final List<PulseLine> pulseLines;

    public int getLineNumber() {
        return lineNumber;
    }

    private final int lineNumber;

    public float getEndInCrotchets() { return lengthInCrotchets + offsetInCrotchets; }

    public float getLengthInCrotchets() {
        return lengthInCrotchets;
    }
  
    private final float lengthInCrotchets;

    public float getOffsetInCrotchets() {
        return offsetInCrotchets;
    }

    private final float offsetInCrotchets;

    public float getStartTimeInCrotchets() {
        return startTimeInCrotchets;
    }

    private final float startTimeInCrotchets;
  
    private final boolean extendPulseLinesUp;
    private final boolean extendPulseLinesDown;

    public Line(List<Stave> staves, float startTimeInCrotchets, float lengthInCrotchets, float offsetInCrotchets,
                int lineNumber, boolean extendPulseLinesUp, boolean extendPulseLinesDown) {
        this.staves = staves;
        this.lineNumber = lineNumber;
        this.startTimeInCrotchets = startTimeInCrotchets;
        this.lengthInCrotchets = lengthInCrotchets;
        this.offsetInCrotchets = offsetInCrotchets;
        this.extendPulseLinesUp = extendPulseLinesUp;
        this.extendPulseLinesDown = extendPulseLinesDown;
        pulseLines = new ArrayList<>();
    }

    /**
     * Draws a line on a given canvas.
     * @param canvas the canvas rendering the score
     * @param <Anchor> the anchor type used by the canvas
     */
    public <Anchor> void draw(MusicCanvas<Anchor> canvas) {
        canvas.addFirstStave(offsetInCrotchets, staves.size());
        for (int i = 0; i < staves.size(); ++i) {
            Stave s = staves.get(i);
            if (i != 0) {
                canvas.addStave(offsetInCrotchets);
            }
            for (PulseLine p: pulseLines) {
                p.drawAroundStave(canvas, s, extendPulseLinesUp || i > 0,
                        extendPulseLinesDown || i < staves.size() - 1,
                        i < staves.size() - 1 ? 5f : 10f,
                        i == 0);
            }
            s.draw(canvas,this);
        }
        drawTimeSignatures(canvas);
        canvas.reserveHeight(RenderingConfiguration.postLineHeight);
    }

    /**
     * Draws a line, with a given set of {@link Clef}s, and a given {@link KeySignature}.
     * Used to draw a line at the start of a section.
     * @param canvas the canvas rendering the score
     * @param clefs the list of clefs for each stave
     * @param keySignature the key signature to draw
     * @param <Anchor> the anchor type used by the canvas
     */
    public <Anchor> void drawWithClefAndKeySig(MusicCanvas<Anchor> canvas, List<Clef> clefs, KeySignature keySignature) {
        canvas.addFirstStave(offsetInCrotchets, staves.size());
        for (int i = 0; i < staves.size(); ++i) {
            Stave s = staves.get(i);
            if (i != 0) {
                canvas.addStave(offsetInCrotchets);
            }
            for (PulseLine p: pulseLines) {
                p.drawAroundStave(canvas, s, extendPulseLinesUp || i > 0,
                        extendPulseLinesDown || i < staves.size() - 1,
                        i < staves.size() - 1 ? 5f : 10f,
                        i == 0);
            }
            s.drawWithClefAndKeySig(canvas,this, clefs.get(i), keySignature);
        }
        drawTimeSignatures(canvas);
        canvas.reserveHeight(RenderingConfiguration.postLineHeight);
    }

    /**
     * Draws all the time signatures on all the bar lines.
     * @param canvas the canvas used for rendering the score
     * @param <Anchor> the anchor type used by the canvas
     */
    private <Anchor> void drawTimeSignatures(MusicCanvas<Anchor> canvas) {
        TimeSignature lastTimeSig = null;
        TimeSignature currentTimeSig;
        for (PulseLine p: pulseLines) {
            if (p instanceof BarLine){
                currentTimeSig = ((BarLine) p).getTimeSignature();
                if (currentTimeSig != null && !(currentTimeSig.equals(lastTimeSig))){
                    lastTimeSig = ((BarLine) p).getTimeSignature();
                    lastTimeSig.draw(canvas, p.getMusicalPosition().getPositionWithStave(staves.get(0)));
                }
            }
        }
    }
}