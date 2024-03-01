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

    public static Score parseToScore(Object mxl) {
        if (mxl instanceof ScorePartwise partwise) {
            TreeMap<Float, Float> newlines = new TreeMap<>() {{ put(0f, 0f); }};
            TreeSet<Float> newSections = new TreeSet<>() {{ add(0f); }};
            TreeMap<String, Part> parts = new TreeMap<>();

            TreeMap<Float, TempoTuple> tempoMarkings = new TreeMap<>();

            TreeMap<Float, Float> tempoChanges = new TreeMap<>() {{ put(0f, startBpm); }};

            TreeMap<Float, List<TimeSignature.BeatTuple>> beatChanges = new TreeMap<>();

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
                            float newlineOffset = 0;

                            if (measureTime + offset != 0) {
                                newlineOffset = measureTime + offset - measureLength;
                            }
                            final KeySignature tempKeySig = currentKeySignature;
                            parseMusicDirective(musicGroupTuples, currentPart, direction,
                                    measureStartTime + measureTime + offset, newlineOffset,
                                    beatChanges, currentTimeSignature,
                                    addNewline, addNewSection, (voice, time) -> {
                                        currentPart.putInArtisticWhitespace(getStaff(direction.getStaff()), voice, time);
                                        var whitespace = new BeamGroupTuple();
                                        whitespace.startTime = (time) - RenderingConfiguration.artisticWhitespaceWidth;
                                        whitespace.endTime = (time);
                                        var restChord = new ChordTuple(whitespace.startTime, 0, tempKeySig);
                                        restChord.duration = RenderingConfiguration.artisticWhitespaceWidth;
                                        var restNote = new Note();
                                        restNote.setRest(new org.audiveris.proxymusic.Rest());
                                        restChord.notes.add(restNote);
                                        whitespace.addChord(restChord);
                                        whitespace.staff = getStaff(direction.getStaff());
                                        whitespace.voice = voice;
                                        currentPart.putInBeamGroup(whitespace); });
                            currentTempo = parseTempoMarking(tempoMarkings, tempoChanges, currentTempo, direction, measureStartTime + measureTime + offset);
                            currentPart.directions.put(measureStartTime + measureTime + offset, direction);
                        }
                    }

                    var beatChange = beatChanges.lowerEntry(measureStartTime + measureTime);
                    if (beatChange != null && beatChange.getKey() >= measureStartTime) {
                        currentTimeSignature.setBeatPattern(beatChange.getValue());
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
                    TreeSet<Float> beamBreaks = new TreeSet<>(newlines.keySet());

                    if (part.getValue().artisticWhitespace.containsKey(staffEntry.getKey())
                            && part.getValue().artisticWhitespace.get(staffEntry.getKey()).containsKey(voiceEntry.getKey())) {
                        for (var time : part.getValue().artisticWhitespace.get(staffEntry.getKey()).get(voiceEntry.getKey())) {
                            float newTime = normaliseTime(time, integratedTime);
                            beamBreaks.add(newTime);
                        }
                    }

                    for (BeamGroupTuple beam : voiceEntry.getValue().values()) {
                        if (beam.isRest()) {
                            beam.splitToInstantiatedRestTuple(newlines, lineIndices, integratedTime, partLines.get(part.getKey()));
                        } else {
                            beam.splitToInstantiatedBeamGroupTuple(beamBreaks, newlines, lineIndices, integratedTime, partLines.get(part.getKey()));
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

            List<HashMap<Integer, HashMap<Integer, TreeMap<Float, Chord>>>> chords = new ArrayList<>();
            List<HashMap<Integer, HashMap<Integer, TreeMap<Float, Whitespace>>>> rests = new ArrayList<>();
            List<HashMap<Integer, HashMap<Integer, Map<Chord, Integer>>>> needsFlag = new ArrayList<>();
            List<HashMap<Integer, HashMap<Integer, Map<Chord, Integer>>>> needsBeamlet = new ArrayList<>();
            for (int i = 0; i < part.getValue().size(); ++i) {

                chords.add(new HashMap<>());
                rests.add(new HashMap<>());
                needsFlag.add(new HashMap<>());
                needsBeamlet.add(new HashMap<>());

                Line tempLine = new Line(new ArrayList<>(), newlinesList.get(i), lineLengths.get(i), lineOffsets.get(i), i);
                finalLines.get(part.getKey()).add(new InstantiatedLineTuple(newlinesList.get(i), tempLine));
                for (var staffEntry : part.getValue().get(i).rests.entrySet()) {
                    Util.ensureCapacity(tempLine.getStaves(), Stave::new, staffEntry.getKey() - 1);
                    Util.ensureKey(chords.get(i), HashMap::new, staffEntry.getKey());
                    Util.ensureKey(rests.get(i), HashMap::new, staffEntry.getKey());
                    Util.ensureKey(needsFlag.get(i), HashMap::new, staffEntry.getKey());
                    Util.ensureKey(needsBeamlet.get(i), HashMap::new, staffEntry.getKey());
                    for (var voiceEntry : staffEntry.getValue().entrySet()) {
                        Util.ensureKey(chords.get(i).get(staffEntry.getKey()), TreeMap::new, voiceEntry.getKey());
                        Util.ensureKey(rests.get(i).get(staffEntry.getKey()), TreeMap::new, voiceEntry.getKey());
                        Util.ensureKey(needsFlag.get(i).get(staffEntry.getKey()), HashMap::new, voiceEntry.getKey());
                        Util.ensureKey(needsBeamlet.get(i).get(staffEntry.getKey()), HashMap::new, voiceEntry.getKey());
                        var fusedRests = InstantiatedRestTuple.fuseRestTuples(voiceEntry.getValue().values().stream().toList());
                        for (InstantiatedRestTuple restTuple : fusedRests) {
                            tempLine.getStaves().get(staffEntry.getKey() - 1).addWhiteSpace(restTuple.toRest(tempLine, rests.get(i)));
                        }
                    }
                }

                for (var staffEntry : part.getValue().get(i).notes.entrySet()) {
                    Util.ensureCapacity(tempLine.getStaves(), Stave::new, staffEntry.getKey() - 1);
                    Util.ensureKey(chords.get(i), HashMap::new, staffEntry.getKey());
                    Util.ensureKey(rests.get(i), HashMap::new, staffEntry.getKey());
                    Util.ensureKey(needsFlag.get(i), HashMap::new, staffEntry.getKey());
                    Util.ensureKey(needsBeamlet.get(i), HashMap::new, staffEntry.getKey());
                    for (var voiceEntry : staffEntry.getValue().entrySet()) {
                        Util.ensureKey(chords.get(i).get(staffEntry.getKey()), TreeMap::new, voiceEntry.getKey());
                        Util.ensureKey(rests.get(i).get(staffEntry.getKey()), TreeMap::new, voiceEntry.getKey());
                        Util.ensureKey(needsFlag.get(i).get(staffEntry.getKey()), HashMap::new, voiceEntry.getKey());
                        Util.ensureKey(needsBeamlet.get(i).get(staffEntry.getKey()), HashMap::new, voiceEntry.getKey());
                        for (var beamTuple : voiceEntry.getValue().values()) {
                            beamTuple.addToAverager(averager);
                            tempLine.getStaves().get(staffEntry.getKey() - 1).addStaveElement(beamTuple.toBeamGroup(tempLine, chords.get(i), needsFlag.get(i), needsBeamlet.get(i)));
                        }
                    }
                }

                for (InstantiatedPulseLineTuple pulseTuple : part.getValue().get(i).pulses) {
                    tempLine.addPulseLine(pulseTuple.toPulseLine(tempLine));
                }

                for (var staffEntry : part.getValue().get(i).musicGroups.entrySet()) {
                    Util.ensureCapacity(tempLine.getStaves(), Stave::new, staffEntry.getKey() - 1);
                    for (InstantiatedMusicGroupTuple musicGroupTuple : staffEntry.getValue()) {
                        tempLine.getStaves().get(staffEntry.getKey() - 1).addMusicGroup(musicGroupTuple.toMusicGroup(tempLine, chords.get(i)));
                    }
                }

                for (InstantiatedTempoTuple tempoTuple : part.getValue().get(i).tempoMarkings) {
                    tempLine.getStaves().get(0).addMusicGroup(tempoTuple.toMusicGroup(tempLine, chords.get(i)));
                }
            }
            parsingPart.get(part.getKey()).upwardsStems = averager.getAverageStaveLine() < 4;

            for (var lineEntry : chords) {
                for (var staffEntry : lineEntry.entrySet()) {
                    for (var voiceEntry : staffEntry.getValue().entrySet()) {
                        for (var chordEntry : voiceEntry.getValue().entrySet()) {
                            if (chordEntry.getKey() > voiceEntry.getValue().firstKey()) {
                                chordEntry.getValue().removeTiesTo();
                            }
                        }
                    }
                }
            }

            HashMap<Integer, TreeMap<Float, Chord>> emptyStaffChords = new HashMap<>();
            HashMap<Integer, TreeMap<Float, Whitespace>> emptyStaffRests = new HashMap<>();
            TreeMap<Float, Chord> emptyVoiceChords = new TreeMap<>();
            TreeMap<Float, Whitespace> emptyVoiceRests = new TreeMap<>();
            for (int i = 0; i < needsFlag.size(); ++i) {
                for (var staffEntry : needsFlag.get(i).entrySet()) {
                    for (var voiceEntry : staffEntry.getValue().entrySet()) {
                        for (var entry : voiceEntry.getValue().entrySet()) {
                            Line tempLine = finalLines.get(part.getKey()).get(i).line;
                            boolean prevLine = false;
                            var preEntry = chords.get(i).get(staffEntry.getKey()).get(voiceEntry.getKey()).lowerEntry(entry.getKey().getCrotchetsIntoLine());
                            var preChord = preEntry == null ? null : preEntry.getValue();
                            var preRestEntry = rests.get(i).get(staffEntry.getKey()).get(voiceEntry.getKey()).floorEntry(entry.getKey().getCrotchetsIntoLine());
                            var preRest = preRestEntry == null ? null : preRestEntry.getValue();
                            if (preChord == null && i > 0) {
                                preEntry = chords.get(i-1).getOrDefault(staffEntry.getKey(), emptyStaffChords).getOrDefault(voiceEntry.getKey(), emptyVoiceChords).lastEntry();
                                if (preEntry != null) {
                                    preChord = preEntry.getValue().moveToNextLine(tempLine);
                                    prevLine = true;
                                }
                            }
                            if (preRest == null && i > 0) {
                                preRestEntry = rests.get(i-1).getOrDefault(staffEntry.getKey(), emptyStaffRests).getOrDefault(voiceEntry.getKey(), emptyVoiceRests).lastEntry();
                                if (preRestEntry != null) {
                                    preRest = preRestEntry.getValue().moveToNextLine(tempLine);
                                }
                            }
                            if (preRest != null && preChord != null && preRest.getEndCrotchets() > preChord.getCrotchetsIntoLine()) {
                                preChord = null;
                            }
                            if (preChord != null && prevLine) {
                                tempLine.getStaves().get(staffEntry.getKey() - 1).addStaveElement(preChord);
                            }
                            tempLine.getStaves().get(staffEntry.getKey() - 1).addMusicGroup(new Flag(preChord, entry.getKey(), tempLine, entry.getValue()));
                        }
                    }
                }
            }

            for (int i = 0; i < needsFlag.size(); ++i) {
                for (var staffEntry : needsBeamlet.get(i).entrySet()) {
                    for (var voiceEntry : staffEntry.getValue().entrySet()) {
                        for (var entry : voiceEntry.getValue().entrySet()) {
                            Line tempLine = finalLines.get(part.getKey()).get(i).line;
                            boolean nextLine = false;
                            var postEntry = chords.get(i).get(staffEntry.getKey()).get(voiceEntry.getKey()).higherEntry(entry.getKey().getCrotchetsIntoLine());
                            var postChord = postEntry == null ? null : postEntry.getValue();
                            var postRestEntry = rests.get(i).get(staffEntry.getKey()).get(voiceEntry.getKey()).ceilingEntry(entry.getKey().getCrotchetsIntoLine());
                            var postRest = postRestEntry == null ? null : postRestEntry.getValue();
                            if (postChord == null && i < chords.size() - 1) {
                                postEntry = chords.get(i+1).getOrDefault(staffEntry.getKey(), emptyStaffChords).getOrDefault(voiceEntry.getKey(), emptyVoiceChords).firstEntry();
                                if (postEntry != null) {
                                    postChord = postEntry.getValue().moveToPrevLine(tempLine);
                                    nextLine = true;
                                }
                            }
                            if (postRest == null && i < chords.size() - 1) {
                                postRestEntry = rests.get(i+1).getOrDefault(staffEntry.getKey(), emptyStaffRests).getOrDefault(voiceEntry.getKey(), emptyVoiceRests).firstEntry();
                                if (postRestEntry != null) {
                                    postRest = postRestEntry.getValue().moveToPrevLine(tempLine);
                                }
                            }
                            if (postRest != null && postChord != null && postRest.getStartCrotchets() < postChord.getCrotchetsIntoLine()) {
                                postChord = null;
                            }
                            if (postChord != null && nextLine) {
                                tempLine.getStaves().get(staffEntry.getKey() - 1).addStaveElement(postChord);
                            }
                            tempLine.getStaves().get(staffEntry.getKey() - 1).addMusicGroup(new Beamlet(postChord, entry.getKey(), tempLine, entry.getValue()));
                        }
                    }
                }
            }
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

    static boolean isNewline(FormattedTextId formattedText, float time, float offset, BiConsumer<Float, Float> addNewline) {
        if (formattedText.getValue().equals("n") && formattedText.getEnclosure() == EnclosureShape.RECTANGLE) {
            addNewline.accept(time, offset);
            return true;
        } else if (formattedText.getValue().equals("\\n")) {
            addNewline.accept(time, offset);
            return true;
        }
        return false;
    }

    static boolean isNewSection(FormattedTextId formattedText, float time, float offset, BiConsumer<Float, Float> addNewSection) {
        if (formattedText.getValue().equals("s") && formattedText.getEnclosure() == EnclosureShape.RECTANGLE) {
            addNewSection.accept(time, offset);
            return true;
        } else if (formattedText.getValue().equals("\\s")) {
            addNewSection.accept(time, offset);
            return true;
        }
        return false;
    }

    static boolean isPulseDirective(FormattedTextId formattedText, float time, TreeMap<Float, List<TimeSignature.BeatTuple>> beatChanges, TimeSignature timeSig) {
        String text = formattedText.getValue();
        String pulseRules;
        if (text.startsWith("t") && formattedText.getEnclosure() == EnclosureShape.RECTANGLE) {
            pulseRules = text.substring(1);
        } else if (text.startsWith("\\t")) {
            pulseRules = text.substring(2);
        } else {
            return false;
        }
        try {
            var beats = new ArrayList<TimeSignature.BeatTuple>();
            String[] beatInfos = pulseRules.split("\\|");
            for (var beatInfo : beatInfos) {
                String[] splitBeat = beatInfo.split("/");
                if (splitBeat.length == 1) {
                    if (splitBeat[0].equals("")) {
                        return false;
                    } else {
                        beats.add(new TimeSignature.BeatTuple(Integer.parseInt(splitBeat[0]),
                                timeSig.getBeatType(), 1));
                    }
                } else if (splitBeat.length == 2) {
                    beats.add(new TimeSignature.BeatTuple(Integer.parseInt(splitBeat[0]),
                            timeSig.getBeatType(),
                            Integer.parseInt(splitBeat[1])));
                } else if (splitBeat.length == 3) {
                    beats.add(new TimeSignature.BeatTuple(Integer.parseInt(splitBeat[0]),
                            Integer.parseInt(splitBeat[2]),
                            Integer.parseInt(splitBeat[1])));
                }
            }
            beatChanges.put(time, beats);
            timeSig.setBeatPattern(beats);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    static boolean isArtisticWhitespace(FormattedTextId formattedText, float time, BiConsumer<Integer, Float> addArtisticWhitespace) {
        String text = formattedText.getValue();
        if (text.startsWith("w") && formattedText.getEnclosure() == EnclosureShape.RECTANGLE) {
            try {
                if (text.length() == 1) {
                    addArtisticWhitespace.accept(1, time);
                    return true;
                }
                addArtisticWhitespace.accept(Integer.parseInt(formattedText.getValue().substring(1)), time);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        if (text.startsWith(("\\w"))) {
            try {
                if (text.length() == 2) {
                    addArtisticWhitespace.accept(1, time);
                    return true;
                }
                addArtisticWhitespace.accept(Integer.parseInt(formattedText.getValue().substring(2)), time);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    static List<MusicGroupType> wedgeGroups = new ArrayList<>(2) {{ add(MusicGroupType.DIM); add(MusicGroupType.CRESC); }};
    static void parseMusicDirective(TreeMap<MusicGroupType, TreeMap<Integer, MusicGroupTuple>> target, ParsingPartTuple currentPart, Direction direction,
                                    float time, float newlineOffset,
                                    TreeMap<Float, List<TimeSignature.BeatTuple>> beatChanges, TimeSignature timeSig,
                                    BiConsumer<Float, Float> addNewline, BiConsumer<Float, Float> addNewSection, BiConsumer<Integer, Float> addArtisticWhitespace) {
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
                        if (wordOrSymbol instanceof FormattedTextId text) {
                            if (!isPulseDirective(text, time, beatChanges, timeSig)
                                    && !isNewline(text, time, newlineOffset, addNewline)
                                    && !isNewSection(text, time, newlineOffset, addNewSection)
                                    && !isArtisticWhitespace(text, time, addArtisticWhitespace)) {
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
            ret.setBeatPatternToDefault();
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

    static void addPulseLines(TimeSignature timeSig, float measureStartTime, List<PulseLineTuple> pulseLines, String measureName, TimeSignature displaySig) {
        float fromStartTime = 0;
        for (var beatTuple : timeSig.getBeatPattern()) {
            float duration = beatTuple.durationInUnits() * 4f / beatTuple.beatType();
            if (fromStartTime == 0) {
                pulseLines.add(new PulseLineTuple(measureStartTime, measureName, 0, displaySig));
            } else {
                pulseLines.add(new PulseLineTuple(measureStartTime + fromStartTime, measureName, 1, displaySig));
            }
            for (int i = 0; i < beatTuple.subBeats(); ++i) {
                pulseLines.add(new PulseLineTuple(measureStartTime + fromStartTime + duration * i / beatTuple.subBeats(), measureName, 2, displaySig));
            }
            fromStartTime += duration;
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
