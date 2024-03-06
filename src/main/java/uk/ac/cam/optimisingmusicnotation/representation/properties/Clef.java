package uk.ac.cam.optimisingmusicnotation.representation.properties;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.Stave;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A musical clef.
 */
public class Clef {
    /**
     * Gets the sign of the clef
     * @return sign the sign of the clef
     */
    public ClefSign getSign() {
        return sign;
    }
    private final ClefSign sign;
    /**
     * Gets the line the clef is drawn on
     * The lineDistanceFromBottomOfClef property of ClefSign
     * indicates which part of the clef goes on this line.
     * @return line the line that the clef is drawn on
     */
    public int getLine() {
        return line;
    }

    private final int line;
    /**
     * Gets the number of octaves below or above the conventional octave the clef is.
     * Default 0.
     * @return octaveChange
     */
    public int getOctaveChange() {
        return octaveChange;
    }

    private final int octaveChange;

    /**
     * Make a clef with the given sign, with default positioning.
     * @param sign the sign of the clef to make
     */
    public Clef(ClefSign sign){
        this.sign = sign;
        this.line = sign.defaultLinesFromBottomOfStave;
        this.octaveChange = 0;
    }

    /**
     * Make a clef with the given sign which will be drawn on the given line.
     * @param sign the sign of the clef to make
     * @param line the line to draw the clef on
     */
    public Clef(ClefSign sign, int line){
        this.sign = sign;
        this.line = line;
        this.octaveChange = 0;
    }

    /**
     * Make a clef with the given sign which will be drawn on the given line,
     * changed by the given octaves.
     * @param sign the sign of the clef to make
     * @param line the line to draw the clef on
     * @param octaveChange the number of octaves below or above the default octave for the clef
     */
    public Clef(ClefSign sign, int line, int octaveChange) {
        this.sign = sign;
        this.line = line;
        this.octaveChange = octaveChange;
    }

    /**
     * Draw a clef
     * @param canvas the canvas rendering the score
     * @param line the line to draw the clef at the start of
     * @param stave the stave to draw the clef on
     * @param numAlterations the number of alterations in the key signature
     *                       being drawn next to the clef
     * @param <Anchor> the anchor type used by the canvas
     */
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Line line, Stave stave, int numAlterations){
        Anchor anchor = canvas.getLineStartAnchor(new MusicalPosition(line, stave, 0));
        String clefPath = RenderingConfiguration.imgFilePath + "/clefs/" + this.toString().toLowerCase() + ".svg";
        float topLeftY = (this.line/2f) + ((sign.height - sign.lineDistanceFromBottomOfClef)-4);
        try{
            canvas.drawImage(clefPath, anchor,-(6f+numAlterations) ,topLeftY,0f,this.sign.height);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the sign name to a string
     * @return the sign name as a string
     */
    @Override
    public String toString() {
        return sign.toString();
    }

    /**
     * Convert a pitch name to a list of pitches between the bottom line of the stave
     * and 3 spaces above the stave
     * @param name the name of the pitch to convert
     * @return a list of pitches with the given pitch name
     */
    public List<Pitch> pitchNameToPitches(PitchName name){
        List<Pitch> retList = new ArrayList<>();
        List<PitchName> pitches = Arrays.stream(PitchName.values()).toList();
        int bottomNameI = pitches.indexOf(getBottomLinePitch());
        int nameI = pitches.indexOf(name);
        int spacesAbove = Math.floorMod((nameI - bottomNameI) , pitches.size());
        retList.add(new Pitch(spacesAbove,0, 0));
        if (spacesAbove < 3){
            retList.add(new Pitch(spacesAbove + 7,0, 0));
        }
        return retList;
    }

    private PitchName getBottomLinePitch(){
        int diff = line - sign.defaultLinesFromBottomOfStave;
        List<PitchName> pitches = Arrays.stream(PitchName.values()).toList();
        int numPitches = pitches.size();
        switch (sign){
            case G -> {
                return pitches.get((pitches.indexOf(PitchName.E)+diff) % numPitches);
            }
            case F -> {
                return pitches.get((pitches.indexOf(PitchName.G)+diff) % numPitches);
            }
            case C -> {
                return pitches.get((pitches.indexOf(PitchName.F)+diff) % numPitches);
            }
            default -> {
                return null;
            }
        }
    }
}
