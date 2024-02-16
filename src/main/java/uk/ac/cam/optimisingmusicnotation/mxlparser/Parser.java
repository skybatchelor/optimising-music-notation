package uk.ac.cam.optimisingmusicnotation.mxlparser;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import org.audiveris.proxymusic.*;
import org.audiveris.proxymusic.util.Marshalling;
import uk.ac.cam.optimisingmusicnotation.rendering.PdfMusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.Piece;
import uk.ac.cam.optimisingmusicnotation.representation.Section;
import uk.ac.cam.optimisingmusicnotation.representation.Stave;
import uk.ac.cam.optimisingmusicnotation.representation.beatlines.BarLine;
import uk.ac.cam.optimisingmusicnotation.representation.beatlines.BeatLine;
import uk.ac.cam.optimisingmusicnotation.representation.beatlines.PulseLine;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.BeamGroup;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.StaveElement;
import uk.ac.cam.optimisingmusicnotation.representation.whitespaces.Rest;
import uk.ac.cam.optimisingmusicnotation.representation.whitespaces.Whitespace;

import javax.xml.bind.JAXBElement;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.String;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Parser {
    private static class ChordTuple {
        List<Note> notes;
        float crotchets;
        float duration;
        int lowestLine;

        public ChordTuple(float crochets, int lowestLine) {
            notes = new ArrayList<>();
            this.crotchets = crochets;
            this.lowestLine = lowestLine;
        }
    }

    private static class InstantiatedChordTuple {
        List<uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch> pitches;
        List<uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental> accidentals;
        float crotchetsIntoLine;
        float duration;
        uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType noteType;
        int dots;

        public InstantiatedChordTuple(List<uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch> pitches, List<uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental> accidentals,
                                      float crotchetsIntoLine, float duration, uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType noteType, int dots) {
            this.pitches = pitches;
            this.accidentals = accidentals;
            this.crotchetsIntoLine = crotchetsIntoLine;
            this.duration = duration;
            this.noteType = noteType;
            this.dots = dots;
        }
    }

    private static class BeamTuple {
        int start;
        int end;
        int number;

        public BeamTuple(int start, int end, int number) {
            this.start = start; this.end = end; this.number = number;
        }
    }

    private static class BeamGroupTuple {
        List<ChordTuple> chords;


        public BeamGroupTuple() {
            chords = new ArrayList<>();
        }
    }

    private static class InstantiatedBeamGroupTuple {
        List<InstantiatedChordTuple> chords;
        List<BeamTuple> beams;

        public InstantiatedBeamGroupTuple() { chords = new ArrayList<>(); beams = new ArrayList<>(); }
    }

    private static class RestTuple {
        float startTime;
        float endTime;

        public RestTuple(float startTime, float endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    private static class PulseLineTuple {
        float time;
        String name;
        int beatWeight;

        PulseLineTuple(float time, String name, int beatWeight) {
            this.time = time;
            this.name = name;
            this.beatWeight = beatWeight;
        }
    }

    private static class InstantiatedPulseLineTuple {
        float timeInLine;
        String name;
        int beatWeight;

        InstantiatedPulseLineTuple(float timeInLine, String name, int beatWeight) {
            this.timeInLine = timeInLine;
            this.name = name;
            this.beatWeight = beatWeight;
        }
    }

    private static class ParsingPartTuple {
        List<BeamGroupTuple> beamGroups;
        List<PulseLineTuple> pulseLines;
        TreeMap<Float, uk.ac.cam.optimisingmusicnotation.representation.properties.Clef> clefs;

        public ParsingPartTuple() {
            beamGroups = new ArrayList<>();
            pulseLines = new ArrayList<>();
            clefs = new TreeMap<>();
        }
    }

    private static class LineTuple {
        float startTime;
        List<RestTuple> rests;
        List<InstantiatedPulseLineTuple> pulses;
        List<InstantiatedBeamGroupTuple> notes;

        LineTuple(float startTime) {
            this.startTime = startTime;
            rests = new ArrayList<>();
            pulses = new ArrayList<>();
            notes = new ArrayList<>();
        }
    }

    private static class InstantiatedLineTuple {
        float startTime;
        Line line;

        InstantiatedLineTuple(float startTime, Line line) {
            this.startTime = startTime;
            this.line = line;
        }
    }

    public static Score parseToScore(Object mxl) {
        if (mxl instanceof ScorePartwise partwise) {
            TreeMap<Float, ChordTuple> chords = new TreeMap<>();
            TreeMap<Float, Direction> directions = new TreeMap<>();
            TreeMap<Float, Float> newlines = new TreeMap<>() {{ put(0f, 0f); }};
            TreeSet<Float> newSections = new TreeSet<>() {{ add(0f); }};
            TreeMap<String, Part> parts = new TreeMap<>();

            TreeMap<String, TreeMap<Float, uk.ac.cam.optimisingmusicnotation.representation.properties.Clef>> clefs =
                    new TreeMap<>();

            List<Object> scoreParts = partwise.getPartList().getPartGroupOrScorePart();
            for (Object part : scoreParts) {
                if (part instanceof ScorePart) {
                    ScorePart scorePart = (ScorePart) part;
                    Part ret = new Part();
                    parts.put(scorePart.getId(), ret);
                    ret.setName(scorePart.getPartName().getValue());
                    ret.setAbbreviation(scorePart.getPartAbbreviation().getValue());
                } else if (part instanceof PartGroup) {
                    PartGroup partGroup = (PartGroup) part;
                } else {

                }
            }

            float totalLength = 0;

            TreeMap<String, ParsingPartTuple> parsingParts = new TreeMap<>();
            TreeMap<String, List<LineTuple>> partLines = new TreeMap<>();

            TreeMap<String, List<TreeMap<Float, Line>>> partSections = new TreeMap<>();
            List<ScorePartwise.Part> musicParts = partwise.getPart();
            for (ScorePartwise.Part part : musicParts) {
                String partId = "";
                if (part.getId() instanceof ScorePart scorePart) {
                    partId = scorePart.getId();
                }
                ParsingPartTuple currentPart = new ParsingPartTuple();
                parsingParts.put(partId, currentPart);
                partLines.put(partId, new ArrayList<>());
                partSections.put(partId, new ArrayList<>());
                float measureStartTime = 0;
                TimeSignature currentTimeSignature = new TimeSignature();
                int divisions = 0;
                float prevChange = 0;
                int lowestLineGrandStaveLine = 0;
                List<uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch> currentChordPitches = new ArrayList<>();
                List<uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental> currentChordAccidentals = new ArrayList<>();
                ChordTuple currentChord = new ChordTuple(0, 0);
                BeamGroupTuple beamGroup = new BeamGroupTuple();
                List<ScorePartwise.Part.Measure> measures = part.getMeasure();

                for (ScorePartwise.Part.Measure measure : measures) {
                    float measureTime = 0;
                    float measureLength = currentTimeSignature.beatNum * 4f / (currentTimeSignature.beatType);

                    for(Object component : measure.getNoteOrBackupOrForward()) {
                        if (component instanceof Attributes attributes) {
                            if (attributes.getTime() != null) {
                                for(Time time : attributes.getTime()) {
                                    var timeSignature = parseTimeSignature(time.getTimeSignature());
                                    if (timeSignature != null) {
                                        currentTimeSignature = timeSignature;
                                    }
                                }
                            }
                            if (attributes.getClef() != null) {
                                for(org.audiveris.proxymusic.Clef clef : attributes.getClef()) {
                                    uk.ac.cam.optimisingmusicnotation.representation.properties.Clef parsed = parseClef(clef);
                                    lowestLineGrandStaveLine = clefToLowestLineGrandStaveLine(parsed);
                                    currentChord.lowestLine = lowestLineGrandStaveLine;
                                    currentPart.clefs.put(measureStartTime + measureTime, parsed);
                                }
                            }
                            if (attributes.getDivisions() != null) {
                                divisions = attributes.getDivisions().intValue();
                            }
                            measureLength = currentTimeSignature.beatNum * 4f / (currentTimeSignature.beatType);
                        } else if (component instanceof Note note) {
                            if (note.getDuration() != null) {
                                prevChange = note.getDuration().intValue() / (float)divisions;
                            } else {
                                prevChange = 0;
                            }
                            if (note.getChord() == null) {
                                if (currentChord.notes.size() != 0) {
                                    chords.put(currentChord.crotchets, currentChord);
                                }
                                currentChord = new ChordTuple(measureStartTime + measureTime, lowestLineGrandStaveLine);
                                measureTime += prevChange;
                            }
                            currentChord.notes.add(note);
                            if (note.getDuration() != null) {
                                currentChord.duration = note.getDuration().intValue() / (float)divisions;
                            }
                            boolean addedToBeamGroup = false;
                            if (note.getBeam() != null) {
                                for(Beam beam : note.getBeam()) {
                                    if (beam.getNumber() == 1) {
                                        switch (beam.getValue()) {
                                            case BEGIN:
                                                beamGroup.chords.add(currentChord);
                                                addedToBeamGroup = true;
                                                break;
                                            case END:
                                                beamGroup.chords.add(currentChord);
                                                currentPart.beamGroups.add(beamGroup);
                                                beamGroup = new BeamGroupTuple();
                                                addedToBeamGroup = true;
                                                break;
                                            case CONTINUE:
                                                beamGroup.chords.add(currentChord);
                                                addedToBeamGroup = true;
                                                break;
                                            case FORWARD_HOOK:
                                                beamGroup.chords.add(currentChord);
                                                currentPart.beamGroups.add(beamGroup);
                                                beamGroup = new BeamGroupTuple();
                                                addedToBeamGroup = true;
                                                break;
                                            case BACKWARD_HOOK:
                                                beamGroup.chords.add(currentChord);
                                                currentPart.beamGroups.add(beamGroup);
                                                beamGroup = new BeamGroupTuple();
                                                addedToBeamGroup = true;
                                                break;
                                        }
                                    }
                                }
                            }
                            if (!addedToBeamGroup && note.getChord() == null) {
                                beamGroup.chords.add(currentChord);
                                currentPart.beamGroups.add(beamGroup);
                                beamGroup = new BeamGroupTuple();
                            }
                        } else if (component instanceof Backup backup) {
                            measureTime -= backup.getDuration().intValue() / (float) divisions;
                        } else if (component instanceof Direction direction) {
                            float offset = 0;
                            if (direction.getOffset() != null) {
                                offset = direction.getOffset().getValue().intValue() / (float) divisions;
                            }
                            if (isNewline(direction)) {
                                newlines.put(measureStartTime + measureTime + offset, measureTime + offset - measureLength);
                            }
                            if (isNewSection(direction)) {
                                newSections.add(measureStartTime + measureTime + offset);
                                newlines.put(measureStartTime + measureTime + offset, measureTime + offset - measureLength);
                            }
                            directions.put(measureStartTime + measureTime + offset, direction);
                        }
                    }

                    addPulseLines(currentTimeSignature, measureStartTime, currentPart.pulseLines, measure.getText());

                    measureStartTime += measureLength;
                }
                totalLength = Math.max(measureStartTime, totalLength);
            }

            Map<Float, Integer> lineIndices = new HashMap<>();
            List<Float> lineLengths = new ArrayList<>();
            List<Float> lineOffsets = new ArrayList<>();
            int index = 0;
            float prevLineStart = 0;
            for (Float newline : newlines.keySet()) {
                lineIndices.put(newline, index);
                lineOffsets.add(newlines.get(newline));
                if (index != 0) {
                    lineLengths.add(newline - prevLineStart);
                    prevLineStart = newline;
                }
                for (List<LineTuple> partList : partLines.values()) {
                    partList.add(new LineTuple(newline));
                }
                ++index;
            }
            lineLengths.add(totalLength - prevLineStart);

            Map<Float, Integer> sectionIndices = new HashMap<>();
            index = 0;
            for (Float newSection : newSections) {
                sectionIndices.put(newSection, index);
                for (List<TreeMap<Float, Line>> lineList : partSections.values()) {
                    lineList.add(new TreeMap<>());
                }
                ++index;
            }

            for (Map.Entry<String, ParsingPartTuple> part : parsingParts.entrySet()) {
                for (BeamGroupTuple beam : part.getValue().beamGroups) {
                    float lineStart = newlines.floorKey(beam.chords.get(0).crotchets);
                    int lineNum = lineIndices.get(lineStart);
                    if (isRest(beam)) {
                        partLines.get(part.getKey()).get(lineNum).rests.add(beamTupleToRestTuple(beam, lineStart));
                    } else {
                        partLines.get(part.getKey()).get(lineNum).notes.add(beamTupleToInstantiatedBeamTuple(beam, lineStart, lineNum));
                    }
                }
            }

            for (Map.Entry<String, ParsingPartTuple> part : parsingParts.entrySet()) {
                for (PulseLineTuple pulseLine : part.getValue().pulseLines) {
                    float lineStart = newlines.floorKey(pulseLine.time);
                    int lineNum = lineIndices.get(lineStart);
                    partLines.get(part.getKey()).get(lineNum).pulses.add(pulseTupleToInstantiatedPulseTuple(pulseLine, lineStart, lineNum));
                }
            }
            TreeMap<String, List<InstantiatedLineTuple>> finalLines = new TreeMap<>();
            List<Float> newlinesList = newlines.keySet().stream().toList();


            for (Map.Entry<String, List<LineTuple>> part : partLines.entrySet()) {
                finalLines.put(part.getKey(), new ArrayList<>());
                for (int i = 0; i < part.getValue().size(); ++i) {
                    List<StaveElement> elements = new ArrayList<>();
                    Stave stave = new Stave(elements, new ArrayList<>());

                    Line tempLine = new Line(new ArrayList<>() {{ add(stave); }}, lineLengths.get(i), lineOffsets.get(i), i);
                    finalLines.get(part.getKey()).add(new InstantiatedLineTuple(newlinesList.get(i), tempLine));

                    for (RestTuple restTuple : part.getValue().get(i).rests) {
                        tempLine.getStaves().get(0).addWhiteSpace(restTupleToRest(restTuple, tempLine));
                    }

                    for (InstantiatedBeamGroupTuple beamTuple : part.getValue().get(i).notes) {
                        tempLine.getStaves().get(0).addStaveElement(instantiatedBeamTupleToBeamGroup(beamTuple, tempLine));
                    }

                    for (InstantiatedPulseLineTuple pulseTuple : part.getValue().get(i).pulses) {
                        tempLine.addPulseLine(instantiatedPulseLineTupleToPulseLine(pulseTuple, tempLine));
                    }
                }
            }

            for (Map.Entry<String, List<InstantiatedLineTuple>> part : finalLines.entrySet()) {
                for (InstantiatedLineTuple instantiatedLineTuple : part.getValue()) {
                    float sectionStart = newSections.floor(instantiatedLineTuple.startTime);
                    int sectionNum = sectionIndices.get(sectionStart);
                    partSections.get(part.getKey()).get(sectionNum).put(instantiatedLineTuple.startTime, instantiatedLineTuple.line);
                }
            }

            TreeMap<String, List<Section>> finalSections = new TreeMap<>();

            for (Map.Entry<String, List<TreeMap<Float, Line>>> part : partSections.entrySet()) {
                List<Section> sections = new ArrayList<>();
                for (TreeMap<Float, Line> lines : part.getValue()) {
                    sections.add(new Section(lines.values().stream().toList(),
                            parsingParts.get(part.getKey()).clefs.floorEntry(lines.firstKey()).getValue()
                            ));
                }
                finalSections.put(part.getKey(), sections);
            }

            for (Map.Entry<String, List<Section>> part : finalSections.entrySet()) {
                parts.get(part.getKey()).sections = part.getValue();
            }

            return new Score(getWorkTitle(partwise), parts.values().stream().toList());
        }
        return null;
    }

    static String getWorkTitle(ScorePartwise score) {
        if (score.getWork() != null && score.getWork().getWorkTitle() != null) {
            return score.getWork().getWorkTitle();
        } else {
            return "";
        }
    }

    static boolean isRest(BeamGroupTuple tuple) {
        return (tuple.chords.get(0).notes.get(0).getRest() != null);
    }

    static InstantiatedBeamGroupTuple beamTupleToInstantiatedBeamTuple(BeamGroupTuple tuple, float lineTime, int lineNum) {
        InstantiatedBeamGroupTuple beamTuple = new InstantiatedBeamGroupTuple();
        for (ChordTuple chordTuple : tuple.chords) {
            beamTuple.chords.add(chordTupleToInstantiatedChordTuple(chordTuple, lineTime, lineNum));
        }

        Integer[] beaming = new Integer[10];
        Arrays.fill(beaming, -1);
        Integer[] beamStarts = new Integer[10];
        Arrays.fill(beamStarts, -1);
        for (int i = 0; i < beamTuple.chords.size(); ++i) {
            for (Note note : tuple.chords.get(i).notes) {
                for (Beam beam : note.getBeam()) {
                    if (beam.getNumber() != 1) {
                        switch (beam.getValue()) {
                            case FORWARD_HOOK:
                            case BACKWARD_HOOK:
                                if (beaming[beam.getNumber() - 2] == -1) {
                                    beamTuple.beams.add(new BeamTuple(i, i, beam.getNumber() - 1));
                                } else {
                                    System.out.println("Started hook beaming when beam had already started");
                                }
                                break;
                            case BEGIN:
                                if (beaming[beam.getNumber() - 2] == -1) {
                                    beamStarts[beam.getNumber() - 2] = i;
                                    beaming[beam.getNumber() - 2] = i;
                                } else {
                                    System.out.println("Started beam when beam had already started");
                                }
                                break;
                            case CONTINUE:
                                if (beaming[beam.getNumber() - 2] == i - 1) {
                                    beaming[beam.getNumber() - 2] = i;
                                } else {
                                    System.out.println("Continued invalid beam");
                                }
                                break;
                            case END:
                                if (beaming[beam.getNumber() - 2] == i - 1) {
                                    beaming[beam.getNumber() - 2] = -1;
                                    if (beamStarts[beam.getNumber() - 2] != -1) {
                                        beamTuple.beams.add(new BeamTuple(beamStarts[beam.getNumber() - 2], i, beam.getNumber() - 1));
                                    }
                                } else {
                                    System.out.println("Ended invalid beam");
                                }
                                break;
                        }
                    }
                }
            }
        }
        return beamTuple;
    }

    static boolean isBeamed(uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType noteType) {
        switch (noteType) {
            case MAXIMA, BREVE, SEMIBREVE, MINIM, CROTCHET -> { return false; }
            case QUAVER, SQUAVER, DSQUAVER, HDSQUAVER -> { return true; }
        }
        return false;
    }

    static BeamGroup instantiatedBeamTupleToBeamGroup(InstantiatedBeamGroupTuple beamTuple, Line line) {
        if (beamTuple.chords.size() == 1) {
            if (!isBeamed(beamTuple.chords.get(0).noteType)) {
                return instantiatedChordTupleToChord(beamTuple.chords.get(0), line);
            }
        }
        List<Chord> chords = new ArrayList<>();
        for (InstantiatedChordTuple chordTuple : beamTuple.chords) {
            chords.add(instantiatedChordTupleToChord(chordTuple, line));
        }
        BeamGroup group = new BeamGroup(chords);
        for (BeamTuple tuple : beamTuple.beams) {
            group.addBeam(tuple.start, tuple.end, tuple.number);
        }
        return group;
    }

    static RestTuple beamTupleToRestTuple(BeamGroupTuple tuple, float lineTime) {
        float startTime = 100000000;
        float endTime = 0;
        for (ChordTuple chordTuple : tuple.chords) {
            startTime = Math.min(chordTuple.crotchets - lineTime, startTime);
            endTime = Math.max(chordTuple.crotchets + chordTuple.duration - lineTime, endTime);
        }
        return new RestTuple(startTime, endTime);
    }

    static Whitespace restTupleToRest(RestTuple tuple, Line line) {
        return new Rest(new MusicalPosition(line, tuple.startTime), new MusicalPosition(line, tuple.endTime));
    }

    static InstantiatedChordTuple chordTupleToInstantiatedChordTuple(ChordTuple chord, float lineTime, int lineNum) {
        List<uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch> pitches = new ArrayList<>();
        List<uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental> accidentals = new ArrayList<>();
        for (Note note : chord.notes) {
            if (note.getPitch() != null) {
                pitches.add(new uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch(pitchToGrandStaveLine(note.getPitch()) - chord.lowestLine, 0));
            } else {
                pitches.add(new uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch(0, 0));
            }
            if (note.getAccidental() != null) {
                switch (note.getAccidental().getValue()) {
                    case FLAT -> { accidentals.add(uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.FLAT); }
                    case SHARP -> { accidentals.add(uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.SHARP); }
                    case FLAT_FLAT -> { accidentals.add(uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.DOUBLE_FLAT); }
                    case DOUBLE_SHARP -> { accidentals.add(uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.DOUBLE_SHARP); }
                    case NATURAL -> { accidentals.add(uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.NATURAL); }
                    default -> { accidentals.add(uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.NONE); }
                }
            } else {
                accidentals.add(uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.NONE);
            }
        }
        return new InstantiatedChordTuple(pitches, accidentals, chord.crotchets - lineTime, chord.duration, convertNoteType(chord.notes.get(0).getType()), getDotNumber(chord.notes.get(0)));
    }

    static int getDotNumber(Note note) {
        if (note.getDot() == null) {
            return 0;
        }
        return note.getDot().size();
    }

    static Chord instantiatedChordTupleToChord(InstantiatedChordTuple chordTuple, Line line) {
        return new Chord(chordTuple.pitches, chordTuple.accidentals, new MusicalPosition(line, chordTuple.crotchetsIntoLine), chordTuple.duration, chordTuple.noteType, chordTuple.dots);
    }

    static InstantiatedPulseLineTuple pulseTupleToInstantiatedPulseTuple(PulseLineTuple tuple, float lineTime, int lineNum) {
        return new InstantiatedPulseLineTuple(tuple.time - lineTime, tuple.name, tuple.beatWeight);
    }

    static PulseLine instantiatedPulseLineTupleToPulseLine(InstantiatedPulseLineTuple pulseTuple, Line line) {
        switch (pulseTuple.beatWeight) {
            case 0:
                return new BarLine(new MusicalPosition(line, pulseTuple.timeInLine), pulseTuple.name);
            default:
                return new BeatLine(new MusicalPosition(line, pulseTuple.timeInLine), pulseTuple.beatWeight);
        }
    }

    static uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType convertNoteType(NoteType noteType) {
        switch (noteType.getValue()) {
            case "maxima" -> { return uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.MAXIMA; }
            case "long" -> { return uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.BREVE; }
            case "whole" -> { return uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.SEMIBREVE; }
            case "half" -> { return uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.MINIM; }
            case "quarter" -> { return uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.CROTCHET; }
            case "eighth" -> { return uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.QUAVER; }
            case "16th" -> { return uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.SQUAVER; }
            case "32nd" -> { return uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.DSQUAVER; }
            case "64th" -> { return uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.HDSQUAVER; }
            default -> { throw new IllegalArgumentException(); }
        }
    }

    // translates a pitch into the number of lines above the root of C0.
    static int pitchToGrandStaveLine(Pitch pitch) {
        int line = 0;
        switch (pitch.getStep()) {
            case C -> { line += 0; }
            case D -> { line += 1; }
            case E -> { line += 2; }
            case F -> { line += 3; }
            case G -> { line += 4; }
            case A -> { line += 5; }
            case B -> { line += 6; }
        }
        line += 7 * pitch.getOctave();
        return line;
    }

    static int pitchToGrandStaveLine(Step step, int octave) {
        int line = 0;
        switch (step) {
            case C -> { line += 0; }
            case D -> { line += 1; }
            case E -> { line += 2; }
            case F -> { line += 3; }
            case G -> { line += 4; }
            case A -> { line += 5; }
            case B -> { line += 6; }
        }
        line += 7 * octave;
        return line;
    }

    static boolean isNewline(Direction direction) {
        if (direction.getDirectionType() != null) {
            for (var directionType : direction.getDirectionType()) {
                if (directionType.getWordsOrSymbol() != null) {
                    for (var wordOrSymbol : directionType.getWordsOrSymbol()) {
                        if (wordOrSymbol instanceof FormattedTextId) {
                            if (((FormattedTextId) wordOrSymbol).getValue().equals("\\n")) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    static boolean isNewSection(Direction direction) {
        if (direction.getDirectionType() != null) {
            for (var directionType : direction.getDirectionType()) {
                if (directionType.getWordsOrSymbol() != null) {
                    for (var wordOrSymbol : directionType.getWordsOrSymbol()) {
                        if (wordOrSymbol instanceof FormattedTextId) {
                            if (((FormattedTextId) wordOrSymbol).getValue().equals("\\s")) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    static TimeSignature parseTimeSignature(List<JAXBElement<String>> timeSignature) {
        if (timeSignature != null) {
            TimeSignature ret = new TimeSignature();
            for (JAXBElement<String> timeElement : timeSignature) {
                if (timeElement.getName().getLocalPart().equals("beats")) {
                    ret.setBeatNum(Integer.parseInt(timeElement.getValue()));
                } else if (timeElement.getName().getLocalPart().equals("beat-type")) {
                    ret.setBeatType(Integer.parseInt(timeElement.getValue()));
                }
            }
            return ret;
        }
        return null;
    }

    static int clefToLowestLineGrandStaveLine(uk.ac.cam.optimisingmusicnotation.representation.properties.Clef clef) {
        int lowestLineGrandStaveLine = 0;
        switch (clef.getSign()) {
            case C -> { lowestLineGrandStaveLine = pitchToGrandStaveLine(Step.C, 4); }
            case F -> { lowestLineGrandStaveLine = pitchToGrandStaveLine(Step.F, 3); }
            case G -> { lowestLineGrandStaveLine = pitchToGrandStaveLine(Step.G, 4); }
            case PERCUSSION -> { lowestLineGrandStaveLine = 0; }
        }
        lowestLineGrandStaveLine += 7 * clef.getOctaveChange();
        lowestLineGrandStaveLine -= clef.getLine();

        return lowestLineGrandStaveLine;
    }

    static uk.ac.cam.optimisingmusicnotation.representation.properties.Clef parseClef(org.audiveris.proxymusic.Clef clef) {
        int octaveOffset = 0;
        if (clef.getClefOctaveChange() != null) {
            octaveOffset = clef.getClefOctaveChange().intValue();
        }
        int staveLine = 0;
        if (clef.getLine() != null) {
            staveLine = (clef.getLine().intValue() - 1) * 2;
        }
        switch (clef.getSign()) {
            case C -> {
                return new uk.ac.cam.optimisingmusicnotation.representation.properties.Clef(
                        uk.ac.cam.optimisingmusicnotation.representation.properties.ClefSign.C, staveLine, octaveOffset);
            }
            case F -> {
                return new uk.ac.cam.optimisingmusicnotation.representation.properties.Clef(
                        uk.ac.cam.optimisingmusicnotation.representation.properties.ClefSign.F, staveLine, octaveOffset);
            }
            case G -> {
                return new uk.ac.cam.optimisingmusicnotation.representation.properties.Clef(
                        uk.ac.cam.optimisingmusicnotation.representation.properties.ClefSign.G, staveLine, octaveOffset);
            }
            case PERCUSSION -> {
                return new uk.ac.cam.optimisingmusicnotation.representation.properties.Clef(
                        uk.ac.cam.optimisingmusicnotation.representation.properties.ClefSign.PERCUSSION, staveLine, octaveOffset);
            }
        }
        throw new IllegalArgumentException("Unknown clef symbol");
    }

    static void addPulseLines(TimeSignature time, float measureStartTime, List<PulseLineTuple> pulseLines, String measureName) {
        switch (time.beatNum) {
            case 1:
                pulseLines.add(new PulseLineTuple(measureStartTime, measureName, 0));
                break;
            case 2:
                pulseLines.add(new PulseLineTuple(measureStartTime, measureName, 0));
                pulseLines.add(new PulseLineTuple(measureStartTime + 1 * 4f / time.beatType, measureName, 1));
                break;
            case 3:
                pulseLines.add(new PulseLineTuple(measureStartTime, measureName, 0));
                pulseLines.add(new PulseLineTuple(measureStartTime + 1 * 4f / time.beatType, measureName, 1));
                pulseLines.add(new PulseLineTuple(measureStartTime + 2 * 4f / time.beatType, measureName, 1));
                break;
            case 4:
                pulseLines.add(new PulseLineTuple(measureStartTime, measureName, 0));
                pulseLines.add(new PulseLineTuple(measureStartTime + 1 * 4f / time.beatType, measureName, 1));
                pulseLines.add(new PulseLineTuple(measureStartTime + 2 * 4f / time.beatType, measureName, 1));
                pulseLines.add(new PulseLineTuple(measureStartTime + 3 * 4f / time.beatType, measureName, 1));
                break;
            case 5:
                pulseLines.add(new PulseLineTuple(measureStartTime, measureName, 0));
                pulseLines.add(new PulseLineTuple(measureStartTime + 1 * 4f / time.beatType, measureName, 2));
                pulseLines.add(new PulseLineTuple(measureStartTime + 2 * 4f / time.beatType, measureName, 2));
                pulseLines.add(new PulseLineTuple(measureStartTime + 3 * 4f / time.beatType, measureName, 1));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4 * 4f / time.beatType, measureName, 2));
                break;
            case 6:
                pulseLines.add(new PulseLineTuple(measureStartTime, measureName, 0));
                pulseLines.add(new PulseLineTuple(measureStartTime + 1f * 4f / time.beatType, measureName, 2));
                pulseLines.add(new PulseLineTuple(measureStartTime + 2f * 4f / time.beatType, measureName, 2));
                pulseLines.add(new PulseLineTuple(measureStartTime + 3f * 4f / time.beatType, measureName, 1));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4f * 4f / time.beatType, measureName, 2));
                pulseLines.add(new PulseLineTuple(measureStartTime + 5f * 4f / time.beatType, measureName, 2));
                break;
            case 7:
                pulseLines.add(new PulseLineTuple(measureStartTime, measureName, 0));
                pulseLines.add(new PulseLineTuple(measureStartTime + 1 * 4f / time.beatType, measureName, 1));
                pulseLines.add(new PulseLineTuple(measureStartTime + 2 * 4f / time.beatType, measureName, 2));
                pulseLines.add(new PulseLineTuple(measureStartTime + 3 * 4f / time.beatType, measureName, 1));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4 * 4f / time.beatType, measureName, 2));
                pulseLines.add(new PulseLineTuple(measureStartTime + 5 * 4f / time.beatType, measureName, 1));
                pulseLines.add(new PulseLineTuple(measureStartTime + 6 * 4f / time.beatType, measureName, 1));
                break;
            case 8:
                break;
            case 9:
                pulseLines.add(new PulseLineTuple(measureStartTime, measureName, 0));
                pulseLines.add(new PulseLineTuple(measureStartTime + 1f * 4f / time.beatType, measureName, 2));
                pulseLines.add(new PulseLineTuple(measureStartTime + 2f * 4f / time.beatType, measureName, 2));
                pulseLines.add(new PulseLineTuple(measureStartTime + 3f * 4f / time.beatType, measureName, 1));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4f * 4f / time.beatType, measureName, 2));
                pulseLines.add(new PulseLineTuple(measureStartTime + 5f * 4f / time.beatType, measureName, 2));
                pulseLines.add(new PulseLineTuple(measureStartTime + 6f * 4f / time.beatType, measureName, 1));
                pulseLines.add(new PulseLineTuple(measureStartTime + 7f * 4f / time.beatType, measureName, 2));
                pulseLines.add(new PulseLineTuple(measureStartTime + 8f * 4f / time.beatType, measureName, 2));
                break;
            case 10:
                break;
            case 11:
                break;
            case 12:
                pulseLines.add(new PulseLineTuple(measureStartTime, measureName, 0));
                pulseLines.add(new PulseLineTuple(measureStartTime + 1f * 4f / time.beatType, measureName, 2));
                pulseLines.add(new PulseLineTuple(measureStartTime + 2f * 4f / time.beatType, measureName, 2));
                pulseLines.add(new PulseLineTuple(measureStartTime + 3f * 4f / time.beatType, measureName, 1));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4f * 4f / time.beatType, measureName, 2));
                pulseLines.add(new PulseLineTuple(measureStartTime + 5f * 4f / time.beatType, measureName, 2));
                pulseLines.add(new PulseLineTuple(measureStartTime + 6f * 4f / time.beatType, measureName, 1));
                pulseLines.add(new PulseLineTuple(measureStartTime + 7f * 4f / time.beatType, measureName, 2));
                pulseLines.add(new PulseLineTuple(measureStartTime + 8f * 4f / time.beatType, measureName, 2));
                pulseLines.add(new PulseLineTuple(measureStartTime + 9f * 4f / time.beatType, measureName, 1));
                pulseLines.add(new PulseLineTuple(measureStartTime + 10f * 4f / time.beatType, measureName, 2));
                pulseLines.add(new PulseLineTuple(measureStartTime + 11f * 4f / time.beatType, measureName, 2));
                break;
            case 13:
                break;
        }
    }

    public static Object openMXL(String input) {
        try (ZipInputStream xml = new ZipInputStream(new FileInputStream(input))) {
            ZipEntry zipEntry = xml.getNextEntry();
            while (zipEntry != null) {
                if(zipEntry.getName().equals("score.xml")) {
                    return Marshalling.unmarshal(xml);
                }
                zipEntry = xml.getNextEntry();
            }
            return null;
        } catch (Exception e) {

        }
        return null;
    }

    private Parser() {}

    public static void main(String[] args) {
        String target = "test_scores/TestScore2.mxl";
        if (args.length > 0) {
            target = args[0];
        }

        Object mxl = Parser.openMXL(target);
        System.out.println(mxl.toString());
        Score score = Parser.parseToScore(mxl);
        String outDir = "./out/"; // Output Directory
        Path outDirPath = Paths.get(outDir);
        if (!Files.exists(outDirPath)) {
            try {
                Files.createDirectory(outDirPath);
            } catch (IOException e) {
                System.err.println("Error while creating output directory path: ");
                e.printStackTrace();
            }
        }

        String outTarget = "output";

        if (args.length > 2) {
            outTarget = args[2];
        }

        int targetPart = 0;
        if (args.length > 1) {
            targetPart = Integer.valueOf(args[1]);
        }

        if (targetPart == -1) {
            for (Part part : score.parts) {
                try (PdfWriter writer = new PdfWriter(outDir + outTarget + "_" + part.name + ".pdf")) {
                    PdfDocument pdf = new PdfDocument(writer);
                    PageSize ps = PageSize.A4;
                    pdf.addNewPage(ps);

                    PdfMusicCanvas canvas = new PdfMusicCanvas(pdf);
                    Piece testPiece = new Piece(part.sections);
                    testPiece.draw(canvas);
                    pdf.close();
                }
                catch (Exception e) {
                    System.err.println("Error while creating PDF: ");
                    e.printStackTrace();
                }
            }
        } else {
            try (PdfWriter writer = new PdfWriter(outDir + outTarget + ".pdf")) {
                PdfDocument pdf = new PdfDocument(writer);
                PageSize ps = PageSize.A4;
                pdf.addNewPage(ps);

                PdfMusicCanvas canvas = new PdfMusicCanvas(pdf);
                Piece testPiece = new Piece(score.parts.get(targetPart).sections);
                testPiece.draw(canvas);
                pdf.close();
            }
            catch (IOException e) {
                System.err.println("Error while creating PDF: ");
                e.printStackTrace();
            }
        }
    }
}
