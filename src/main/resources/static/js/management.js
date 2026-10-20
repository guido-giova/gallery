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
