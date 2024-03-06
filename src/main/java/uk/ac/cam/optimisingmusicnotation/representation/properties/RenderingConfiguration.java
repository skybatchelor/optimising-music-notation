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
    public static float beamWidth = 0.5f;
    public static float gapBetweenBeams = 0.25f;
    public static float beamOffset = -0f;

    // Flag information
    public static float flagRatio = 0.5f;
    public static boolean allFlaggedLeft = false;
    public static boolean singleFlaggedLeft = true;
    public static boolean allFlaggedRight = false;
    public static boolean singleFlaggedRight = false;

    // Beamlet information
    public static float beamletRatio = 0.25f;
    public static boolean beamletLeft = false;
    public static boolean singleBeamletLeft = false;
    public static boolean beamletRight = true;
    public static boolean singleBeamletRight = true;
    public static int beamletLimit = 0;

    // Whitespace information
    public static float artisticWhitespaceWidth = 0.4f; // A crotchet of whitespace + dead space

    // Text information
    public static String defaultFontFilePath;
    public static String dynamicsFontFilePath;
    public static String fontFilePath;

    // Image information
    public static String imgFilePath;

    // Dynamics information
    public static float dynamicsOffset = -3f;
    public static float hairpinHeight = 1.5f;
    public static float hairpinInset = 1.5f;
    public static float hairpinLineWidth = 0.1f;
    public static float dynamicsTextHeight = 6f;

    // Tuplet information
    public static float tupletOffset = 1.5f;
    public static float tupletEndHook = 1f;
    public static float tupletNumHeight = 5f;
    public static float tupletLineWidth = 0.1f;
    public static float tupletOverHook = 0f;
    public static boolean tupletFillPeriod = false;

    // Tempo marking information
    public static float tempoNoteScale = 0.5f;
    public static float tempoNoteSpacing = 1.5f;
    public static float tempoNoteTimeScale = 2.5f;

    // Stave text information
    public static float staveTextHeight = 5f;

    // Stave image information
    public static float verticalMargin = 0.25f;
    public static float horizontalMargin = 0.25f;

    // Coda and segno information
    public static float signWidth = 4f;
    public static float signHeight = 4f;
    public static float signOffset = 5f;

    // Line information
    public static float postLineHeight = 5f;

    // Section information
    public static float postSectionHeight = 5f;

    // Color information
    public static Color greyColor = new Color(0xCCCCCC);
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
