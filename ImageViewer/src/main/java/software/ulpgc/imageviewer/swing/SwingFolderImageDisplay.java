package software.ulpgc.imageviewer.swing;

import software.ulpgc.imageviewer.FolderImageLoader;
import software.ulpgc.imageviewer.ImageDisplay;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SwingFolderImageDisplay extends JPanel implements ImageDisplay {

    private final List<PaintOrder> orders;
    private List<String> imagePaths; // Store image file paths
    private int shiftStart;
    private Dragged dragged = Dragged.Null;
    private Released released = Released.Null;
    private FolderImageLoader loader;
    private int currentOffset = 0; // Current offset for sliding

    public SwingFolderImageDisplay() {

        this.orders = new ArrayList<>();
        this.imagePaths = new ArrayList<>();
        this.addMouseListener(mouseListener());
        this.addMouseMotionListener(mouseMotionListener());

        addChooseFolderButton(); // Always add the choose folder button

    }
    private void addChooseFolderButton() {
        JButton selectFolderButton = new JButton("Select Folder");
        selectFolderButton.addActionListener(e -> chooseFolder());
        this.add(selectFolderButton);
    }
    public void initializeWithFolderPath(String folderPath) {
        loader = new FolderImageLoader(folderPath);
        imagePaths = loader.getImages();
        if (!imagePaths.isEmpty()) {
            setFocusOnImage(0); // Display the first image
        }
        repaint();
    }
    private void loadImagePaths(String folderPath) {
        loader = new FolderImageLoader(folderPath);
        imagePaths = loader.getImages();
        if (!imagePaths.isEmpty()) {
            setFocusOnImage(0);
        }
        repaint();
    }
    private MouseMotionListener mouseMotionListener() {
        return new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {
                int delta = e.getX() - shiftStart;
                currentOffset += delta;
                shiftStart = e.getX(); // Update shiftStart for the next drag event
                repaint(); // Repaint the panel to reflect the new offset
            }

            public void mouseMoved(MouseEvent e) {
            }
        };
    }

    private MouseListener mouseListener() {
        return new MouseListener() {
            public void mouseClicked(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
                shiftStart = e.getX();
            }

            public void mouseReleased(MouseEvent e) {
                int delta = e.getX() - shiftStart;
                currentOffset += delta;

                int panelWidth = getWidth();
                int imageCount = imagePaths.size();
                int totalImageWidth = panelWidth * imageCount;

                // Normalize the current offset
                currentOffset %= totalImageWidth;
                if (currentOffset > 0) {
                    currentOffset -= totalImageWidth;
                }

                // Calculate the most visible image index
                int mostVisibleImageIndex = -currentOffset / panelWidth;
                int visiblePartOfNextImage = -currentOffset % panelWidth;

                if (visiblePartOfNextImage > panelWidth / 2) {
                    mostVisibleImageIndex++;
                }

                mostVisibleImageIndex = (mostVisibleImageIndex + imageCount) % imageCount;
                setFocusOnImage(mostVisibleImageIndex);
            }


            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        };
    }

    private void chooseFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String folderPath = chooser.getSelectedFile().getAbsolutePath();
            loadImagePaths(folderPath);
        }
    }


    public void paint(Graphics g) {
        super.paint(g);
        if (imagePaths.isEmpty()) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        int panelWidth = getWidth();

        // Calculate the normalized offset to keep it within the total width of all images
        int totalImageWidth = panelWidth * imagePaths.size();
        int normalizedOffset = (currentOffset % totalImageWidth + totalImageWidth) % totalImageWidth;

        // Determine the starting and ending points for drawing images
        int startIdx = -(normalizedOffset / panelWidth) - 1; // Extra -1 to cover partial images at the start
        int endIdx = startIdx + (getWidth() / panelWidth) + 2; // +2 to cover partial images at the end

        for (int i = startIdx; i <= endIdx; i++) {
            int idx = Math.floorMod(i, imagePaths.size());
            BufferedImage image = loadImage(imagePaths.get(idx));
            if (image != null) {
                int xPosition = i * panelWidth + normalizedOffset;
                g2d.drawImage(image, xPosition, 0, panelWidth, getHeight(), this);
            }
        }
    }


    private BufferedImage loadImage(String imagePath) {
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            System.err.println("Image file does not exist: " + imagePath);
            return null;
        }

        try {
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) {
                System.err.println("Image loaded is null: " + imagePath);
            }
            return image;
        } catch (IOException e) {
            System.err.println("Error loading image: " + imagePath + "; Error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public int width() {
        return this.getWidth();

    }

    @Override
    public void clear() {
        orders.clear();
        repaint();
    }

    @Override
    public void paint(String image, int offset) {
        orders.add(new PaintOrder(image, offset));
        repaint();

    }

    public int getCurrentOffset() {
        return currentOffset;
    }

    public void updateOffset(int newOffset) {
        currentOffset += newOffset;
        repaint();
    }

    @Override
    public void on(Dragged dragged) {
        this.dragged = dragged != null ? dragged : Dragged.Null;
    }

    @Override
    public void on(Released released) {
        this.released = released != null ? released : Released.Null;
    }

    public void setFocusOnImage(int imageIndex) {
        int panelWidth = getWidth();
        currentOffset = -imageIndex * panelWidth;
        repaint();
    }

    public int getImageCount() {
        return imagePaths.size();
    }

    private record PaintOrder(String image, int offset) {
    }
}
