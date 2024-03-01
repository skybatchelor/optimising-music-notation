package uk.ac.cam.optimisingmusicnotation.mxlparser;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import org.audiveris.proxymusic.*;
import org.audiveris.proxymusic.Pitch;
import org.audiveris.proxymusic.util.Marshalling;
import uk.ac.cam.optimisingmusicnotation.rendering.PdfMusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.*;
import uk.ac.cam.optimisingmusicnotation.representation.properties.*;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups.Beamlet;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups.Flag;
import uk.ac.cam.optimisingmusicnotation.representation.whitespaces.Whitespace;

import javax.xml.bind.JAXBElement;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.String;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Parser {
    public static final boolean NEW_SECTION_FOR_KEY_SIGNATURE = true;
    public static final float EPSILON = 0.001f;
    public static final boolean END_BAR_LINE_TIME_SIGNATURE = false;
    public static final boolean END_BAR_LINE_NAME = false;
    public static final boolean TIME_NORMALISED_PARSING = true;
    public static final float TIME_NORMALISATION_FACTOR = 60 * 12;
    public static float startBpm = 120f;
    public static HashSet<String> escapedDirectives = new HashSet<>() {{ add("\\n"); add("\\s"); add("\\w"); }};
    public static HashSet<String> boxedDirectives = new HashSet<>() {{ add("n"); add("s"); add("w"); }};

    public static Score parseToScore(Object mxl) {
        if (mxl instanceof ScorePartwise partwise) {
            TreeMap<Float, Float> newlines = new TreeMap<>() {{ put(0f, 0f); }};
            TreeSet<Float> newSections = new TreeSet<>() {{ add(0f); }};
            TreeMap<String, Part> parts = new TreeMap<>();

            TreeMap<Float, TempoTuple> tempoMarkings = new TreeMap<>();

            TreeMap<Float, Float> tempoChanges = new TreeMap<>() {{ put(0f, startBpm); }};

            BiConsumer<Float, Float> addNewSection = ((time, offset) -> { newSections.add(time); newlines.put(time, offset); });
            BiConsumer<Float, Float> addNewline = newlines::put;

            List<Object> scoreParts = partwise.getPartList().getPartGroupOrScorePart();
            for (Object part : scoreParts) {
                if (part instanceof ScorePart scorePart) {
                    Part ret = new Part();
                    parts.put(scorePart.getId(), ret);
                    ret.setName(scorePart.getPartName().getValue());
                    ret.setAbbreviation(scorePart.getPartAbbreviation().getValue());
                } else if (part instanceof PartGroup partGroup) {

                } else {

                }
            }

            float totalLength = 0;

            TreeMap<String, ParsingPartTuple> parsingParts = new TreeMap<>();
            TreeMap<String, List<LineTuple>> partLines = new TreeMap<>();

            TreeMap<String, List<TreeMap<Float, Line>>> partSections = new TreeMap<>();
            List<ScorePartwise.Part> musicParts = partwise.getPart();

            for (ScorePartwise.Part part : musicParts) {
                float currentTempo = startBpm;

                TreeMap<MusicGroupType, TreeMap<Integer, MusicGroupTuple>> musicGroupTuples = new TreeMap<>();
                for (MusicGroupType musicGroupType : MusicGroupType.values()) {
                    musicGroupTuples.put(musicGroupType, new TreeMap<>());
                }

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
                KeySignature currentKeySignature = new KeySignature();
                int divisions = 0;
                float prevChange;
                int lowestLineGrandStaveLine = 0;
                ChordTuple currentChord = new ChordTuple(0, 0, currentKeySignature);
                BeamGroupTuple beamGroup = new BeamGroupTuple();
                List<ScorePartwise.Part.Measure> measures = part.getMeasure();

                for (ScorePartwise.Part.Measure measure : measures) {
                    boolean newTimeSignature = false;
                    float measureTime = 0;
                    float measureLength = currentTimeSignature.getBeatNum() * 4f / (currentTimeSignature.getBeatType());

                    for(Object component : measure.getNoteOrBackupOrForward()) {
                        if (component instanceof Attributes attributes) {
                            if (attributes.getTime() != null) {
                                for(Time time : attributes.getTime()) {
                                    var timeSignature = parseTimeSignature(time.getTimeSignature());
                                    if (timeSignature != null) {
                                        currentTimeSignature = timeSignature;
                                        newTimeSignature = true;
                                    }
                                }
                            }
                            if (attributes.getKey() != null) {
                                for(Key key : attributes.getKey()) {
                                    var keySignature = parseKeySignature(key, currentKeySignature);
                                    if (keySignature != null) {
                                        currentKeySignature = keySignature;
                                        currentPart.keySignatures.put(measureStartTime + measureTime, currentKeySignature);
                                        if (NEW_SECTION_FOR_KEY_SIGNATURE) {
                                            if (measureTime == 0) {
                                                addNewSection.accept(measureStartTime + measureTime, 0f);
                                            } else {
                                                addNewSection.accept(measureStartTime + measureTime, measureTime - measureLength);
                                            }
                                        }
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
                            measureLength = currentTimeSignature.getBeatNum() * 4f / (currentTimeSignature.getBeatType());
                        } else if (component instanceof Note note) {
                            parseSlurs(musicGroupTuples, currentPart, note, measureStartTime + measureTime);
                            if (note.getDuration() != null) {
                                prevChange = note.getDuration().intValue() / (float)divisions;
                            } else {
                                prevChange = 0;
                            }
                            if (note.getChord() == null) {
                                currentChord = new ChordTuple(measureStartTime + measureTime, lowestLineGrandStaveLine, currentKeySignature);
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
                                            case BEGIN, CONTINUE -> {
                                                beamGroup.addChord(currentChord);
                                                addedToBeamGroup = true;
                                            }
                                            case END, FORWARD_HOOK, BACKWARD_HOOK -> {
                                                beamGroup.addChord(currentChord);
                                                currentPart.putInBeamGroup(beamGroup);
                                                beamGroup = new BeamGroupTuple();
                                                addedToBeamGroup = true;
                                            }
                                        }
                                    }
                                }
                            }
                            if (!addedToBeamGroup && note.getChord() == null) {
                                beamGroup.addChord(currentChord);
                                currentPart.putInBeamGroup(beamGroup);
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
                                if (measureTime + offset == 0) {
                                    addNewline.accept(measureStartTime + measureTime + offset, 0f);
                                } else {
                                    addNewline.accept(measureStartTime + measureTime + offset, measureTime + offset - measureLength);
                                }
                            }
                            if (isNewSection(direction)) {
                                if (measureTime + offset == 0) {
                                    addNewSection.accept(measureStartTime + measureTime + offset, 0f);
                                } else {
                                    addNewSection.accept(measureStartTime + measureTime + offset, measureTime + offset - measureLength);
                                }
                            }
                            if (isArtisticWhitespace(direction)) {
                                var whitespace = new BeamGroupTuple();
                                whitespace.startTime = (measureStartTime + measureTime + offset) - RenderingConfiguration.artisticWhitespaceWidth;
                                whitespace.endTime = (measureStartTime + measureTime + offset);
                                var restChord = new ChordTuple(whitespace.startTime, 0, currentKeySignature);
                                restChord.duration = RenderingConfiguration.artisticWhitespaceWidth;
                                var restNote = new Note();
                                restNote.setRest(new org.audiveris.proxymusic.Rest());
                                restChord.notes.add(restNote);
                                whitespace.addChord(restChord);
                                whitespace.staff = getStaff(direction.getStaff());
                                currentPart.putInBeamGroup(whitespace);
                            }
                            parseMusicDirective(musicGroupTuples, currentPart, direction, measureStartTime + measureTime + offset);
                            currentTempo = parseTempoMarking(tempoMarkings, tempoChanges, currentTempo, direction, measureStartTime + measureTime + offset);
                            currentPart.directions.put(measureStartTime + measureTime + offset, direction);
                        }
                    }

                    if (newTimeSignature) {
                        addPulseLines(currentTimeSignature, measureStartTime, currentPart.pulseLines,
                                measure.getText() == null ? measure.getNumber() == null ? "" : measure.getNumber() : measure.getText(), currentTimeSignature);
                    } else {
                        addPulseLines(currentTimeSignature, measureStartTime, currentPart.pulseLines,
                                measure.getText() == null ? measure.getNumber() == null ? "" : measure.getNumber() : measure.getText(), null);
                    }
                    measureStartTime += measureLength;
                }
                totalLength = Math.max(measureStartTime, totalLength);
                currentPart.pulseLines.add(new PulseLineTuple(measureStartTime, "", 0, null));
            }

            var integratedTime = integrateTime(tempoChanges);

            var nNewlines = normalisedNewlines(newlines, integratedTime);
            var nNewSections = normalisedSections(newSections, integratedTime);

            Map<Float, Integer> lineIndices = new HashMap<>();
            List<Float> lineLengths = new ArrayList<>();
            List<Float> lineOffsets = new ArrayList<>();
            int index = 0;
            float prevLineStart = 0;
            for (Float newline : nNewlines.keySet()) {
                lineIndices.put(newline, index);
                lineOffsets.add(nNewlines.get(newline));
                if (index != 0) {
                    lineLengths.add(newline - prevLineStart);
                    prevLineStart = newline;
                }
                for (List<LineTuple> partList : partLines.values()) {
                    partList.add(new LineTuple(newline));
                }
                ++index;
            }
            lineLengths.add(normaliseTime(totalLength, integratedTime) - prevLineStart);

            var sectionIndices = createSectionIndices(nNewSections, partSections);

            var finalSections = finaliseSections(
                    populatePartSections(
                            partSections,
                            instantiateLines(
                                    nNewlines,
                                    populatePartLines(
                                            partLines, parsingParts, tempoMarkings, nNewlines, lineIndices, integratedTime),
                                    lineLengths, lineOffsets, parsingParts),
                            nNewSections, sectionIndices),
                    parsingParts);

            for (Map.Entry<String, List<Section>> part : finalSections.entrySet()) {
                parts.get(part.getKey()).setSections(part.getValue());
                parts.get(part.getKey()).setUpwardsStems(parsingParts.get(part.getKey()).upwardsStems);
            }
            return new Score(getWorkTitle(partwise), getComposer(partwise), parts.values().stream().toList());
        }
        return null;
    }

    static int getStaff(BigInteger staff) {
        return staff == null ? 1 : staff.intValue();
    }


    static int getVoice(String voice) {
        if (voice != null) {
            return Integer.parseInt(voice);
        }
        return 1;
    }

    static TreeMap<Float, TempoChangeTuple> integrateTime(TreeMap<Float, Float> tempoChanges) {
        TreeMap<Float, TempoChangeTuple> integratedTime = new TreeMap<>() {{ put(0f, new TempoChangeTuple(0f, 0f, TIME_NORMALISATION_FACTOR / tempoChanges.firstEntry().getValue())); }};
        if (TIME_NORMALISED_PARSING) {
            for (Map.Entry<Float, Float> entry : tempoChanges.entrySet()) {
                var timeEntry = integratedTime.lowerEntry(entry.getKey());
                var tempoEntry = tempoChanges.lowerEntry(entry.getKey());
                if (timeEntry != null && tempoEntry != null) {
                    integratedTime.put(entry.getKey(), new TempoChangeTuple(
                            entry.getKey(),
                            timeEntry.getValue().time() + (entry.getKey() - timeEntry.getKey()) * (TIME_NORMALISATION_FACTOR / tempoEntry.getValue()),
                            TIME_NORMALISATION_FACTOR / entry.getValue()));
                }
            }
        } else {
            integratedTime.clear();
            integratedTime.put(0f, new TempoChangeTuple(0f, 0f, 1));
        }
        return integratedTime;
    }

    static TreeMap<Float, Float> normalisedNewlines(TreeMap<Float, Float> newlines, TreeMap<Float, TempoChangeTuple> integratedTime) {
        var normalisedNewlines = new TreeMap<Float, Float>();
        for (var newline : newlines.entrySet()) {
            normalisedNewlines.put(normaliseTime(newline.getKey(), integratedTime), normaliseDuration(newline.getKey(), newline.getValue(), integratedTime));
        }
        return normalisedNewlines;
    }

    static TreeSet<Float> normalisedSections(TreeSet<Float> newSections, TreeMap<Float, TempoChangeTuple> integratedTime) {
        var normalisedSections = new TreeSet<Float>();
        for (var newSection : newSections) {
            normalisedSections.add(normaliseTime(newSection, integratedTime));
        }
        return normalisedSections;
    }

    static Map<Float, Integer> createSectionIndices(TreeSet<Float> newSections, TreeMap<String, List<TreeMap<Float, Line>>> partSections) {
        Map<Float, Integer> sectionIndices = new HashMap<>();
        int index = 0;
        for (Float newSection : newSections) {
            sectionIndices.put(newSection, index);
            for (List<TreeMap<Float, Line>> lineList : partSections.values()) {
                lineList.add(new TreeMap<>());
            }
            ++index;
        }
        return sectionIndices;
    }

    static TreeMap<String, List<LineTuple>> populatePartLines(TreeMap<String, List<LineTuple>> partLines,
                                                              TreeMap<String, ParsingPartTuple> parsingParts,
                                                              TreeMap<Float, TempoTuple> tempoMarkings,
                                                              TreeMap<Float, Float> newlines,
                                                              Map<Float, Integer> lineIndices,
                                                              TreeMap<Float, TempoChangeTuple> integratedTime) {
        for (Map.Entry<String, ParsingPartTuple> part : parsingParts.entrySet()) {
            for (var staffEntry : part.getValue().staveBeamGroups.entrySet()) {
                for (var voiceEntry : staffEntry.getValue().entrySet()) {
                    for (BeamGroupTuple beam : voiceEntry.getValue()) {
                        if (beam.isRest()) {
                            beam.splitToInstantiatedRestTuple(newlines, lineIndices, integratedTime, partLines.get(part.getKey()));
                        } else {
                            beam.splitToInstantiatedBeamGroupTuple(newlines, lineIndices, integratedTime, partLines.get(part.getKey()));
                        }
                    }
                }
            }

            for (var staffEntry : part.getValue().staveMusicGroups.entrySet()) {
                for (MusicGroupTuple musicGroup : staffEntry.getValue()) {
                    musicGroup.splitToInstantiatedMusicGroupTuple(newlines, lineIndices, integratedTime, partLines.get(part.getKey()));
                }
            }
            for (Map.Entry<Float, TempoTuple> entry : tempoMarkings.entrySet()) {
                float lineStart = newlines.floorKey(normaliseTime(entry.getValue().time, integratedTime));
                int lineNum = lineIndices.get(lineStart);
                partLines.get(part.getKey()).get(lineNum).tempoMarkings.add(entry.getValue().toInstantiatedTempoTuple(lineStart, integratedTime));
            }
            for (PulseLineTuple pulseLine : part.getValue().pulseLines) {
                float lineStart = newlines.floorKey(normaliseTime(pulseLine.time, integratedTime));
                int lineNum = lineIndices.get(lineStart);
                partLines.get(part.getKey()).get(lineNum).pulses.add(pulseLine.toInstantiatedPulseTuple(lineStart, integratedTime));
                Float lowerLineStart = newlines.lowerKey(normaliseTime(pulseLine.time, integratedTime));
                if (lowerLineStart != null) {
                    int lowerLineNum = lineIndices.get(lowerLineStart);
                    if (lowerLineNum != lineNum) {
                        var pulseTuple = pulseLine.toInstantiatedPulseTuple(lowerLineStart, integratedTime);
                        if (!END_BAR_LINE_NAME) {
                            pulseTuple.name = "";
                        }
                        if (!END_BAR_LINE_TIME_SIGNATURE) {
                            pulseTuple.timeSig = null;
                        }
                        partLines.get(part.getKey()).get(lowerLineNum).pulses.add(pulseTuple);
                    }
                }
            }
        }
        return partLines;
    }

    static TreeMap<String, List<InstantiatedLineTuple>> instantiateLines(TreeMap<Float, Float> newlines, TreeMap<String, List<LineTuple>> partLines,
                                                                         List<Float> lineLengths, List<Float> lineOffsets, TreeMap<String, ParsingPartTuple> parsingPart) {
        TreeMap<String, List<InstantiatedLineTuple>> finalLines = new TreeMap<>();
        List<Float> newlinesList = newlines.keySet().stream().toList();

        StaveLineAverager averager = new MeanAverager();

        for (Map.Entry<String, List<LineTuple>> part : partLines.entrySet()) {
            finalLines.put(part.getKey(), new ArrayList<>());
            averager.reset();
            for (int i = 0; i < part.getValue().size(); ++i) {
                var chords = new TreeMap<Integer, TreeMap<Integer, TreeMap<Float, Chord>>>();
                var rests = new TreeMap<Integer, TreeMap<Integer, TreeMap<Float, Whitespace>>>();
                var needsFlag = new TreeMap<Integer, TreeMap<Integer, Map<Chord, Integer>>>();
                var needsBeamlet = new TreeMap<Integer, TreeMap<Integer, Map<Chord, Integer>>>();
                Stave stave = new Stave();

                Line tempLine = new Line(new ArrayList<>() {{ add(stave); }}, lineLengths.get(i), lineOffsets.get(i), i);
                finalLines.get(part.getKey()).add(new InstantiatedLineTuple(newlinesList.get(i), tempLine));
                for (var staffEntry : part.getValue().get(i).rests.entrySet()) {
                    Util.ensureCapacity(tempLine.getStaves(), Stave::new, staffEntry.getKey() - 1);
                    Util.ensureKey(chords, TreeMap::new, staffEntry.getKey());
                    Util.ensureKey(rests, TreeMap::new, staffEntry.getKey());
                    Util.ensureKey(needsFlag, TreeMap::new, staffEntry.getKey());
                    Util.ensureKey(needsBeamlet, TreeMap::new, staffEntry.getKey());
                    for (var voiceEntry : staffEntry.getValue().entrySet()) {
                        Util.ensureKey(chords.get(staffEntry.getKey()), TreeMap::new, voiceEntry.getKey());
                        Util.ensureKey(rests.get(staffEntry.getKey()), TreeMap::new, voiceEntry.getKey());
                        Util.ensureKey(needsFlag.get(staffEntry.getKey()), HashMap::new, voiceEntry.getKey());
                        Util.ensureKey(needsBeamlet.get(staffEntry.getKey()), HashMap::new, voiceEntry.getKey());
                        var fusedRests = InstantiatedRestTuple.fuseRestTuples(voiceEntry.getValue());
                        for (InstantiatedRestTuple restTuple : fusedRests) {
                            tempLine.getStaves().get(staffEntry.getKey() - 1).addWhiteSpace(restTuple.toRest(tempLine, rests));
                        }
                    }
                }

                for (var staffEntry : part.getValue().get(i).notes.entrySet()) {
                    Util.ensureCapacity(tempLine.getStaves(), Stave::new, staffEntry.getKey() - 1);
                    Util.ensureKey(chords, TreeMap::new, staffEntry.getKey());
                    Util.ensureKey(rests, TreeMap::new, staffEntry.getKey());
                    Util.ensureKey(needsFlag, TreeMap::new, staffEntry.getKey());
                    Util.ensureKey(needsBeamlet, TreeMap::new, staffEntry.getKey());
                    for (var voiceEntry : staffEntry.getValue().entrySet()) {
                        Util.ensureKey(chords.get(staffEntry.getKey()), TreeMap::new, voiceEntry.getKey());
                        Util.ensureKey(rests.get(staffEntry.getKey()), TreeMap::new, voiceEntry.getKey());
                        Util.ensureKey(needsFlag.get(staffEntry.getKey()), HashMap::new, voiceEntry.getKey());
                        Util.ensureKey(needsBeamlet.get(staffEntry.getKey()), HashMap::new, voiceEntry.getKey());
                        for (var beamTuple : voiceEntry.getValue()) {
                            beamTuple.addToAverager(averager);
                            tempLine.getStaves().get(staffEntry.getKey() - 1).addStaveElement(beamTuple.toBeamGroup(tempLine, chords, needsFlag, needsBeamlet));
                        }
                    }
                }

                for (var staffEntry : chords.entrySet()) {
                    for (var voiceEntry : staffEntry.getValue().entrySet()) {
                        for (var chordEntry : voiceEntry.getValue().entrySet()) {
                            if (chordEntry.getKey() > chords.firstKey()) {
                                chordEntry.getValue().removeTiesTo();
                            }
                        }
                    }
                }

                for (var staffEntry : needsFlag.entrySet()) {
                    for (var voiceEntry : staffEntry.getValue().entrySet()) {
                        for (var entry : voiceEntry.getValue().entrySet()) {
                            var preEntry = chords.get(staffEntry.getKey()).get(voiceEntry.getKey()).lowerEntry(entry.getKey().getCrotchetsIntoLine());
                            var preChord = preEntry == null ? null : preEntry.getValue();
                            var preRestEntry = rests.get(staffEntry.getKey()).get(voiceEntry.getKey()).floorEntry(entry.getKey().getCrotchetsIntoLine());
                            if (preRestEntry != null && preRestEntry.getValue().getEndCrotchets() > preChord.getCrotchetsIntoLine()) {
                                preChord = null;
                            }
                            tempLine.getStaves().get(staffEntry.getKey() - 1).addMusicGroup(new Flag(preChord, entry.getKey(), tempLine, entry.getValue()));
                        }
                    }
                }


                for (var staffEntry : needsBeamlet.entrySet()) {
                    for (var voiceEntry : staffEntry.getValue().entrySet()) {
                        for (var entry : voiceEntry.getValue().entrySet()) {
                            var postEntry = chords.get(staffEntry.getKey()).get(voiceEntry.getKey()).higherEntry(entry.getKey().getCrotchetsIntoLine());
                            var postChord = postEntry == null ? null : postEntry.getValue();
                            var postRestEntry = rests.get(staffEntry.getKey()).get(voiceEntry.getKey()).ceilingEntry(entry.getKey().getCrotchetsIntoLine());
                            if (postRestEntry != null && postRestEntry.getValue().getStartCrotchets() < postChord.getCrotchetsIntoLine()) {
                                postChord = null;
                            }
                            tempLine.getStaves().get(staffEntry.getKey() - 1).addMusicGroup(new Beamlet(postChord, entry.getKey(), tempLine, entry.getValue()));
                        }
                    }
                }

                for (InstantiatedPulseLineTuple pulseTuple : part.getValue().get(i).pulses) {
                    tempLine.addPulseLine(pulseTuple.toPulseLine(tempLine));
                }

                for (var staffEntry : part.getValue().get(i).musicGroups.entrySet()) {
                    Util.ensureCapacity(tempLine.getStaves(), Stave::new, staffEntry.getKey() - 1);
                    for (InstantiatedMusicGroupTuple musicGroupTuple : staffEntry.getValue()) {
                        tempLine.getStaves().get(staffEntry.getKey() - 1).addMusicGroup(musicGroupTuple.toMusicGroup(tempLine, chords));
                    }
                }

                for (InstantiatedTempoTuple tempoTuple : part.getValue().get(i).tempoMarkings) {
                    tempLine.getStaves().get(0).addMusicGroup(tempoTuple.toMusicGroup(tempLine, chords));
                }
            }
            parsingPart.get(part.getKey()).upwardsStems = averager.getAverageStaveLine() < 4;
        }
        return finalLines;
    }

    static TreeMap<String, List<TreeMap<Float, Line>>> populatePartSections(TreeMap<String, List<TreeMap<Float, Line>>> partSections,
                                                                            TreeMap<String, List<InstantiatedLineTuple>> finalLines,
                                                                            TreeSet<Float> newSections, Map<Float, Integer> sectionIndices) {
        for (Map.Entry<String, List<InstantiatedLineTuple>> part : finalLines.entrySet()) {
            for (InstantiatedLineTuple instantiatedLineTuple : part.getValue()) {
                float sectionStart = newSections.floor(instantiatedLineTuple.startTime);
                int sectionNum = sectionIndices.get(sectionStart);
                partSections.get(part.getKey()).get(sectionNum).put(instantiatedLineTuple.startTime, instantiatedLineTuple.line);
            }
        }
        return partSections;
    }

    static TreeMap<String, List<Section>> finaliseSections(TreeMap<String, List<TreeMap<Float, Line>>> partSections, TreeMap<String, ParsingPartTuple> parsingParts) {
        TreeMap<String, List<Section>> finalSections = new TreeMap<>();

        for (Map.Entry<String, List<TreeMap<Float, Line>>> part : partSections.entrySet()) {
            List<Section> sections = new ArrayList<>();
            for (TreeMap<Float, Line> lines : part.getValue()) {
                if (lines.size() != 0) {
                    sections.add(new Section(lines.values().stream().toList(),
                            parsingParts.get(part.getKey()).clefs.floorEntry(lines.firstKey()).getValue(),
                            parsingParts.get(part.getKey()).keySignatures.floorEntry(lines.firstKey()).getValue()));
                }
            }
            finalSections.put(part.getKey(), sections);
        }
        return finalSections;
    }

    static String getWorkTitle(ScorePartwise score) {
        if (score.getWork() != null && score.getWork().getWorkTitle() != null) {
            return score.getWork().getWorkTitle();
        }
        return "";
    }

    static String getComposer(ScorePartwise score) {
        if (score.getIdentification() != null && score.getIdentification().getCreator() != null) {
            var composers = new ArrayList<String>();
            for (TypedText text : score.getIdentification().getCreator()) {
                if (text.getType().equals("composer")) {
                    composers.add(text.getValue());
                }
            }
            return String.join(", ", composers);
        }
        return "";
    }

    static float normaliseTime(float time, TreeMap<Float, TempoChangeTuple> integratedTime) {
        if (TIME_NORMALISED_PARSING) {
            var timeEntry = integratedTime.floorEntry(time);
            if (timeEntry != null) {
                return timeEntry.getValue().modulateTime(time);
            } else {
                return integratedTime.firstEntry().getValue().modulateTime(time);
            }
        } else {
            return time;
        }
    }

    static float normaliseDuration(float time, float duration, TreeMap<Float, TempoChangeTuple> integratedTime) {
        var startTime = normaliseTime(time, integratedTime);
        var endTime = normaliseTime(time + duration, integratedTime);
        return endTime - startTime;
    }

    // translates a pitch into the number of lines above the root of C0.
    static int pitchToGrandStaveLine(Pitch pitch) {
        return switch (pitch.getStep()) {
            case C -> 0;
            case D -> 1;
            case E -> 2;
            case F -> 3;
            case G -> 4;
            case A -> 5;
            case B -> 6;
        } + 7 * pitch.getOctave();
    }

    // translates a pitch into the number of lines above the root of C0.
    static int pitchToGrandStaveLine(Unpitched unpitched) {
        return switch (unpitched.getDisplayStep()) {
            case C -> 0;
            case D -> 1;
            case E -> 2;
            case F -> 3;
            case G -> 4;
            case A -> 5;
            case B -> 6;
        } + 7 * unpitched.getDisplayOctave();
    }

    static int pitchToGrandStaveLine(Step step, int octave) {
        return switch (step) {
            case C -> 0;
            case D -> 1;
            case E -> 2;
            case F -> 3;
            case G -> 4;
            case A -> 5;
            case B -> 6;
        } + 7 * octave;
    }

    static uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType convertNoteType(NoteType noteType) {
        return convertNoteType(noteType.getValue());
    }

    static uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType convertNoteType(String noteType) {
        return switch (noteType) {
            case "maxima" -> uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.MAXIMA;
            case "long" -> uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.BREVE;
            case "whole" -> uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.SEMIBREVE;
            case "half" -> uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.MINIM;
            case "quarter" -> uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.CROTCHET;
            case "eighth" -> uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.QUAVER;
            case "16th" -> uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.SQUAVER;
            case "32nd" -> uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.DSQUAVER;
            case "64th" -> uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.HDSQUAVER;
            default -> throw new IllegalArgumentException();
        };
    }

    static boolean isNewline(Direction direction) {
        if (direction.getDirectionType() != null) {
            for (var directionType : direction.getDirectionType()) {
                if (directionType.getWordsOrSymbol() != null) {
                    for (var wordOrSymbol : directionType.getWordsOrSymbol()) {
                        if (wordOrSymbol instanceof FormattedTextId formattedText) {
                            if (formattedText.getValue().equals("n") && formattedText.getEnclosure() == EnclosureShape.RECTANGLE) {
                                return true;
                            }
                            if (formattedText.getValue().equals("\\n")) {
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
                        if (wordOrSymbol instanceof FormattedTextId formattedText) {
                            if (formattedText.getValue().equals("s") && formattedText.getEnclosure() == EnclosureShape.RECTANGLE) {
                                return true;
                            }
                            if (formattedText.getValue().equals("\\s")) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    static boolean isArtisticWhitespace(Direction direction) {
        if (direction.getDirectionType() != null) {
            for (var directionType : direction.getDirectionType()) {
                if (directionType.getWordsOrSymbol() != null) {
                    for (var wordOrSymbol : directionType.getWordsOrSymbol()) {
                        if (wordOrSymbol instanceof FormattedTextId formattedText) {
                            if (formattedText.getValue().equals("w") && formattedText.getEnclosure() == EnclosureShape.RECTANGLE) {
                                return true;
                            }
                            if (formattedText.getValue().equals("\\w")) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    static boolean isDirective(FormattedTextId text) {
        return escapedDirectives.contains(text.getValue())
                || (text.getEnclosure() == EnclosureShape.RECTANGLE && boxedDirectives.contains(text.getValue()));
    }

    static List<MusicGroupType> wedgeGroups = new ArrayList<>(2) {{ add(MusicGroupType.DIM); add(MusicGroupType.CRESC); }};
    static void parseMusicDirective(TreeMap<MusicGroupType, TreeMap<Integer, MusicGroupTuple>> target, ParsingPartTuple currentPart, Direction direction, float time) {
        if (direction.getDirectionType() != null) {
            for (DirectionType directionType : direction.getDirectionType()) {
                if (directionType.getWedge() != null) {
                    Wedge wedge = directionType.getWedge();
                    switch (wedge.getType().name()) {
                        case "DIMINUENDO" -> target.get(MusicGroupType.DIM).put(wedge.getNumber(), new MusicGroupTuple(time, MusicGroupType.DIM, getStaff(direction.getStaff())));
                        case "CRESCENDO" -> target.get(MusicGroupType.CRESC).put(wedge.getNumber(), new MusicGroupTuple(time, MusicGroupType.CRESC, getStaff(direction.getStaff())));
                        case "STOP" -> {
                            for (var type : wedgeGroups) {
                                if (target.get(type).containsKey(wedge.getNumber())) {
                                    var tuple = target.get(type).remove(wedge.getNumber());
                                    tuple.endTime = time;
                                    tuple.aboveStave = direction.getPlacement() == AboveBelow.ABOVE || direction.getPlacement() == null;
                                    currentPart.putInMusicGroup(tuple);
                                    break;
                                }
                            }
                        }
                    }
                }
                if (directionType.getDynamics() != null) {
                    for (var dynamics : directionType.getDynamics()) {
                        if (dynamics.getPOrPpOrPpp() != null) {
                            for (var element : dynamics.getPOrPpOrPpp()) {
                                var tuple = new MusicGroupTuple(time, MusicGroupType.DYNAMIC, getStaff(direction.getStaff()));
                                tuple.endTime = time;
                                tuple.text = element.getName().getLocalPart();
                                tuple.aboveStave = direction.getPlacement() == AboveBelow.ABOVE || direction.getPlacement() == null;
                                currentPart.putInMusicGroup(tuple);
                            }
                        }
                    }
                }
                if (directionType.getWordsOrSymbol() != null) {
                    for (var wordOrSymbol : directionType.getWordsOrSymbol()) {
                        if (wordOrSymbol instanceof FormattedTextId text && !isDirective(text)) {
                            var tuple = new MusicGroupTuple(time, MusicGroupType.TEXT, getStaff(direction.getStaff()));
                            tuple.endTime = time;
                            tuple.text = text.getValue();
                            tuple.aboveStave = direction.getPlacement() == AboveBelow.ABOVE || direction.getPlacement() == null;
                            currentPart.putInMusicGroup(tuple);
                        }
                    }
                }
            }
        }
    }

    static float parseTempoMarking(TreeMap<Float, TempoTuple> tempoMarkings, TreeMap<Float, Float> tempoChanges, float currentTempo, Direction direction, float time) {
        if (direction.getDirectionType() != null) {
            for (DirectionType directionType : direction.getDirectionType()) {
                if (directionType.getMetronome() != null) {
                    Metronome met = directionType.getMetronome();
                    uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType leftItem = uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.CROTCHET;
                    int leftDots = 0;
                    boolean seenLeft = false;
                    uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType rightItem = uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.CROTCHET;
                    int rightDots = 0;
                    boolean seenRight = false;
                    if (met.getBeatUnit() != null) {
                        for (var unit : met.getBeatUnit()) {
                            if (!seenLeft) {
                                if (unit instanceof String noteString) {
                                    leftItem = convertNoteType(noteString);
                                    seenLeft = true;
                                }
                            } else if (!seenRight) {
                                if (unit instanceof Empty) {
                                    leftDots += 1;
                                } else if (unit instanceof String noteString) {
                                    rightItem = convertNoteType(noteString);
                                    seenRight = true;
                                }
                            } else {
                                if (unit instanceof Empty) {
                                    rightDots += 1;
                                }
                            }
                        }
                    }
                    if (met.getPerMinute() != null) {
                        TempoTuple tuple = new TempoTuple(leftItem, leftDots, met.getPerMinute().getValue(), time);
                        tempoMarkings.put(time, tuple);
                        tempoChanges.put(time, tuple.bpmValue);
                        return tuple.bpmValue;
                    } else {
                        TempoTuple tuple = new TempoTuple(leftItem, leftDots, rightItem, rightDots, currentTempo, time);
                        tempoMarkings.put(time, tuple);
                        tempoChanges.put(time, tuple.bpmValue);
                        return tuple.bpmValue;
                    }
                }
            }
        }
        return currentTempo;
    }

    static void parseSlurs(TreeMap<MusicGroupType, TreeMap<Integer, MusicGroupTuple>> target, ParsingPartTuple currentPart, Note note, float time) {
        if (note.getNotations() != null) {
            for (var notation : note.getNotations()) {
                for (var s : notation.getTiedOrSlurOrTuplet()) {
                    if (s instanceof Slur slur) {
                        switch (slur.getType()) {
                            case STOP -> {
                                if (target.get(MusicGroupType.SLUR).containsKey(slur.getNumber())) {
                                    var tuple = target.get(MusicGroupType.SLUR).remove(slur.getNumber());
                                    tuple.endTime = time;
                                    currentPart.putInMusicGroup(tuple);
                                }
                            }
                            case START -> {
                                MusicGroupTuple tuple = new MusicGroupTuple(time, MusicGroupType.SLUR, getStaff(note.getStaff()));
                                tuple.voice = getVoice(note.getVoice());
                                target.get(MusicGroupType.SLUR).put(slur.getNumber(), tuple);
                            }
                        }
                    } else if (s instanceof Tuplet tuple) {

                    }
                }
            }
        }
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

    static KeySignature parseKeySignature(Key keySignature, KeySignature currentKeySignature) {
        if (keySignature != null) {
            KeySignature ret = new KeySignature();
            if (keySignature.getFifths() != null) {
                switch (keySignature.getFifths().intValue()) {
                    case -7:
                        ret.addAlteration(0, PitchName.F, uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.FLAT);
                    case -6:
                        ret.addAlteration(0, PitchName.C, uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.FLAT);
                    case -5:
                        ret.addAlteration(0, PitchName.G, uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.FLAT);
                    case -4:
                        ret.addAlteration(0, PitchName.D, uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.FLAT);
                    case -3:
                        ret.addAlteration(0, PitchName.A, uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.FLAT);
                    case -2:
                        ret.addAlteration(0, PitchName.E, uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.FLAT);
                    case -1:
                        ret.addAlteration(0, PitchName.B, uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.FLAT);
                        break;
                    case 7:
                        ret.addAlteration(0, PitchName.B, uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.SHARP);
                    case 6:
                        ret.addAlteration(0, PitchName.E, uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.SHARP);
                    case 5:
                        ret.addAlteration(0, PitchName.A, uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.SHARP);
                    case 4:
                        ret.addAlteration(0, PitchName.D, uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.SHARP);
                    case 3:
                        ret.addAlteration(0, PitchName.G, uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.SHARP);
                    case 2:
                        ret.addAlteration(0, PitchName.C, uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.SHARP);
                    case 1:
                        ret.addAlteration(0, PitchName.F, uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.SHARP);
                        break;
                    case 0:
                        for (KeySignature.Alteration alteration : currentKeySignature.getAlterations()) {
                            if (alteration.getAccidental() != uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.NATURAL) {
                                ret.addAlteration(alteration.getAlteredPitch(), uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.NATURAL);
                            }
                        }
                        break;
                }
            }
            return ret;
        }
        return null;
    }

    static int clefToLowestLineGrandStaveLine(uk.ac.cam.optimisingmusicnotation.representation.properties.Clef clef) {
        return switch (clef.getSign()) {
            case C -> pitchToGrandStaveLine(Step.C, 4);
            case F -> pitchToGrandStaveLine(Step.F, 3);
            case G, PERCUSSION -> pitchToGrandStaveLine(Step.G, 4);
            case TAB -> 0;
        } + 7 * clef.getOctaveChange() - clef.getLine();
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

    static void addPulseLines(TimeSignature time, float measureStartTime, List<PulseLineTuple> pulseLines, String measureName, TimeSignature timeSig) {
        switch (time.getBeatNum()) {
            case 2 -> {
                pulseLines.add(new PulseLineTuple(measureStartTime, measureName, 0, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4f / time.getBeatType(), measureName, 1, timeSig));
            }
            case 3 -> {
                pulseLines.add(new PulseLineTuple(measureStartTime, measureName, 0, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4f / time.getBeatType(), measureName, 1, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 2 * 4f / time.getBeatType(), measureName, 1, timeSig));
            }
            case 4 -> {
                pulseLines.add(new PulseLineTuple(measureStartTime, measureName, 0, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4f / time.getBeatType(), measureName, 1, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 2 * 4f / time.getBeatType(), measureName, 1, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 3 * 4f / time.getBeatType(), measureName, 1, timeSig));
            }
            case 5 -> {
                pulseLines.add(new PulseLineTuple(measureStartTime, measureName, 0, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 2 * 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 3 * 4f / time.getBeatType(), measureName, 1, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4 * 4f / time.getBeatType(), measureName, 2, timeSig));
            }
            case 6 -> {
                pulseLines.add(new PulseLineTuple(measureStartTime, measureName, 0, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 2 * 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 3 * 4f / time.getBeatType(), measureName, 1, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4 * 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 5 * 4f / time.getBeatType(), measureName, 2, timeSig));
            }
            case 7 -> {
                pulseLines.add(new PulseLineTuple(measureStartTime, measureName, 0, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 2 * 4f / time.getBeatType(), measureName, 1, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 3 * 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4 * 4f / time.getBeatType(), measureName, 1, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 5 * 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 6 * 4f / time.getBeatType(), measureName, 2, timeSig));
            }
            case 9 -> {
                pulseLines.add(new PulseLineTuple(measureStartTime, measureName, 0, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 2 * 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 3 * 4f / time.getBeatType(), measureName, 1, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4 * 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 5 * 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 6 * 4f / time.getBeatType(), measureName, 1, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 7 * 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 8 * 4f / time.getBeatType(), measureName, 2, timeSig));
            }
            case 12 -> {
                pulseLines.add(new PulseLineTuple(measureStartTime, measureName, 0, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 2 * 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 3 * 4f / time.getBeatType(), measureName, 1, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4 * 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 5 * 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 6 * 4f / time.getBeatType(), measureName, 1, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 7 * 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 8 * 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 9 * 4f / time.getBeatType(), measureName, 1, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 10 * 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 11 * 4f / time.getBeatType(), measureName, 2, timeSig));
            }
            default -> {
                pulseLines.add(new PulseLineTuple(measureStartTime, measureName, 0, timeSig));
                for (int i = 1; i < time.getBeatNum(); i++) {
                    pulseLines.add(new PulseLineTuple(measureStartTime + i * 4f / time.getBeatType(), measureName, 2, timeSig));
                }
            }
        }
    }

    public static Object openMXL(String input) throws IOException {
        try (FileInputStream xml = new FileInputStream(input)) {
            return Marshalling.unmarshal(xml);
        } catch (Marshalling.UnmarshallingException e) {
            String[] splitPath = input.split("[/\\\\]");
            String scoreMxlName = splitPath[splitPath.length - 1];
            String scoreXmlName = scoreMxlName.split("\\.")[0] + ".xml";
            try (ZipInputStream xml = new ZipInputStream(new FileInputStream(input))) {
                ZipEntry zipEntry = xml.getNextEntry();
                while (zipEntry != null) {
                    if(zipEntry.getName().equals(scoreXmlName) || zipEntry.getName().equals("score.xml")) {
                        return Marshalling.unmarshal(xml);
                    }
                    zipEntry = xml.getNextEntry();
                }
                return null;
            } catch (Marshalling.UnmarshallingException ex) {
                System.err.println("Problem with xml file.");
                throw new RuntimeException(ex);
            }
        }
    }

    private Parser() {}

    public static void main(String[] args) {
        String target = "test_scores/TestScore2.mxl";
        if (args.length > 0) {
            target = args[0];
        }

        Object mxl;
        try {
            mxl = Parser.openMXL(target);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
            targetPart = Integer.parseInt(args[1]);
        }

        if (targetPart == -1) {
            for (Part part : score.getParts()) {
                try (PdfWriter writer = new PdfWriter(outDir + outTarget + "_" + part.getName() + ".pdf")) {
                    PdfDocument pdf = new PdfDocument(writer);
                    PageSize ps = PageSize.A4;
                    pdf.addNewPage(ps);

                    PdfMusicCanvas canvas = new PdfMusicCanvas(pdf, part.getMaxWidth(), part.getMinOffset());
                    part.draw(canvas, score.getWorkTitle(), score.getComposer());
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

                Part testPart = score.getParts().get(targetPart);
                PdfMusicCanvas canvas = new PdfMusicCanvas(pdf, testPart.getMaxWidth(), testPart.getMinOffset());
                testPart.draw(canvas, score.getWorkTitle(), score.getComposer());
                pdf.close();
            }
            catch (IOException e) {
                System.err.println("Error while creating PDF: ");
                e.printStackTrace();
            }
        }
    }
}
