package com.ggiova.gallery.components.file;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;

@Component
public class MediaInspector {

    public MediaResponse inspectMedia(Path path) {
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
