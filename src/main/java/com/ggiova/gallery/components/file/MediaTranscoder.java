package com.ggiova.gallery.components.file;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class MediaTranscoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(MediaTranscoder.class);
    private static final int TIMEOUT_MINUTES_IMAGE = 1;
    private static final int TIMEOUT_MINUTES_VIDEO = 15;
    private static final double FRAME_SEEK_SECONDS = 0.1d;
    private static final int THUMBNAIL_VIDEO_SIZE = 240;
    private static final int THUMBNAIL_MAX_IMAGE_SIZE = 400;
    private static final int LOW_RES_MAX_IMAGE_SIZE = 200;

    public Optional<String> createVideoThumbnail(Path source, Path target) {
        boolean run = this.run(
                target.toString(),
                TIMEOUT_MINUTES_VIDEO,

                "ffmpeg", "-nostdin", "-y",
                "-i", source.toString(),
                "-map", "0:v:0",
                "-vf", "scale=-2:'min(" + THUMBNAIL_VIDEO_SIZE + ",ih)'",
                "-r", "24",
                "-c:v", "libx264", "-crf", "32", "-preset", "veryfast",
                "-pix_fmt", "yuv420p",
                "-an",
                "-f", "mp4", "-movflags", "+faststart",
                target.toString()
        );
        if (run) {
            return Optional.of("video/mp4");
        } else {
            return Optional.empty();
        }
    }

    public Optional<String> createImageThumbnail(Path source, Path target) {
        return this.createImage(source, target, THUMBNAIL_MAX_IMAGE_SIZE);
    }

    public Optional<String> createVideoLowRes(Path source, Path target) {
        boolean run = this.run(
                target.toString(),
                TIMEOUT_MINUTES_IMAGE,

                "ffmpeg", "-nostdin", "-y",
                "-ss", String.valueOf(FRAME_SEEK_SECONDS),
                "-i", source.toString(),
                "-map", "0:v:0",
                "-frames:v", "1",
                "-vf", "scale='min(" + LOW_RES_MAX_IMAGE_SIZE + ",iw)':'min(" + LOW_RES_MAX_IMAGE_SIZE + ",ih)':force_original_aspect_ratio=decrease",
                "-c:v", "mjpeg",
                "-q:v", "5",
                "-f", "image2",
                target.toString()
        );
        if (run) {
            return Optional.of("image/jpeg");
        } else {
            return Optional.empty();
        }
    }

    public Optional<String> createImageLowRes(Path source, Path target) {
        return this.createImage(source, target, LOW_RES_MAX_IMAGE_SIZE);
    }

    private Optional<String> createImage(Path source, Path target, int lowResMaxImageSize) {
        boolean run = this.run(
                target.toString(),
                TIMEOUT_MINUTES_IMAGE,
                "ffmpeg",
                "-nostdin",
                "-y",
                "-i", source.toString(),
                "-frames:v", "1",
                "-vf", "scale='min(" + lowResMaxImageSize + ",iw)':'min(" + lowResMaxImageSize + ",ih)':force_original_aspect_ratio=decrease",
                "-c:v", "mjpeg",
                "-q:v", "5",
                "-f", "image2",
                target.toString()
        );
        if (run) {
            return Optional.of("image/jpeg");
        } else {
            return Optional.empty();
        }
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