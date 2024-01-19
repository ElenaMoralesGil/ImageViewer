package software.ulpgc.imageviewer;

public interface Image {
    String name();

    Image prev();

    Image next();
}
