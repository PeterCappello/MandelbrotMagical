package mandelbrotmagic;

import java.io.Serializable;

/**
 *
 * @author Pete Cappello
 */
final class KeyFrame implements Serializable
{
    private static final int    DEFAULT_TIME = 16000;
    private static final double DEFAULT_TOTAL_ROTATION_ANGLE = Math.PI / 2.0;

    private final Model model;
    private final int   time = DEFAULT_TIME; // unit: millisecond
    private final double totalRotationAngle = DEFAULT_TOTAL_ROTATION_ANGLE; // unit: radian

    KeyFrame( Model model ) { this.model = model; }

    Model  getModel() { return model; }
    int    getTime()  { return time; }
    double getTotalRotationAngle() { return totalRotationAngle; }

    @Override
    public String toString()
    {
        StringBuffer string = new StringBuffer();
        return new String ( string );
    }
}
