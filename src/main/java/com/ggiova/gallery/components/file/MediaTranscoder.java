package com.ggiova.gallery.components.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Component
public class MediaTranscoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(MediaTranscoder.class);
    private static final int TIMEOUT_MINUTES_IMAGE = 1;
    private static final int TIMEOUT_MINUTES_VIDEO = 15;

    public boolean createVideoThumbnail(Path source, Path target) {
        return this.run(
                target.toString(),
                TIMEOUT_MINUTES_VIDEO,

                "ffmpeg", "-nostdin", "-y",
                "-i", source.toString(),
                "-map", "0:v:0",
                "-vf", "scale=-2:'min(480,ih)'",          // max 480p, never upscale
                "-r", "24",
                "-c:v", "libx264", "-crf", "32", "-preset", "veryfast",
                "-pix_fmt", "yuv420p",
                "-an",
                "-f", "mp4", "-movflags", "+faststart",
                target.toString()
        );
    }

    public boolean createImageThumbnail(Path source, Path target) {
        return this.run(
                target.toString(),
                TIMEOUT_MINUTES_IMAGE,

                "ffmpeg",
                "-nostdin",
                "-y",
                "-i", source.toString(),
                "-frames:v", "1",
                "-vf", "scale='min(500,iw)':'min(500,ih)':force_original_aspect_ratio=decrease",
                "-c:v", "mjpeg",
                "-q:v", "5",
                "-f", "image2",
                target.toString()
        );
    }

    private boolean run(String name, int timeoutMinutes, String... cmd) {
        LOGGER.info("Creating thumbnail for {}", name);
        try {
            Process p = new ProcessBuilder(cmd)
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start();
            if (! p.waitFor(timeoutMinutes, TimeUnit.MINUTES)) {
                p.destroyForcibly();
                LOGGER.error("ffmpeg timed out on {}: {}", name, cmd);
                return false;
            }
            if (p.exitValue() != 0) {
                LOGGER.error("ffmpeg exited on {} with {}: {}", name, p.exitValue(), cmd);
                return false;
            }
            return true;
        } catch (IOException e) {
            LOGGER.error("Failed to run ffmpeg", e);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}