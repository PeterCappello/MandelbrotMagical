package mandelbrotmagic;

/**
 *
 * @author Pete Cappello
 */
final class NodeList
{
    private Node currentNode;

    void add( Model model )
    {
        if ( isEmpty() )
        {
            currentNode = new Node( model );;
        }
        else
        {
            Node newLastNode = new Node( model );
            currentNode.next = newLastNode;
            newLastNode.previous = currentNode;
            currentNode = newLastNode;
        }
    }

    Model goBack()
    {
        if ( currentNode.previous != null )
        {
            currentNode = currentNode.previous;
        }
        return currentNode.model;
    }

    Model goForward()
    {
        if ( currentNode.next != null )
        {
            currentNode = currentNode.next;
        }
        return currentNode.model;
    }

    boolean isEmpty() { return ( currentNode == null ) ? true : false; }

    private class Node
    {
        private Node previous;
        private Node next;
        private Model model;

        Node( Model model )
        {
            this.model = model;
        }
    }

}
