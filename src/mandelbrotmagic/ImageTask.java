package mandelbrotmagic;

import java.awt.image.BufferedImage;
import javax.imageio.*;
import java.io.*;

/**
 * Immutable contains: Image generation task
 *
 * @author Pete Cappello
 */
final class ImageTask implements Task
{
    private Model model;
    private int imageNum;
    private File animationFolder;

    ImageTask( Model model, int imageNum, File animationFolder )
    {
        this.model = model;
        this.imageNum = imageNum;
        this.animationFolder = animationFolder;
    }

    public Object execute()
    {
        BufferedImage bufferedImage = model.getImage();
        File imageFile = new File( animationFolder + "/image" + imageNum + ".png" );
        try
        {
            ImageIO.write( bufferedImage, "png", imageFile );
        }
        catch ( IOException ioException )
        {
            ioException.printStackTrace();
        }
        return null;
    }
}
