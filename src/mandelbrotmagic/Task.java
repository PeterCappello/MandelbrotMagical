package mandelbrotmagic;

/**
 * An encapsulated computation.
 *
 * @param <Return_Value> - the type of the execute method's return value
 * @author cappello
 */
interface Task<Return_Value>
{
    /**
     * This method encapsulas the Task object's computation.
     * 
     * @return This is the "output" of the computation that the execute method
     * encapsulates.
     */
    Return_Value execute();
}
