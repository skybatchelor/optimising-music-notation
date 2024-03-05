package uk.ac.cam.optimisingmusicnotation.representation.properties;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.rendering.TextAlignment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TimeSignature {
    public record BeatTuple(int durationInUnits, int beatType, int subBeats) {

    }

    private int beatNum;
    private int beatType;

    public List<BeatTuple> getBeatPattern() {
        return beatPattern;
    }

    public void setBeatPatternToDefault() {
        this.beatPattern = defaultBeatPatterns(this.beatNum, this.beatType);
    }

    public void setBeatPattern(List<BeatTuple> beatPattern) {
        this.beatPattern = beatPattern;
    }

    private List<BeatTuple> beatPattern;

    public int getBeatNum() {
        return beatNum;
    }
    public int getBeatType() {
        return beatType;
    }

    public TimeSignature() {
        beatNum = 0;
        beatType = 0;
        beatPattern = new ArrayList<>();
    }

    public TimeSignature(int beatNum, int beatType) {
        this.beatNum = beatNum;
        this.beatType = beatType;
        this.beatPattern = defaultBeatPatterns(beatNum, beatType);
    }

    public List<BeatTuple> defaultBeatPatterns(int beatNum, int beatType) {
        switch (beatNum) {
            case 5 -> {
                return new ArrayList<>() {{ add(new BeatTuple(3, beatType, 3));
                    add(new BeatTuple(2, beatType, 2)); }};
            }
            case 6 -> {
                return new ArrayList<>() {{ add(new BeatTuple(3, beatType, 1));
                    add(new BeatTuple(3, beatType, 1)); }};
            }
            case 7 -> {
                return new ArrayList<>() {{ add(new BeatTuple(2, beatType, 2));
                    add(new BeatTuple(2, beatType, 2));
                    add(new BeatTuple(3, beatType, 3)); }};
            }
            case 9 -> {
                return new ArrayList<>() {{ add(new BeatTuple(3, beatType, 1));
                    add(new BeatTuple(3, beatType, 1));
                    add(new BeatTuple(3, beatType, 1)); }};
            }
            case 12 -> {
                return new ArrayList<>() {{ add(new BeatTuple(3, beatType, 1));
                    add(new BeatTuple(3, beatType, 1));
                    add(new BeatTuple(3, beatType, 1));
                    add(new BeatTuple(3, beatType, 1)); }};
            }
            default -> {
                var res = new ArrayList<BeatTuple>();
                for (int i = 0; i < beatNum; ++i) {
                    res.add(new BeatTuple(1, beatType, 1));
                }
                return res;
            }
        }
    }

    public void setBeatNum(int beatNum) { this.beatNum = beatNum; }

    public void setBeatType(int beatType) { this.beatType = beatType; }

    private int maxDigits(){
        return Math.max(String.valueOf(beatNum).length(), String.valueOf(beatType).length());
    }

    public <Anchor> void draw(MusicCanvas<Anchor> canvas, MusicalPosition musicalPosition) {
        Anchor anchor = canvas.getAnchor(musicalPosition);
        float width = maxDigits() * 1.5f;
        try {
            //canvas.drawLine(anchor,-0.5f,7.5f,-0.5f,2f,3.5f);
            canvas.drawWhitespace(anchor,-0.5f,7.5f,width,4.5f);
            canvas.drawText(RenderingConfiguration.dynamicsFontFilePath,Integer.toString(beatNum),10f, TextAlignment.LEFT,
                    anchor,-width/2,9.5f,width,6f);
            canvas.drawText(RenderingConfiguration.dynamicsFontFilePath,Integer.toString(beatType),10f, TextAlignment.LEFT,
                    anchor,-width/2,7.5f,width,6f);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean equals(TimeSignature timeSig) {
        return (timeSig != null && this.beatNum == timeSig.getBeatNum() && this.beatType == timeSig.getBeatType());
    }
}
