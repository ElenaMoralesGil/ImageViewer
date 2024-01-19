package software.ulpgc.imageviewer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FolderImageLoader implements ImageLoader {
    private final File folder;
    private final List<String> images;

    public FolderImageLoader(String folderPath) {
        this.folder = new File(folderPath);
        this.images = new ArrayList<>();
        loadImages();
    }

    public List<String> getImages() {
        return images;
    }

    private void loadImages() {
        if (folder.exists() && folder.isDirectory()) {
            File[] imageFiles = folder.listFiles();
            if (imageFiles != null) {
                for (File file : imageFiles) {
                    if (isImageFile(file)) {
                        images.add(file.getAbsolutePath()); // Store absolute path
                    }

                }
            } else {
                System.out.println("There are no files in this directory");
            }
        }
    }

    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".bmp") || name.endsWith(".gif");
    }

    @Override
    public Image load() {
        return imageAt(0);
    }

    private Image imageAt(int i) {
        return new Image() {
            @Override
            public String name() {
                return images.get(i);
            }

            @Override
            public Image prev() {
                return imageAt(i > 0 ? i - 1 : images.size() - 1);
            }

            @Override
            public Image next() {
                return imageAt((i + 1) % images.size());
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Image && ((Image) obj).name().equals(this.name());
            }
        };
    }

}