const MAIN = "http://localhost:4200";

const dropZone = document.getElementById("drop-zone");
const gallery = document.getElementById("gallery");
const fileInput = document.getElementById("file-input");

dropZone.addEventListener("drop", dropHandler);

window.addEventListener("drop", (e) => {
    if ([...e.dataTransfer.items].some((item) => item.kind === "file")) {
        e.preventDefault();
    }
});

dropZone.addEventListener("dragover", (e) => {
    const fileItems = [...e.dataTransfer.items].filter(
        (item) => item.kind === "file",
    );
    if (fileItems.length > 0) {
        e.preventDefault();
        if (fileItems.some((item) => item.type.startsWith("image/"))) {
            e.dataTransfer.dropEffect = "copy";
        } else {
            e.dataTransfer.dropEffect = "none";
        }
    }
});

window.addEventListener("dragover", (e) => {
    const fileItems = [...e.dataTransfer.items].filter(
        (item) => item.kind === "file",
    );
    if (fileItems.length > 0) {
        e.preventDefault();
        if (!dropZone.contains(e.target)) {
            e.dataTransfer.dropEffect = "none";
        }
    }
});

function makeCard(item) {
    const card = document.createElement("button");
    card.className = "media-card";
    card.type = "button";
    card.addEventListener("click", () => openViewer(preview));

    const preview = document.createElement("img");
    preview.className = "media-preview";
    preview.src = `${MAIN}/api/files/${item.id}/content`;
    preview.loading  = "lazy";
    preview.decoding = "async";
    card.append(preview);

    return card;
}

async function uploadFiles(files) {
    const imageFiles = [...files].filter((file) =>
        file.type.startsWith("image/"),
    );

    if (imageFiles.length === 0) { return; }

    const formData = new FormData();
    for (const file of imageFiles) {
        formData.append("files", file);
    }

    try {
        const response = await fetch(MAIN + "/api/files/upload", {
            method: "POST",
            body: formData,
        });

        if (!response.ok) {
            throw new Error(`Upload failed: ${response.status}`);
        }

        const results = await response.json();
        console.log("Upload successful:", results);

        return results;
    } catch (error) {
        console.error("Error uploading files:", error);
    }
}

function dropHandler(ev) {
    ev.preventDefault();

    const files = [...ev.dataTransfer.items]
        .filter((item) => item.kind === "file")
        .map((item) => item.getAsFile())
        .filter((file) => file !== null);

    uploadFiles(files);
    loadImages();
}

fileInput.addEventListener("change", (e) => {
    const files = [...e.target.files];

    uploadFiles(files);
    loadImages();

    // Allow selecting the same file again later.
    e.target.value = "";
});

async function loadImages() {
    try {
        const response = await fetch(MAIN + "/api/files");

        if (!response.ok) {
            throw new Error(`Failed to load images: ${response.status}`);
        }

        const items = await response.json();

        const fragment = document.createDocumentFragment();
        items.forEach(item => {
            fragment.append(makeCard(item));
        });
        gallery.replaceChildren(fragment);
    } catch (error) {
        console.error("Error loading images:", error);
    }
}

function openViewer(opener) {
    console.log("Tried opening file:", opener);
}

loadImages();
