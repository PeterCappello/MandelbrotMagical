package mandelbrotmagic;

import java.util.concurrent.BlockingQueue;

/**
 *
 * @author Pete Cappello
 */
final class Worker extends Thread
{
    private BlockingQueue<Task> taskQ;

    Worker( BlockingQueue<Task> taskQ )
    {
        this.taskQ   = taskQ;
        start();
    }

    @Override
    public void run()
    {
        while ( true )
        {
            try
            {
                taskQ.take().execute();
            }
            catch ( InterruptedException ignore ) {}
        }
    }
}
