package io.github.palexdev.materialfx.demo.utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for handling image operations across different project paths
 */
public class ImageUtils {
    
    private static final String DEFAULT_IMAGE_NAME = "default_product.png";
    
    // Add potential image base directories
    private static final List<String> IMAGE_DIRS = new ArrayList<>();
    
    static {
        // Initialize the list of image directories to check
        
        // Main project paths
        IMAGE_DIRS.add("C:/pi-JavaFx-user/pi-JavaFx-user/demo/src/main/resources/io/github/palexdev/materialfx/demo/images");
        
        // Copied project paths
        IMAGE_DIRS.add("C:/piiii/pi-JavaFx-user - Copy/pi-JavaFx-user/demo/src/main/resources/io/github/palexdev/materialfx/demo/images");
        
        // Relative paths
        String userDir = System.getProperty("user.dir");
        IMAGE_DIRS.add(userDir + "/demo/src/main/resources/io/github/palexdev/materialfx/demo/images");
        
        // Create all directories that don't exist
        for (String dir : new ArrayList<>(IMAGE_DIRS)) {
            try {
                File dirFile = new File(dir);
                if (!dirFile.exists()) {
                    boolean created = dirFile.mkdirs();
                    System.out.println("Created directory: " + dir + ", success: " + created);
                    
                    // If we couldn't create it, remove it from the list
                    if (!created) {
                        IMAGE_DIRS.remove(dir);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error creating directory " + dir + ": " + e.getMessage());
                // If there was an error, remove this dir from the list
                IMAGE_DIRS.remove(dir);
            }
        }
        
        System.out.println("Available image directories: " + IMAGE_DIRS);
    }
    
    /**
     * Load an image using multiple fallback strategies
     * @param imageName the filename of the image (without path)
     * @return Image object if found, null if not found
     */
    public static Image loadImage(String imageName) {
        if (imageName == null || imageName.isEmpty()) {
            return loadDefaultImage();
        }
        
        System.out.println("Attempting to load image: " + imageName);
        
        // Try to load from resources first
        try {
            String resourcePath = "/io/github/palexdev/materialfx/demo/images/" + imageName;
            InputStream is = ImageUtils.class.getResourceAsStream(resourcePath);
            if (is != null) {
                Image image = new Image(is);
                if (!image.isError() && image.getWidth() > 0) {
                    System.out.println("Successfully loaded image from resources: " + resourcePath);
                    return image;
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load image from resources: " + e.getMessage());
        }
        
        // Try each directory in our list
        for (String dir : IMAGE_DIRS) {
            try {
                File imageFile = new File(dir, imageName);
                if (imageFile.exists() && imageFile.isFile()) {
                    try (FileInputStream fis = new FileInputStream(imageFile)) {
                        Image image = new Image(fis);
                        if (!image.isError() && image.getWidth() > 0) {
                            System.out.println("Successfully loaded image from file: " + imageFile.getAbsolutePath());
                            return image;
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Could not load image from directory " + dir + ": " + e.getMessage());
            }
        }
        
        System.out.println("Failed to load image: " + imageName + ". Using default image instead.");
        return loadDefaultImage();
    }
    
    /**
     * Load the default product image
     * @return default image or a placeholder if not found
     */
    public static Image loadDefaultImage() {
        // Try to load the default image
        for (String dir : IMAGE_DIRS) {
            try {
                File defaultFile = new File(dir, DEFAULT_IMAGE_NAME);
                if (defaultFile.exists() && defaultFile.isFile()) {
                    try (FileInputStream fis = new FileInputStream(defaultFile)) {
                        Image image = new Image(fis);
                        if (!image.isError() && image.getWidth() > 0) {
                            System.out.println("Successfully loaded default image from: " + defaultFile.getAbsolutePath());
                            return image;
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Could not load default image from directory: " + e.getMessage());
            }
        }
        
        // If we get here, create a placeholder image
        return createPlaceholderImage();
    }
    
    /**
     * Set an image on an ImageView with proper error handling
     * @param imageView the ImageView to set
     * @param imageName the filename of the image to load
     */
    public static void setImageOnView(ImageView imageView, String imageName) {
        if (imageView == null) {
            return;
        }
        
        Image image = loadImage(imageName);
        imageView.setImage(image);
    }
    
    /**
     * Create a simple placeholder image when no image can be loaded
     * @return a simple placeholder image
     */
    public static Image createPlaceholderImage() {
        try {
            Canvas canvas = new Canvas(100, 100);
            javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
            
            // Fill background
            gc.setFill(Color.LIGHTGRAY);
            gc.fillRect(0, 0, 100, 100);
            
            // Draw border
            gc.setStroke(Color.GRAY);
            gc.strokeRect(0, 0, 100, 100);
            
            // Add text
            gc.setFill(Color.DARKGRAY);
            gc.fillText("No Image", 20, 50);
            
            // Create image from canvas
            WritableImage snapshot = canvas.snapshot(null, null);
            System.out.println("Created placeholder image");
            return snapshot;
        } catch (Exception e) {
            System.err.println("Error creating placeholder image: " + e.getMessage());
            // Last resort - return a 1x1 pixel image
            return new WritableImage(1, 1);
        }
    }
    
    /**
     * Save an uploaded image to all configured image directories
     * @param imageFile the source image file
     * @return the unique filename that was used to save the file
     */
    public static String saveUploadedImage(File imageFile) {
        if (imageFile == null || !imageFile.exists()) {
            return DEFAULT_IMAGE_NAME;
        }
        
        try {
            // Generate a unique filename
            String originalFilename = imageFile.getName();
            String extension = "";
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalFilename.substring(dotIndex);
            }
            
            String uniqueFilename = "product_" + System.currentTimeMillis() + extension;
            System.out.println("Generated unique filename for upload: " + uniqueFilename);
            
            // Save to all available directories
            for (String dir : IMAGE_DIRS) {
                try {
                    File destFile = new File(dir, uniqueFilename);
                    Files.copy(imageFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Copied image to: " + destFile.getAbsolutePath());
                } catch (Exception e) {
                    System.err.println("Could not copy to directory " + dir + ": " + e.getMessage());
                }
            }
            
            return uniqueFilename;
        } catch (Exception e) {
            System.err.println("Error saving image: " + e.getMessage());
            e.printStackTrace();
            return DEFAULT_IMAGE_NAME;
        }
    }
    
    /**
     * Ensure that the default image exists in all image directories
     */
    public static void ensureDefaultImageExists() {
        try {
            // First, find an existing default image to use as the source
            File sourceFile = null;
            
            // Try to find it in resources
            try (InputStream is = ImageUtils.class.getResourceAsStream("/io/github/palexdev/materialfx/demo/images/" + DEFAULT_IMAGE_NAME)) {
                if (is != null) {
                    // Copy to a temporary file
                    Path tempPath = Files.createTempFile("default_image", ".png");
                    Files.copy(is, tempPath, StandardCopyOption.REPLACE_EXISTING);
                    sourceFile = tempPath.toFile();
                }
            } catch (Exception e) {
                System.err.println("Could not extract default image from resources: " + e.getMessage());
            }
            
            // If we couldn't find it in resources, look for it in the directories
            if (sourceFile == null) {
                for (String dir : IMAGE_DIRS) {
                    File defaultFile = new File(dir, DEFAULT_IMAGE_NAME);
                    if (defaultFile.exists() && defaultFile.isFile()) {
                        sourceFile = defaultFile;
                        break;
                    }
                }
            }
            
            // If we found a source file, copy it to all directories
            if (sourceFile != null) {
                for (String dir : IMAGE_DIRS) {
                    try {
                        File destFile = new File(dir, DEFAULT_IMAGE_NAME);
                        if (!destFile.exists()) {
                            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            System.out.println("Created default image in: " + destFile.getAbsolutePath());
                        }
                    } catch (Exception e) {
                        System.err.println("Could not create default image in " + dir + ": " + e.getMessage());
                    }
                }
            } else {
                System.err.println("Could not find a source for the default image to copy.");
            }
        } catch (Exception e) {
            System.err.println("Error ensuring default image exists: " + e.getMessage());
        }
    }
}
