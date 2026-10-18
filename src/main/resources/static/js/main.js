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

function displayImages(files) {
    for (const file of files) {
        if (file.type.startsWith("image/")) {
            const button = document.createElement("button");
            const img = document.createElement("img");

            img.src = URL.createObjectURL(file);
            img.alt = file.name;
            img.classList.add("media-preview");

            button.classList.add("media-card");
            button.appendChild(img);
            button.appendChild(document.createTextNode(file.name));

            gallery.appendChild(button);
        }
    }
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

    displayImages(files);
    uploadFiles(files);
}

fileInput.addEventListener("change", (e) => {
    const files = [...e.target.files];

    displayImages(files);
    uploadFiles(files);

    // Allow selecting the same file again later.
    e.target.value = "";
});
