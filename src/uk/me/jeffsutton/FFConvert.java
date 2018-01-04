package uk.me.jeffsutton;

import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FFConvert {

    static int aCount = 0;
    static int vCount = 0;
    static int sCount = 0;

    static List<String> commands = new ArrayList<>();

    static boolean quietMode = false;

    static String vCodec = "libx264";
    static String vPreset = "slow";
    static String vProfile = "high";
    static String vLevel = "4.0";

    static String aCodec = "aac";
    static String aBitrate = "192k";
    static String vBitrate = "3M";

    static boolean downmix = false;

    static boolean force = false;

    static String container = "mp4";

    static String vScale = "None";
    static String vAspect = "None";

    static boolean deinterlace = false;
    static String deIntFilter = "yadif";

    static String crf = "21";

    static boolean videoCopy = false;
    static boolean audioCopy = false;

    public static void main(String[] args) {
        File f = new File(args[0]);

        for (String arg : args) {
            if (arg.equals("-quiet")) {
                quietMode = true;
            }
        }

        if (!quietMode) {
            System.out.println("FFConverter. (c) 2016-2017 Jeff Sutton.");
            System.out.println("Arg:\t" + f.getAbsolutePath());
        }

        if (f != null && f.exists() && f.isFile() && !f.isDirectory()) {
            try {
                processFile(f.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (f != null && f.exists() && f.isDirectory()) {
            File[] fa = f.listFiles();
            Arrays.sort(fa);
            for (File foo : fa) {
                if (foo.getName().endsWith(".mkv") || foo.getName().endsWith(".avi") || foo.getName().endsWith(".mp4") || foo.getName().endsWith(".m4v")  || foo.getName().endsWith(".ts"))
                    try {
                        processFile(foo.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }
        System.out.println("\n\n");
        for (String s : commands) {
            System.out.println(s);
        }
    }

    public static String getCommands(File f, String vCod, String vPre, String vPro, String vLev, String aCod,
                                     String aBit, boolean down, String cont, boolean forc, String vBr, String scale,
                                     String aspect, boolean dI, String dIF, String cr, boolean vCopy, boolean ac3) {
        commands = new ArrayList<>();
        quietMode = false;
        vCodec = vCod;
        vPreset = vPre;
        vProfile = vPro;
        vLevel = vLev;
        aCodec = aCod;
        aBitrate = aBit;
        downmix = down;
        container = cont;
        force = forc;
        vBitrate = vBr;
        vScale = scale;
        vAspect = aspect;
        deinterlace = dI;
        deIntFilter = dIF;
        crf = cr;

        videoCopy = vCopy;
        audioCopy = ac3;

        if (f != null && f.exists() && f.isFile() && !f.isDirectory()) {
            try {
                processFile(f.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (f != null && f.exists() && f.isDirectory()) {
            File[] fa = f.listFiles();
            Arrays.sort(fa);
            for (File foo : fa) {
                if (foo.getName().endsWith(".mkv") || foo.getName().endsWith(".avi") || foo.getName().endsWith(".mp4") || foo.getName().endsWith(".m4v") || foo.getName().endsWith(".ts"))
                    try {
                        processFile(foo.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }
        StringBuilder output = new StringBuilder();
        for (String s : commands) {
            output.append(s).append("\n");
        }

        return output.toString();
    }

    private static void processFile(String filename) throws IOException {
        FFprobe probe = new FFprobe();
        aCount = 0;
        vCount = 0;
        sCount = 0;
        boolean process = false;
        try {
            FFmpegProbeResult results = probe.probe(filename);
            if (!quietMode) {
                System.out.println("\n\nFile:\t\t" + results.getFormat().filename);
                System.out.println("Format:\t\t" + results.getFormat().format_name);
            }
            StringBuilder cmd = new StringBuilder();
            StringBuilder additionalCommands = new StringBuilder();
            cmd.append("ffmpeg -i \"").append(results.getFormat().filename).append("\"");

            if (!vScale.equalsIgnoreCase("none") || !vAspect.equalsIgnoreCase("none") || deinterlace) {
                cmd.append(" -filter_complex \"");
                if (deinterlace) {
                    if (deIntFilter.equalsIgnoreCase("yadif (auto)")) {
                        cmd.append("yadif=1:-1:0");
                    }
                    if (!vScale.equalsIgnoreCase("none") || (!vAspect.equalsIgnoreCase("none"))) {
                        cmd.append(",");
                    }
                }
                if (!vScale.equalsIgnoreCase("none")) {
                    cmd.append("scale=" + vScale);
                    if (!vScale.equalsIgnoreCase("none") && (!vAspect.equalsIgnoreCase("none"))) {
                        cmd.append(",");
                    }
                }
                if (!vAspect.equalsIgnoreCase("none")) {
                    cmd.append("setdar=" + vAspect);
                }

                cmd.append("\" ");
            }

            for (FFmpegStream stream : results.getStreams()) {

                if (!quietMode) {
                    System.out.println("Stream " + stream.index + ":\t" + stream.codec_name + " \t" + stream.level + "\t " + stream.is_avc + "\t " + stream.codec_tag_string + "\t " + stream.sample_aspect_ratio);
                }
                if (stream.codec_name.equalsIgnoreCase("h264") || stream.codec_name.equalsIgnoreCase("hevc")
                        || stream.codec_name.equalsIgnoreCase("mpeg2video") || stream.codec_name.equalsIgnoreCase("divx")
                        || stream.codec_name.equalsIgnoreCase("xvid") || stream.codec_name.equalsIgnoreCase("mpeg4")) {
                    if (force || stream.level > 40 || stream.codec_name.equalsIgnoreCase("mpeg2video")
                            || stream.codec_name.equalsIgnoreCase("divx")
                            || stream.codec_name.equalsIgnoreCase("xvid") || stream.codec_name.equalsIgnoreCase("mpeg4")) {
                        process = true;
                    }
                    cmd.append(buildVideoMap(stream));
                } else if (stream.codec_name.equalsIgnoreCase("aac") || stream.codec_name.equalsIgnoreCase("ac3")|| stream.codec_name.equalsIgnoreCase("eac3")
                        || stream.codec_name.equalsIgnoreCase("mp3") || stream.codec_name.equalsIgnoreCase("dca")  || stream.codec_name.equalsIgnoreCase("flac")) {
                    if (stream.codec_name.equalsIgnoreCase("dtc") || stream.codec_name.equalsIgnoreCase("ac3")
                            || stream.codec_name.equalsIgnoreCase("eac3")  || stream.codec_name.equalsIgnoreCase("dca")  || stream.codec_name.equalsIgnoreCase("flac")) {
                        process = true;
                    }
                    if (downmix && aCount == 0 && stream.channels > 2) {
                        process = true;
                    }
                    cmd.append(buildAudioMap(stream));
                } else if (stream.codec_name.equalsIgnoreCase("dvdsub") || stream.codec_name.equalsIgnoreCase("subrip") || stream.codec_name.equalsIgnoreCase("mov_text") || stream.codec_name.equalsIgnoreCase("srt")) {
                    cmd.append(buildSubtitleMap(stream));
                    if (stream.codec_name.equalsIgnoreCase("mov_text") || stream.codec_name.equalsIgnoreCase("srt")) {
                        // Do we want to extract these subtitles using MP4Box?
                        additionalCommands.append(String.format("MP4Box -srt %1$s \"%2$s\"\n", stream.index, results.getFormat().filename));
                    }
                } else if (stream.codec_name.equalsIgnoreCase("png")) {
                    //cmd.append(buildPNGMap(stream));
                }
            }
            if (container.equalsIgnoreCase("mkv")) {
                cmd.append(" -f matroska ");
            }

            String outputFile = results.getFormat().filename;

            cmd.append(" -movflags faststart \"").append(results.getFormat().filename).append(".1.").append(container + "\"");
            if (process)
                commands.add(cmd.toString());
            if (additionalCommands.length() > 0) {
                commands.add(additionalCommands.toString().trim());
            }
        } catch (Exception eek) {

        }
    }

    private static String buildVideoMap(FFmpegStream stream) {
        StringBuilder stringBuilder = new StringBuilder();
        if (videoCopy) {
            stringBuilder.append(String.format(" -map 0:%1$s -c:v:%2$s copy", stream.index, vCount));
        } else if (vCodec.equalsIgnoreCase("libx264")) {
            if (!force && stream.codec_name.equalsIgnoreCase("h264") && stream.level < 41) {
                stringBuilder.append(String.format(" -map 0:%1$s -c:v:%2$s copy", stream.index, vCount));
            } else {
                stringBuilder.append(String.format(" -map 0:%1$s -c:v:%2$s " + vCodec + " -preset " + vPreset + " -threads 0 -crf " + crf + " -maxrate " + vBitrate + " -bufsize 3M -strict experimental -profile:v " + vProfile + " -level " + vLevel, stream.index, vCount));
            }
            vCount++;
        } else if (vCodec.equalsIgnoreCase("libx265") || vCodec.equalsIgnoreCase("hevc")) {
            if (!force && (stream.codec_name.equalsIgnoreCase("h265") || stream.codec_name.equalsIgnoreCase("hevc")) && stream.level < 41) {
                stringBuilder.append(String.format(" -map 0:%1$s -c:v:%2$s copy", stream.index, vCount));
            } else {
                stringBuilder.append(String.format(" -map 0:%1$s -c:v:%2$s " + vCodec + " -preset " + vPreset + " -threads 0 -x265-params crf="+crf+":ref=5:rd=5:psy-rd=2:no-sao=1:no-strong-intra-smoothing=1 -maxrate " + vBitrate + " -bufsize 3M -strict experimental " + " -level " + vLevel, stream.index, vCount));
            }
            vCount++;
        }
        return stringBuilder.toString();
    }

    private static String buildAudioMap(FFmpegStream stream) {
        StringBuilder stringBuilder = new StringBuilder();
        System.out.println("Audio channels: " + stream.channels + " layout: " + stream.channel_layout);
        if (audioCopy) {
            if (stream.codec_name.equalsIgnoreCase("ac3")) {
                stringBuilder.append(String.format(" -map 0:%1$s -c:a:%2$s " + aCodec + " -b:a:%2$s " + aBitrate + " -strict experimental -metadata:s:a:%2$s language=eng -metadata:s:a:%2$s title=\"" + stream.channel_layout + "\"", stream.index, aCount));
            } else {
                stringBuilder.append(String.format(" -map 0:%1$s -c:a:%2$s copy", stream.index, aCount));
            }
            aCount++;
            return stringBuilder.toString();
        } else if (stream.channels > 2 && downmix && aCount == 0) {
            stringBuilder.append(String.format(" -map 0:%1$s -ac:%2$s 2 -c:a:%2$s " + aCodec + " -b:a:%2$s " + aBitrate + " -strict experimental -filter:a:%2$s \"pan=stereo|FL=FC+0.30*FL+0.30*BL|FR=FC+0.30*FR+0.30*BR\" -metadata:s:a:%2$s language=eng -metadata:s:a:%2$s title=\"Stereo\"", stream.index, aCount));
            aCount++;
        }
        stringBuilder.append(String.format(" -map 0:%1$s -c:a:%2$s " + aCodec + " -b:a:%2$s " + aBitrate + " -strict experimental -metadata:s:a:%2$s language=eng -metadata:s:a:%2$s title=\"" + stream.channel_layout + "\"", stream.index, aCount));
        aCount++;
        return stringBuilder.toString();
    }

    private static String buildSubtitleMap(FFmpegStream stream) {
        StringBuilder stringBuilder = new StringBuilder();
        String sType;
        if (stream.codec_name.equalsIgnoreCase("dvdsub")) {
            sType = "dvd_subtitle";
        } else {
            if (container.equalsIgnoreCase("mkv")) {
                sType = "copy";
            } else {
                sType = "mov_text";
            }
        }
        stringBuilder.append(String.format(" -map 0:%1$s -c:s:%2$s %3$s", stream.index, sCount, sType));
        sCount++;
        return stringBuilder.toString();
    }

    private static String buildPNGMap(FFmpegStream stream) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format(" -map 0:%1$s -c:v:%2$s png", stream.index, vCount));
        vCount++;
        return stringBuilder.toString();
    }
}
