package com.ggiova.gallery.components.file;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

@Component
public class MediaInspector {
    private static final Logger LOGGER = LoggerFactory.getLogger(MediaInspector.class);

    // Only supports this types
    private static final Set<String> VIDEO_TYPES = Set.of(
            "video/mp4",
            "video/webm",
            "video/quicktime"
    );

    private final Tika tika = new Tika();
    private final VideoInspector videoInspector;

    public MediaInspector(VideoInspector videoInspector) {
        this.videoInspector = videoInspector;
    }

    public MediaResponse inspectMedia(Path path) {
        MediaResponse image = this.inspectImage(path);
        if (!(image instanceof MediaResponse.UnknownMedia)) {
            return image; // an image, or a real I/O failure
        }

        try {
            // Detected from the file's bytes, not its name or the client's Content-Type.
            String mime = this.tika.detect(path);
            LOGGER.info("Tika detected '{}' for {}", mime, path.getFileName());

            if (VIDEO_TYPES.contains(mime)) {
                return this.videoInspector.inspect(path, mime);
            }
            return new MediaResponse.UnknownMedia();
        } catch (IOException e) {
            return new MediaResponse.Failed(e.getMessage());
        }
    }

    private MediaResponse inspectImage(Path path) {
        try (ImageInputStream iis = ImageIO.createImageInputStream(path.toFile())) {
            Iterator<ImageReader> readers = iis == null
                    ? Collections.emptyIterator()
                    : ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                return new MediaResponse.UnknownMedia();
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(iis);
                String[] mimes = reader.getOriginatingProvider().getMIMETypes();
                if (mimes == null || mimes.length == 0) {
                    return new MediaResponse.UnknownMedia();
                }
                return new MediaResponse.MediaInfo(mimes[0], reader.getWidth(0), reader.getHeight(0), null);
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            return new MediaResponse.Failed(e.getMessage());
        }
    }
}