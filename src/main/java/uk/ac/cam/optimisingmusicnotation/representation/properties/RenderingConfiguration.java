package uk.ac.cam.optimisingmusicnotation.representation.properties;

import java.net.URISyntaxException;
import java.nio.file.Paths;

public class RenderingConfiguration {
    public static float subBeatLineWidth = 0.1f;
    public static float beatLineWidth = 0.201f;
    public static float barLineWidth = 0.4f;
    public static float stemWidth = 0.15f;
    public static boolean upwardStems = true;
    public static float gapHeight = 0.125f;
    public static float artisticWhitespaceWidth = 1f; // A crochet of whitespace + dead space
    public static float pulseLineHeight = 5f;
    public static String fontFilePath;
    public static float ledgerLineWidth = 1.5f;
    public static float hookRatio = 0.5f;
    public static float flagRatio = 0.5f;
    public static float beamletRatio = 0.5f;
    public static boolean allHooked = false;
    public static boolean allFlagged = false;
    public static boolean beamlets = true;
    public static int beamletLimit = 0;
    public static float staveLineWidth = 0.1f;

    static {
        try {
            fontFilePath = Paths.get(RenderingConfiguration.class.getResource("/fonts/Roboto-Regular.ttf").toURI()).toString();
            imgFilePath = Paths.get(RenderingConfiguration.class.getResource("/img").toURI()).toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static String imgFilePath;
    public static float beamWidth = 0.5f;
    public static float gapBetweenBeams = 0.25f;
    public static float beamOffset = 0.25f;
}
