package com.ggiova.gallery.components.file;

import com.ggiova.gallery.util.Doubles;
import com.ggiova.gallery.util.Integers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Reads width, height and duration of a video by running {@code ffprobe}.
 * Requires {@code ffprobe} (part of {@code ffmpeg}) to be installed and on the PATH.
 */
@Component
public class VideoInspector {
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoInspector.class);
    private static final long TIMEOUT_SECONDS = 15;

    public MediaResponse inspect(Path path, String mime) {
        Process process = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffprobe",
                    "-v", "error",
                    "-protocol_whitelist", "file",
                    "-select_streams", "v:0",
                    "-show_entries", "stream=width,height:format=duration",
                    "-of", "default=noprint_wrappers=1",
                    "-i", path.toString()
            );
            pb.redirectError(ProcessBuilder.Redirect.DISCARD);
            process = pb.start();

            if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return new MediaResponse.Failed("ffprobe timed out");
            }
            if (process.exitValue() != 0) {
                LOGGER.warn("ffprobe exited with {} for {}", process.exitValue(), path.getFileName());
                return new MediaResponse.UnknownMedia();
            }

            // The output is a few short lines, so reading after exit is safe.
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> values = new HashMap<>();
            for (String line : output.split("\\R")) {
                int eq = line.indexOf('=');
                if (eq > 0) {
                    values.put(line.substring(0, eq).trim(), line.substring(eq + 1).trim());
                }
            }

            Optional<Integer> width = Integers.parse(values.get("width"));
            Optional<Integer> height = Integers.parse(values.get("height"));
            if (width.isEmpty() || height.isEmpty()) {
                return new MediaResponse.UnknownMedia(); // no video stream
            }

            Long durationMs = Doubles.parse(values.get("duration"))
                                     .map(d -> Math.round(d * 1000))
                                     .orElse(null);
            return new MediaResponse.MediaInfo(mime, width.get(), height.get(), durationMs);
        } catch (IOException e) {
            LOGGER.error("Could not run ffprobe (is it installed?)", e);
            return new MediaResponse.Failed(e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            return new MediaResponse.Failed("Interrupted while inspecting video");
        }
    }
}