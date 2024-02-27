package uk.ac.cam.optimisingmusicnotation.representation.properties;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.HashMap;

public class RenderingConfiguration {

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

    // Beaming information
    public static float hookRatio = 0.5f;
    public static float flagRatio = 0.5f;
    public static float beamletRatio = 0.5f;
    public static boolean allHooked = false;
    public static boolean allFlagged = false;
    public static boolean beamlets = true;
    public static int beamletLimit = 0;
    public static float beamWidth = 0.5f;
    public static float gapBetweenBeams = 0.25f;
    public static float beamOffset = -0f;

    // Whitespace information
    public static float artisticWhitespaceWidth = 1f; // A crochet of whitespace + dead space

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
    public static float dynamicsTextHeight = 6f;

    // Tempo marking information

    public static float tempoNoteScale = 0.5f;
    public static float tempoNoteTimeScale = 1f;

    static {
        try {
            HashMap<String, String> env = new HashMap<>();
            String[] fontPaths = RenderingConfiguration.class.getResource("/fonts").toURI().toString().split("!");
            if (fontPaths.length == 1) {
                fontFilePath = Paths.get(URI.create(fontPaths[0])).toString();
                defaultFontFilePath = fontFilePath + "/Roboto-Regular.ttf";
                dynamicsFontFilePath = fontFilePath + "/Century_Condensed_Bold_Italic.ttf";
                imgFilePath = Paths.get(RenderingConfiguration.class.getResource("/img").toURI()).toString();
            }
            else {
                try (FileSystem fs = FileSystems.newFileSystem(URI.create(fontPaths[0]), env)) {
                    fontFilePath = fs.getPath(fontPaths[1]).toString();
                    defaultFontFilePath = fontFilePath + "/Roboto-Regular.ttf";
                    dynamicsFontFilePath = fontFilePath + "/Century_Condensed_Bold_Italic.ttf";
                    imgFilePath = fs.getPath(RenderingConfiguration.class.getResource("/img").toURI().toString().split("!")[1]).toString();
                }
            }
        } catch (URISyntaxException | NullPointerException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
