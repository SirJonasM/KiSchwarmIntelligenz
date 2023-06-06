import java.awt.MouseInfo;
import java.awt.Point;

public class MouseTracker {
    private Point mouseLocation;

    public MouseTracker() {
        // initialize mouseLocation to the current mouse position
        mouseLocation = MouseInfo.getPointerInfo().getLocation();
    }

    public Point getLocation() {
        // update mouseLocation to the current mouse position
        mouseLocation = MouseInfo.getPointerInfo().getLocation();
        return mouseLocation;
    }
}
