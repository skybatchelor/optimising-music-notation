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
    public enum NoneThisNext {
        NONE, THIS , NEXT
    }


    // Pulse line information
    public static float subBeatLineWidth = 0.1f;
    public static float beatLineWidth = 0.201f;
    public static float barLineWidth = 0.4f;
    public static float pulseLineHeight = 5f;

    // Stave information
    public static float staveLineWidth = 0.1f;

    // Note information
    public static float stemWidth = 0.15f;
    public static boolean upwardStems = false;
    public static float stemLength = 3f;
    public static float gapHeight = 0.125f;
    public static float ledgerLineWidth = 1.5f;
    public static float dotRadius = 0.2f;
    public static float dotSpacing = 0.2f;
    public static float noteheadRadius = 0.5f;

    // Capital information
    public static NoneThisNext newSectionAddsCapital = NoneThisNext.NEXT;
    public static NoneThisNext newlineAddsCapital = NoneThisNext.NONE;
    public static NoneThisNext artisticWhitespaceAddsCapital = NoneThisNext.THIS;
    public static float capitalScaleFactor = 1.333f;

    // Beaming information
    public static float hookRatio = 0.5f;
    public static boolean hookSingleLeft = false;
    public static boolean hookAllLeft = false;
    public static boolean hookSingleRight = true;
    public static boolean hookAllRight = false;
    public static boolean hookStart = true;
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
}
