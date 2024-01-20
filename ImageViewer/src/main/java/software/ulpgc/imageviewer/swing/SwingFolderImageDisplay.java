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
        focusOnImage();
        repaint();
    }

    private void loadImagePaths(String folderPath) {
        loader = new FolderImageLoader(folderPath);
        imagePaths = loader.getImages();
        focusOnImage();
        repaint();
    }

    private void focusOnImage() {
        if (!imagePaths.isEmpty()) {
            setFocusOnImage(0);
        }
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

                normalizeCurrentOffset(totalImageWidth);

                // Calculate the most visible image index
                int mostVisibleImageIndex = -currentOffset / panelWidth;
                int visiblePartOfNextImage = -currentOffset % panelWidth;

                if (visiblePartOfNextImage > panelWidth / 2) {
                    mostVisibleImageIndex++;
                }

                mostVisibleImageIndex = (mostVisibleImageIndex + imageCount) % imageCount;
                setFocusOnImage(mostVisibleImageIndex);
            }

            private void normalizeCurrentOffset(int totalImageWidth) {
                currentOffset %= totalImageWidth;
                if (currentOffset > 0) {
                    currentOffset -= totalImageWidth;
                }
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
        loadImagePaths(returnVal, chooser);
    }

    private void loadImagePaths(int returnVal, JFileChooser chooser) {
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String folderPath = chooser.getSelectedFile().getAbsolutePath();
            loadImagePaths(folderPath);
        }
    }


    public void paint(Graphics g) {
        super.paint(g);
        if (isImageEmpty()) return;

        Graphics2D g2d = (Graphics2D) g;
        int panelWidth = getWidth();

        int normalizedOffset = getNormalizedOffset(panelWidth);

        // Determine the starting and ending points for drawing images
        int startIdx = -(normalizedOffset / panelWidth) - 1; // Extra -1 to cover partial images at the start
        int endIdx = startIdx + (getWidth() / panelWidth) + 2; // +2 to cover partial images at the end

        drawImage(startIdx, endIdx, panelWidth, normalizedOffset, g2d);
    }

    private void drawImage(int startIdx, int endIdx, int panelWidth, int normalizedOffset, Graphics2D g2d) {
        for (int i = startIdx; i <= endIdx; i++) {
            int idx = Math.floorMod(i, imagePaths.size());
            BufferedImage image = loadImage(imagePaths.get(idx));
            if (image != null) {
                int xPosition = i * panelWidth + normalizedOffset;
                g2d.drawImage(image, xPosition, 0, panelWidth, getHeight(), this);
            }
        }
    }

    private boolean isImageEmpty() {
        return imagePaths.isEmpty();
    }

    private int getNormalizedOffset(int panelWidth) {
        int totalImageWidth = panelWidth * imagePaths.size();
        return (currentOffset % totalImageWidth + totalImageWidth) % totalImageWidth;
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


    private record PaintOrder(String image, int offset) {
    }
}
