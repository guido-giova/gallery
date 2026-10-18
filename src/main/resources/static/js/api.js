import { address } from "./address.js";

export async function uploadFiles(files) {
    const imageFiles = [...files].filter((file) =>
        file.type.startsWith("image/"),
    );

    if (imageFiles.length === 0) { return; }

    const formData = new FormData();
    for (const file of imageFiles) {
        formData.append("files", file);
    }

    try {
        const response = await fetch(address("/api/files/upload"), {
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

function makeCard(item) {
    const card = document.createElement("button");
    card.className = "media-card";
    card.type = "button";
    card.addEventListener("click", () => openViewer(preview));

    const preview = document.createElement("img");
    preview.className = "media-preview";
    preview.src = address(`/api/files/${item.id}/content`);
    preview.loading  = "lazy";
    preview.decoding = "async";
    card.append(preview);

    return card;
}

export async function loadImages() {
    try {
        const response = await fetch(address("/api/files"));

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
