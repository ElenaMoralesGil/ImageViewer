package software.ulpgc.imageviewer.swing;

import software.ulpgc.imageviewer.ImageDisplay;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private SwingFolderImageDisplay imageDisplay;

    public MainFrame() {
        this.setTitle("Image Viewer");
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.imageDisplay = new SwingFolderImageDisplay();
        this.add(imageDisplay);
    }

    public SwingFolderImageDisplay createFolderImageDisplay() {
        return imageDisplay;
    }

}
