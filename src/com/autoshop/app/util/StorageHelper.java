package com.autoshop.app.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StorageHelper {

    // The folder name in storage
    private static final String IMAGE_DIR = "taloane";

    /**
     * Copies the selected file to the app's local "taloane" folder.
     * Returns the relative path to be stored in the database.
     */
    public static String copyToAppStorage(String sourcePath) {
        if (sourcePath == null || sourcePath.isEmpty()) return "";

        // 1. Check if it's already in our storage (don't re-copy)
        // If the path starts with our folder name, it's already portable.
        if (sourcePath.startsWith(IMAGE_DIR + File.separator) || sourcePath.startsWith(IMAGE_DIR + "/")) {
            return sourcePath;
        }

        File sourceFile = new File(sourcePath);
        if (!sourceFile.exists()) return "";

        try {
            // 2. Ensure the directory exists (next to the .exe)
            File directory = new File(IMAGE_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 3. Generate a safe, unique filename
            // Format: plate_timestamp.extension (e.g., TM12ABC_20231027_1530.jpg)
            String extension = getExtension(sourceFile.getName());
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String newFileName = "img_" + timestamp + "." + extension;

            File destFile = new File(directory, newFileName);

            // 4. Copy the file
            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // 5. Return the Relative Path (e.g., "car_photos\img_....jpg")
            return IMAGE_DIR + File.separator + newFileName;

        } catch (IOException e) {
            e.printStackTrace();
            return ""; // Fail gracefully
        }
    }

    /**
     * Resolves a stored relative path to a usable absolute path for display.
     */
    public static String getAbsolutePath(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) return "";

        // Convert "car_photos/img.jpg" to "E:\AutoShop\car_photos\img.jpg"
        File file = new File(relativePath);
        return file.getAbsolutePath();
    }

    private static String getExtension(String filename) {
        int i = filename.lastIndexOf('.');
        if (i > 0) {
            return filename.substring(i + 1);
        }
        return "jpg"; // default fallback
    }
}