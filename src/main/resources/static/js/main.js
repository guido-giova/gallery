const dropZone = document.getElementById("drop-zone");

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

const gallery = document.getElementById("gallery");

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

function dropHandler(ev) {
    ev.preventDefault();
    const files = [...ev.dataTransfer.items]
        .map((item) => item.getAsFile())
        .filter((file) => file);
    displayImages(files);
}

const fileInput = document.getElementById("file-input");
fileInput.addEventListener("change", (e) => {
    displayImages(e.target.files);
});
