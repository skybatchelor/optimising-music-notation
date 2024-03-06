package uk.ac.cam.optimisingmusicnotation.representation.properties;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;

/**
 * A class to hold a variety of configuration variables to control how elements of the score render.
 */
public class RenderingConfiguration {
    /**
     * Represents how tags might affect other settings. Primarily used for how different elements capitalise notes.
     */
    public enum NoneThisNext {
        NONE, THIS , NEXT
    }


    // Pulse line information
    /** The width of sub beat / offbeat pulse lines */
    public static float subBeatLineWidth = 0.1f;
    /** The width of beat pulse lines */
    public static float beatLineWidth = 0.201f;
    /** The width of bar lines */
    public static float barLineWidth = 0.4f;
    /** How high above a stave non-extended pulse lines are drawn */
    public static float pulseLineHeight = 5f;

    // Stave information
    /** The line width for the stave lines */
    public static float staveLineWidth = 0.1f;

    // Note information
    /** How wide stems are drawn */
    public static float stemWidth = 0.15f;
    /** Whether stems are drawn upwards or not. Changed by each part */
    public static boolean upwardStems = false;
    /** The length of stems */
    public static float stemLength = 3f;
    /** The height of the space between a note and the pulse lines */
    public static float gapHeight = 0.125f;
    /** How wide ledger lines are */
    public static float ledgerLineWidth = 1.5f;
    /** The radius of dots used for dotted notes */
    public static float dotRadius = 0.2f;
    /** The space between dots used for dotted notes */
    public static float dotSpacing = 0.2f;
    /** The base radius of a notehead */
    public static float noteheadRadius = 0.5f;

    // Capital information
    /** Whether a new section does not capitalise a note, capitalises the note below it, or capitalises the next available note */
    public static NoneThisNext newSectionAddsCapital = NoneThisNext.NEXT;
    /** Whether a newline does not capitalise a note, capitalises the note below it, or capitalises the next available note */
    public static NoneThisNext newlineAddsCapital = NoneThisNext.NONE;
    /** Whether a breath whitespace does not capitalise a note, capitalises the note below it, or capitalises the next available note */
    public static NoneThisNext artisticWhitespaceAddsCapital = NoneThisNext.THIS;
    /** The factor by which capitalised notes are made larger */
    public static float capitalScaleFactor = 1.333f;

    // Beaming information
    /** What proportion of the duration of the note are filled by hooks in beams */
    public static float hookRatio = 0.5f;
    /** Whether a single hook should hook left */
    public static boolean hookSingleLeft = false;
    /** Whether all secondary beams should hook left */
    public static boolean hookAllLeft = false;
    /** Whether a single hook should hook right */
    public static boolean hookSingleRight = true;
    /** Whether all secondary beams should hook right */
    public static boolean hookAllRight = false;
    /** Whether the chord at the start of a beam group should be hooked. If so, it is hooked right. Primarily used in conjunction with flag settings */
    public static boolean hookStart = true;
    /** Whether the chord at the end of a beam group should be hooked. If so, it is hooked left. Primarily used in conjunction with flag settings */
    public static boolean hookEnd = true;
    /** The line width of beams */
    public static float beamWidth = 0.5f;
    /** The gap between two beams vertically */
    public static float gapBetweenBeams = 0.25f;
    /** A height offset from the stem end for the beam. */
    public static float beamOffset = -0f;

    // Flag information
    /** The proportion of the duration of the note the flag occupies */
    public static float flagRatio = 0.5f;
    /** Whether beam groups have a flag to the left */
    public static boolean allFlaggedLeft = false;
    /** Whether single flagged notes have a flag to the left */
    public static boolean singleFlaggedLeft = true;
    /** Whether beam groups have a flag to the right */
    public static boolean allFlaggedRight = false;
    /** Whether single flagged notes have a flag to the right */
    public static boolean singleFlaggedRight = false;

    // Beamlet information
    /** The proportion of the duration of the note the beamlet occupies */
    public static float beamletRatio = 0.25f;
    /** Whether beam groups have a beamlet to the left */
    public static boolean beamletLeft = false;
    /** Whether single flagged notes have a beamlet to the left */
    public static boolean singleBeamletLeft = false;
    /** Whether beam groups have a beamlet to the right */
    public static boolean beamletRight = true;
    /** Whether single flagged notes have a beamlet to the right */
    public static boolean singleBeamletRight = true;
    /** How many beams a beamlet can draw. Will usually be either 0 (1 beam), or 10 (all beams) */
    public static int beamletLimit = 0;

    // Whitespace information
    /** The crotchet duration of artistic whitespace */
    public static float artisticWhitespaceWidth = 0.4f; // A crotchet of whitespace + dead space

    // Text information
    /** The file path of the default font */
    public static String defaultFontFilePath;
    /** The file path of the font for dynamic markings */
    public static String dynamicsFontFilePath;
    /** The base file path for fonts */
    public static String fontFilePath;

    // Image information
    /** The base file path for images */
    public static String imgFilePath;

    // Dynamics information
    /** The vertical offset below the lowest anchor that the centre of dynamic markings are put at */
    public static float dynamicsOffset = -3f;
    /** The height of a hairpin */
    public static float hairpinHeight = 1.5f;
    /** The horizontal inset of a hairpin from both ends */
    public static float hairpinInset = 1.5f;
    /** The line width used to draw hairpins */
    public static float hairpinLineWidth = 0.1f;
    /** How tall dynamic text markings should be */
    public static float dynamicsTextHeight = 6f;

    // Tuplet information
    /** Hwo far above/below the notes/stave to draw the tuple bracket */
    public static float tupletOffset = 1.5f;
    /** How long the end of the tuplet bracket is */
    public static float tupletEndHook = 1f;
    /** The height of the number in a tuplet */
    public static float tupletNumHeight = 5f;
    /** The line width used to draw the tuplet bracket */
    public static float tupletLineWidth = 0.1f;
    /** How far the tuplet should extend past the end and start time */
    public static float tupletOverHook = 0f;
    /** Whether tuplets should end at the last note, or the end of the period they effect */
    public static boolean tupletFillPeriod = false;

    // Tempo marking information
    /** The scale of notes used in tempo markings */
    public static float tempoNoteScale = 0.5f;
    /** The spacing between the note and the rest of the tempo marking */
    public static float tempoNoteSpacing = 1.5f;
    /** The timescale that tempo notes use */
    public static float tempoNoteTimeScale = 2.5f;

    // Stave text information
    /** The height of stave text */
    public static float staveTextHeight = 5f;

    // Stave image information
    /** How much vertical whitespace should be drawn around images with whitespace backings */
    public static float verticalMargin = 0.25f;
    /** How much horizontal whitespace should be drawn around images with whitespace backings */
    public static float horizontalMargin = 0.25f;

    // Coda and segno information
    /** How wide the signs are */
    public static float signWidth = 4f;
    /** How tall the signs are */
    public static float signHeight = 4f;
    /** How far above the highest point on the stave the sigs are */
    public static float signOffset = 5f;

    // Line information
    /** How much space is given after a line */
    public static float postLineHeight = 5f;

    // Section information
    /** How much space is given after a section */
    public static float postSectionHeight = 5f;

    // Color information
    /** The light grey used by pulse lines */
    public static Color greyColor = new Color(0xCCCCCC);
    /** The black used by the title and composer */
    public static Color blackColor = new Color(0x000000);

    static {
        try {
            HashMap<String, String> env = new HashMap<>();
            String[] fontPaths = Objects.requireNonNull(RenderingConfiguration.class.getResource("/fonts")).toURI().toString().split("!");
            if (fontPaths.length == 1) {
                fontFilePath = Paths.get(URI.create(fontPaths[0])).toString();
                defaultFontFilePath = fontFilePath + "/Roboto-Regular.ttf";
                dynamicsFontFilePath = fontFilePath + "/Century_Condensed_Bold_Italic.ttf";
                imgFilePath = Paths.get(Objects.requireNonNull(RenderingConfiguration.class.getResource("/img")).toURI()).toString();
            }
            else {
                try (FileSystem fs = FileSystems.newFileSystem(URI.create(fontPaths[0]), env)) {
                    fontFilePath = fs.getPath(fontPaths[1]).toString();
                    defaultFontFilePath = fontFilePath + "/Roboto-Regular.ttf";
                    dynamicsFontFilePath = fontFilePath + "/Century_Condensed_Bold_Italic.ttf";
                    imgFilePath = fs.getPath(Objects.requireNonNull(RenderingConfiguration.class.getResource("/img")).toURI().toString().split("!")[1]).toString();
                }
            }
        } catch (URISyntaxException | NullPointerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private RenderingConfiguration() {}
}
