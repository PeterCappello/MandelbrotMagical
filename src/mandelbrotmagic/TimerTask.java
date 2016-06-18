package mandelbrotmagic;

/**
 *
 * @author Pete Cappello
 */
public final class TimerTask implements Task
{
    private long startTime;
    private int  numImages;

    TimerTask( long startTime, int numImages )
    {
        this.startTime = startTime;
        this.numImages = numImages;
    }

    public Object execute()
    {
        long elapsedTime = System.currentTimeMillis() - startTime;
        System.out.println("Animation: image file generation time: "
                + ( elapsedTime / 1000 ) + " seconds.  sec/file: "
                + ( ( elapsedTime / 1000 ) / numImages )  );
        return null;
    }
}
