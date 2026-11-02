CREATE TABLE variants (
        id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        media_id     BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
        kind         TEXT NOT NULL,
        storage_path TEXT NOT NULL UNIQUE,
        mime         TEXT NOT NULL,
        UNIQUE (media_id, kind)
);