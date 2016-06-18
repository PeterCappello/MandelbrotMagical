/*
 * 1. Why is paintComponent invoked twice?
 * 2. Get nicer color map
 * 3. Make zoom out work for Ctrl-MouseDown.
 */
package mandelbrotmagic;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 *
 * @author Peter Cappello
 */
final class ImagePanel extends JPanel implements MouseListener
{
    private Model model;
    private Main main;
    private NodeList modelHistory = new NodeList();
    
    ImagePanel( Model model, Main main )
    {
        this.model = model;
        this.main = main;
        modelHistory.add( model );
        addMouseListener( this );
    }

    @Override
    public void paintComponent( Graphics graphics )
    {
//        super.paintComponent(graphics);
        long startTime = System.currentTimeMillis();
        main.displayModelParameters();
        model.drawImage( graphics, this );
        long elapsedTime = System.currentTimeMillis() - startTime;
        System.out.println( "ImagePanel.paintComponent: " + elapsedTime + " ms" );
    }

    //  Mouselistener implementation
    public void mousePressed( MouseEvent mouseEvent ) {}

    public void mouseReleased( MouseEvent mouseEvent ) {}

    public void mouseEntered( MouseEvent mouseEvent ) {}

    public void mouseExited( MouseEvent mouseEvent ) {}

    public void mouseClicked( MouseEvent mouseEvent )
    {
        model = new Model( model );
        modelHistory.add( model );
        model.recenter( mouseEvent.getX(), mouseEvent.getY() );
        System.out.println( "button: " + mouseEvent.getButton() );
        boolean isZoomIn = true;
        if ( mouseEvent.getButton() == MouseEvent.BUTTON3 )
        {
            isZoomIn = false;
        }
        System.out.println( " modifierstext: |" + mouseEvent.getMouseModifiersText( InputEvent.CTRL_DOWN_MASK )  +"|");
        if ( mouseEvent.getMouseModifiersText( InputEvent.CTRL_DOWN_MASK ) == "Ctrl" )
        {
            isZoomIn = false;
        }
        model.zoom( isZoomIn );
        main.setModel( model );
        repaint();
    }

    void add( Model model )
    {
        this.model = model;
        modelHistory.add( model );
        repaint();
    }
    
    void setModel( Model model ) { this.model = model; }

    void drawPrevious()
    {
        model = modelHistory.goBack();
        repaint();
    }

    void drawNext()
    {
        model = modelHistory.goForward();
        repaint();
    }
}
