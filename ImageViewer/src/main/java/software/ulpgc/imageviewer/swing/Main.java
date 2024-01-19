package software.ulpgc.imageviewer.swing;

import software.ulpgc.imageviewer.FolderImageLoader;
import software.ulpgc.imageviewer.Image;
import software.ulpgc.imageviewer.ImageDisplay;
import software.ulpgc.imageviewer.ImagePresenter;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::selectFolderAndStart);
    }

    private static void selectFolderAndStart() {
        String folderPath = selectFolder();
        if (folderPath != null) {
            initializeAndShowMainFrame(folderPath);
        } else {
            JOptionPane.showMessageDialog(null, "Folder selection cancelled.");
        }
    }

    private static String selectFolder() {
        JFileChooser folderChooser = new JFileChooser();
        folderChooser.setDialogTitle("Select an Image Folder");
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = folderChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return folderChooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    private static void initializeAndShowMainFrame(String folderPath) {
        FolderImageLoader loader = new FolderImageLoader(folderPath);
        if (!loader.getImages().isEmpty()) {
            MainFrame mainFrame = new MainFrame();
            SwingFolderImageDisplay imageDisplay = mainFrame.createFolderImageDisplay();
            imageDisplay.initializeWithFolderPath(folderPath);
            Image image = loader.load();
            new ImagePresenter(image, imageDisplay);
            mainFrame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(null, "No images available to display.");
        }
    }
}

