/*
 * 0. Fix icons for forward and back buttons
 * 1. Save a KeyFrameList - then create one for testing.
 * 1.5 multithread image generation.
 * 2. In JFileChooser: Restrict file suffixes displayed
 * 3. In JFileChooser: Set a selection of file suffixes (file types)
 * 4. Store the Model object as png image metadata.
 *
 *  0.1 MandelbrotMagicMenu: implement JMenuItems: about, preferences, hide, hideOthers, showAll;
 *      Implement File Open
 * 0.0 Correct shortcuts for menu items
 * 0.0 Display shortcuts in menu items
 * On modified TextFields: validity check input
 * On modified TextFields: do right thing when multiple fields are modified, 1 is entered.
 */
package mandelbrotmagic;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 *
 * @author Peter Cappello
 */
final class Main extends JFrame
{
    private Model model = new Model();
    private File imageFolder;
    private java.util.List<KeyFrame> keyFrameList;
    private File animationFolder;
    private BlockingQueue<Task> imageTaskQ = new LinkedBlockingQueue<Task>();

    // GUI components
    private JMenuBar menuBar = new JMenuBar();
        private JMenu mandelbrotmagicMenu = new JMenu("MandelbrotMagic");
            private JMenuItem aboutMenuItem = new JMenuItem("About");
            private JMenuItem preferencesMenuItem = new JMenuItem("Preferences", KeyEvent.VK_P);
            private JMenuItem hideMenuItem = new JMenuItem("Hide MandelbrotMagic", KeyEvent.VK_H);
            private JMenuItem hideOthersMenuItem = new JMenuItem("Hide Others", KeyEvent.VK_Q);
            private JMenuItem showAllMenuItem = new JMenuItem("Show All");
            private JMenuItem quitMenuItem = new JMenuItem("Quit MandelbrotMagic", KeyEvent.VK_Q);

        private JMenu imageMenu = new JMenu("Image");
            private JMenuItem openImageMenuItem = new JMenuItem("Open", KeyEvent.VK_O);
            private JMenuItem saveImageMenuItem = new JMenuItem("Save", KeyEvent.VK_S);

       private JMenu movieMenu = new JMenu("Animation");
            private JMenuItem newMovieMenuItem = new JMenuItem("New", KeyEvent.VK_M);
            private JMenuItem addMovieMenuItem = new JMenuItem("Add key image", KeyEvent.VK_A);
            private JMenuItem animateMovieMenuItem = new JMenuItem("Animate", KeyEvent.VK_Z);

    private JToolBar toolBar = new JToolBar("Tool Bar");
        private JButton backButton    = new JButton();
        private JButton forwardButton = new JButton();
           
    private ImagePanel imagePanel = new ImagePanel( model, this );
    private JScrollPane scrollPane = new JScrollPane( imagePanel );
//            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
//            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED );
    private JPanel controlPanel = new JPanel();
        private JPanel imageControlPanel = new JPanel();
            private JLabel imageControlPanelLabel = new JLabel("Image Controls");
            private JPanel imageParameterControls = new JPanel();
                private JLabel centerRealLabel = new JLabel( "Real coordinate ");
                private JTextField centerRealTextField = new JTextField( 15 );
                private JLabel centerImagLabel = new JLabel( "Imag coordinate ");
                private JTextField centerImagTextField = new JTextField( 15 );
                private JLabel edgeLengthLabel = new JLabel( "Edge length ");
                private JTextField edgeLengthTextField = new JTextField( 15 );
                private JLabel iterationLimitLabel = new JLabel( "Iteration limit ");
                private JTextField iterationLimitTextField = new JTextField( 15 );
                private JLabel colorMapOffsetLabel = new JLabel( "Color map offset ");
                private JTextField colorMapOffsetTextField = new JTextField( 15 );
                private JLabel rotationAngleLabel = new JLabel( "Rotation angle (radians) ");
                private JTextField rotationAngleTextField = new JTextField( 15 );

            private JPanel imageButtonPanel = new JPanel();
                private JButton resetButton = new JButton( "Reset");
                private JButton  saveButton = new JButton( "Save");

        private JPanel animationControlPanel = new JPanel();
            private JLabel animationControlPanelLabel = new JLabel("Animation Controls");
            private JPanel animationButtonPanel = new JPanel();
                private JButton newAnimationButton = new JButton("New");
                private JButton addAnimationButton = new JButton("Add Key Frame");
                private JButton animateAnimationButton = new JButton("Animate");

    static final int FRAME_RATE  = 15;

    Main() { initComponents(); }

    void displayModelParameters()
    {
        centerRealTextField.setText( Double.toString( model.getCenterReal() ) );
        centerImagTextField.setText( Double.toString( model.getCenterImag() ) );
        edgeLengthTextField.setText( Double.toString( model.getEdgeLength() ) );
        iterationLimitTextField.setText( Integer.toString( model.getIterationLimit() ) );
        colorMapOffsetTextField.setText( Integer.toString( model.getColorTableOffset() ) );
        rotationAngleTextField.setText( Integer.toString( model.getRotationAngle() ) );
    }

    private void initComponents()
    {
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        setSize( 1400, 900 );
        setLayout( new BorderLayout() );

        // Menu bar initialization
        setJMenuBar( menuBar );
            menuBar.add( mandelbrotmagicMenu );
                mandelbrotmagicMenu.add( aboutMenuItem );
                mandelbrotmagicMenu.add( preferencesMenuItem );
                mandelbrotmagicMenu.add( hideMenuItem );
                mandelbrotmagicMenu.add( hideOthersMenuItem );
                mandelbrotmagicMenu.add( showAllMenuItem );
                mandelbrotmagicMenu.add( quitMenuItem );
            menuBar.add( imageMenu );
                imageMenu.add( openImageMenuItem );
                imageMenu.add( saveImageMenuItem );
           menuBar.add( movieMenu );
                movieMenu.add( newMovieMenuItem );
                movieMenu.add( addMovieMenuItem );
                movieMenu.add( animateMovieMenuItem );

           addToolBarButtons();
        add( toolBar, BorderLayout.PAGE_START );
        add( scrollPane, BorderLayout.CENTER );
//        add( imagePanel, BorderLayout.CENTER );
//        add( saveButton, BorderLayout.PAGE_END );

        controlPanel.setLayout( new BorderLayout() );

            imageControlPanel.setLayout( new BorderLayout() );
            imageControlPanel.add( imageControlPanelLabel, BorderLayout.NORTH );
                imageParameterControls.setLayout( new GridLayout( 6, 2 ) );
                imageParameterControls.add( centerRealLabel );
                imageParameterControls.add( centerRealTextField );
                imageParameterControls.add( centerImagLabel );
                imageParameterControls.add( centerImagTextField );
                imageParameterControls.add( edgeLengthLabel );
                imageParameterControls.add( edgeLengthTextField );
                imageParameterControls.add( iterationLimitLabel );
                imageParameterControls.add( iterationLimitTextField );
                imageParameterControls.add( colorMapOffsetLabel );
                imageParameterControls.add( colorMapOffsetTextField );
                imageParameterControls.add( rotationAngleLabel );
                imageParameterControls.add( rotationAngleTextField );
            imageControlPanel.add( imageParameterControls, BorderLayout.CENTER );
                imageButtonPanel.setLayout(new GridLayout( 1, 2 ));
                imageButtonPanel.add( resetButton );
                imageButtonPanel.add( saveButton );
            imageControlPanel.add( imageButtonPanel, BorderLayout.SOUTH );

        controlPanel.add( imageControlPanel, BorderLayout.NORTH );

            animationControlPanel.setLayout( new BorderLayout() );
            animationControlPanel.add( animationControlPanelLabel, BorderLayout.NORTH );
            animationControlPanel.add( animationButtonPanel, BorderLayout.CENTER );
                animationButtonPanel.setLayout(new GridLayout( 1, 3 ));
                animationButtonPanel.add( newAnimationButton );
                animationButtonPanel.add( addAnimationButton );
                animationButtonPanel.add( animateAnimationButton );
        controlPanel.add( animationControlPanel, BorderLayout.SOUTH );
        add( controlPanel, BorderLayout.EAST );

        scrollPane.setBorder( BorderFactory.createEtchedBorder( EtchedBorder.LOWERED ) );
        imageControlPanel.setBorder( BorderFactory.createEtchedBorder( EtchedBorder.LOWERED ) );
        animationControlPanel.setBorder( BorderFactory.createEtchedBorder( EtchedBorder.LOWERED ) );

        //------------------------------------------
        // contoller TEMPLATE CODE for each action
        //------------------------------------------
        //  __________ MandelbrotMagic Menu __________
        aboutMenuItem.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent actionEvent )
            {
                aboutMenuItemActionPerformed( actionEvent );
            }
        });

        hideMenuItem.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent actionEvent )
            {
                hideMenuItemActionPerformed( actionEvent );
            }
        });

        quitMenuItem.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent actionEvent )
            {
                quitMenuItemActionPerformed( actionEvent );
            }
        });


        //  __________ Image Menu __________
        openImageMenuItem.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent actionEvent )
            {
                openMenuItemActionPerformed( actionEvent );
            }
        });

        saveImageMenuItem.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent actionEvent )
            {
                saveMenuItemActionPerformed( actionEvent );
            }
        });

        //  __________ Animation Menu __________
        newMovieMenuItem.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent actionEvent )
            {
                newMovieMenuItemActionPerformed( actionEvent );
            }
        });

        addMovieMenuItem.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent actionEvent )
            {
                addMovieMenuItemActionPerformed( actionEvent );
            }
        });

        animateMovieMenuItem.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent actionEvent )
            {
                animateMovieMenuItemActionPerformed( actionEvent );
            }
        });

        //  __________ Tool Bar ActionListener  __________
        backButton.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent actionEvent )
            {
                backButtonActionPerformed( actionEvent );
            }
        });

        forwardButton.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent actionEvent )
            {
                forwardButtonActionPerformed( actionEvent );
            }
        });


        //  __________ Image Control Panel __________
        centerRealTextField.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent actionEvent )
            {
                centerRealTextFieldActionPerformed( actionEvent );
            }
        });

        centerImagTextField.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent actionEvent )
            {
                centerImagTextFieldActionPerformed( actionEvent );
            }
        });

        edgeLengthTextField.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent actionEvent )
            {
                edgeLengthTextFieldActionPerformed( actionEvent );
            }
        });

        iterationLimitTextField.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent actionEvent )
            {
                iterationLimitTextFieldActionPerformed( actionEvent );
            }
        });

        colorMapOffsetTextField.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent actionEvent )
            {
                colorMapOffsetTextFieldActionPerformed( actionEvent );
            }
        });

        rotationAngleTextField.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent actionEvent )
            {
                rotationAngleTextFieldActionPerformed( actionEvent );
            }
        });

        resetButton.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent actionEvent )
            {
                resetButtonActionPerformed( actionEvent );
            }
        });

        saveButton.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent actionEvent )
            {
                saveMenuItemActionPerformed( actionEvent );
            }
        });

        //  __________ Animation Control Panel __________
        newAnimationButton.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent actionEvent )
            {
                newMovieMenuItemActionPerformed( actionEvent );
            }
        });

        addAnimationButton.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent actionEvent )
            {
                addMovieMenuItemActionPerformed( actionEvent );
            }
        });

        animateAnimationButton.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent actionEvent )
            {
                animateMovieMenuItemActionPerformed( actionEvent );
            }
        });

        // create animation image worker thread pool
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        System.out.println( "availableProcessors: " + availableProcessors );
        for ( int i = 0; i < availableProcessors; i++ )
        {
            new Worker( imageTaskQ );
        }
        
        Model.initColorTable();
    }

    private void addToolBarButtons()
    {
        // icon source: java.sun.com/developer/techDocs/hi/repository/TBG_Navigation.html
        makeNavigationButton( backButton, "Back24", "backAction", "Back to previous image" );
        makeNavigationButton( forwardButton, "Forward24", "forwardAction", "Forward to next image" );
    }

    private void makeNavigationButton( JButton button, String imageName,
            String actionCommand, String toolTipText )
    {
        String imgLocation = "resources/toolbarButtonGraphics/navigation/" + imageName + ".gif";
        URL imageURL = Main.class.getResource( imgLocation );

        button.setActionCommand( actionCommand ); // ?? Needed?
        button.setToolTipText( toolTipText );
        if ( imageURL != null )
        {
            button.setIcon( new ImageIcon( imageURL, toolTipText ) );
        }
        else
        {
            System.err.println( "Resource not found: " + imgLocation );
    //            button.setText( toolTipText );
            Icon icon = new ImageIcon( "resources/toolbarButtonGraphics/navigation/" + imageName + ".gif", toolTipText );
            button.setIcon( icon );
        }
        toolBar.add( button );
    }

    // _____________________________
    //  controller for each action
    // _____________________________
    // ______________________________________
    //  MandelbrotMagic Menu Item Conrollers
    // ______________________________________
    private void aboutMenuItemActionPerformed( ActionEvent actionEvent )
    {
        String message =
                "Mandelbrot Magic \n" +
                "For more information, consult mandelbrotmagic.petercappello.com \n\n" +
                "Product version: 1.0 \n" +
                "Java: 1.5.0_22";
        String title = "About";
        int messageType = JOptionPane.PLAIN_MESSAGE;
//        URL url = getClass().getResource( "resources/apps_logo_sm.gif");
//        if ( url == null )
//        {
//            System.err.println("Could not find: resources/apps_logo_sm.gif");
//            return;
//        }
//        Icon icon = new ImageIcon( url, "A Mandelbrot set image");
        Icon icon = new ImageIcon( "resources/images/322px-Mandel_zoom_00_mandelbrot_set.jpg", "A Mandelbrot set image");
        JOptionPane.showMessageDialog( this, message, title, messageType, icon );
        return;
    }

    private void hideMenuItemActionPerformed( ActionEvent actionEvent )
    {
//        setVisible( false );
    }

    private void quitMenuItemActionPerformed( ActionEvent actionEvent )
    {
        System.exit( 0 );
    }

    // ______________________________________
    //  Image Menu Item Conrollers
    // _______________________________________
    private void openMenuItemActionPerformed( ActionEvent actionEvent )
    {
        BufferedImage bufferedImage = null;
        JFileChooser fileChooser = new JFileChooser( imageFolder );
        int returnValue = fileChooser.showDialog( this, "Open");
        if ( returnValue == JFileChooser.APPROVE_OPTION )
        {
            File imageFile = fileChooser.getSelectedFile();
            try
            {
                bufferedImage = ImageIO.read( imageFile );
            }
            catch ( IOException ioException )
            {
                ioException.printStackTrace();
            }
        }
    }

    private void saveMenuItemActionPerformed( ActionEvent actionEvent )
    {
        JFileChooser fileChooser = new JFileChooser( imageFolder );
        int returnValue = fileChooser.showDialog( this, "Save");
        if ( returnValue == JFileChooser.APPROVE_OPTION )
        {
            File imageFile = fileChooser.getSelectedFile();
            try
            {
                ImageIO.write( model.getImage(), "png", imageFile );
            }
            catch ( IOException ioException )
            {
                ioException.printStackTrace();
            }
        }
    }

    // ______________________________________
    //  Movie Menu Item Conrollers
    // _______________________________________
    private void newMovieMenuItemActionPerformed( ActionEvent actionEvent )
    {
        keyFrameList = new LinkedList<KeyFrame>();
    }

    private void addMovieMenuItemActionPerformed( ActionEvent actionEvent )
    {
        if ( keyFrameList == null )
        {
            JOptionPane.showMessageDialog( this, "To animate, first, create a new Key Frame List.");
            return;
        }
        keyFrameList.add( new KeyFrame( model ) );
    }

    private void animateMovieMenuItemActionPerformed( ActionEvent actionEvent )
    {
        if ( keyFrameList == null )
        {
            JOptionPane.showMessageDialog( this, "To animate, create a new Key Frame List.");
            return;
        }
        if ( keyFrameList.size() < 2 )
        {
            JOptionPane.showMessageDialog( this, "To animate, at least 2 Key Frames are needed.");
            return;
        }
        assert keyFrameList.size() > 1;
        Iterator<KeyFrame> iterator = keyFrameList.iterator();
        KeyFrame currentKeyFrame = iterator.next();

        JFileChooser fileChooser = new JFileChooser( animationFolder );
        int  returnValue = fileChooser.showDialog( this, "Enter an arbitrary file name.");
        if ( returnValue != JFileChooser.APPROVE_OPTION )
        {
            return;
        }
        animationFolder = fileChooser.getCurrentDirectory();
        long startTime = System.currentTimeMillis();

        // generate and enqueue image tasks
        Model currentModel = currentKeyFrame.getModel();
        int imageNum = 1;
        Task imageTask = new ImageTask( currentModel, imageNum++, animationFolder );
        imageTaskQ.add( imageTask );
        while ( iterator.hasNext() )
        {
            KeyFrame nextKeyFrame = iterator.next();
            imageNum = generateImageTasks( currentKeyFrame, nextKeyFrame, imageNum );
            currentKeyFrame = nextKeyFrame;
        }
        long elapsedTime = System.currentTimeMillis() - startTime;
        System.out.println( "Animation: image task generation time: " + elapsedTime + "ms."
                + "  Tasks/sec: " + ( imageNum / elapsedTime )
                + " Number of images: " + imageNum + ".");
        Task task = new TimerTask ( startTime, imageNum );
        imageTaskQ.add( task );
    }

    //_________ Tool Bar controllers
    private void backButtonActionPerformed( ActionEvent actionEvent )
    {
        imagePanel.drawPrevious();
    }

    private void forwardButtonActionPerformed( ActionEvent actionEvent )
    {
        imagePanel.drawNext();
    }


    //_________ Image Control controllers
    private void centerRealTextFieldActionPerformed( ActionEvent actionEvent )
    {
        double value = Double.parseDouble( centerRealTextField.getText() );
        model = new Model( model );
        model.setCenterReal( value );
        imagePanel.setModel( model );
        imagePanel.repaint();
    }

    private void centerImagTextFieldActionPerformed( ActionEvent actionEvent )
    {
        double value = Double.parseDouble( centerImagTextField.getText() );
        model = new Model( model );
        model.setCenterImag( value );
        imagePanel.setModel( model );
        imagePanel.repaint();
    }

    private void edgeLengthTextFieldActionPerformed( ActionEvent actionEvent )
    {
        double value = Double.parseDouble( edgeLengthTextField.getText() );
        model = new Model( model );
        model.setEdgeLength( value );
        imagePanel.setModel( model );
        imagePanel.repaint();
    }

    private void iterationLimitTextFieldActionPerformed( ActionEvent actionEvent )
    {
        int value = Integer.parseInt( iterationLimitTextField.getText() );
        model = new Model( model );
        model.setIterationLimit( value );
        imagePanel.setModel( model );
        imagePanel.repaint();
    }

    private void colorMapOffsetTextFieldActionPerformed( ActionEvent actionEvent )
    {
        int value = Integer.parseInt( colorMapOffsetTextField.getText() );
        model = new Model( model );
        model.setColorTableOffset( value );
        imagePanel.setModel( model );
        imagePanel.repaint();
    }

    private void rotationAngleTextFieldActionPerformed( ActionEvent actionEvent )
    {
        int value = Integer.parseInt( rotationAngleTextField.getText() );
        model = new Model( model );
        model.setRotationAngle( value );
        imagePanel.setModel( model );
        imagePanel.repaint();
    }

    private void resetButtonActionPerformed( ActionEvent actionEvent )
    {
        imagePanel.add( new Model() );
    }

    private int generateImageTasks( KeyFrame keyFrame1, KeyFrame keyFrame2, int imageNum )
    {
        double time = keyFrame2.getTime() / 1000.0;
        double totalRotationAngle = keyFrame2.getTotalRotationAngle();
        int numFrames = (int) Math.ceil( time * FRAME_RATE );

        Model model1 = keyFrame1.getModel();
        Model model2 = keyFrame2.getModel();
        double x1 = model1.getCenterReal();
        double x2 = model2.getCenterReal();
        double y1 = model1.getCenterImag();
        double y2 = model2.getCenterImag();
        double z1 = model1.getEdgeLength();
        double z2 = model2.getEdgeLength();
        int    i1 = model1.getIterationLimit();
        int    i2 = model2.getIterationLimit();
        int    o1 = model1.getColorTableOffset();
        int    o2 = model2.getColorTableOffset();
        int    r1 = model1.getRotationAngle();
        int    r2 = model2.getRotationAngle();

        double X = x2 - x1;
        double Y = y2 - y1;
        double Z = z2 / z1;
        int    I = i2 - i1;
        int    O = o2 - o1;
        int    R = r2 - r1;
        double dX = X / numFrames;
        double dY = Y / numFrames;
        double dZ = Math.pow( Z, 1.0 / numFrames );
        double dR = totalRotationAngle / numFrames;

        Model currentModel = model1;
        for ( int i = 1; i < numFrames; i++ )
        {
            x1 += dX;
            y1 += dY;
            z1 *= dZ;
            int ii = i1 + I * i / numFrames;
            int oo = o1 + O * i / numFrames;
            int rr = r1 + R * i / numFrames;
            r1 += dR;
            currentModel = new Model( currentModel, x1, y1, z1, ii, oo, rr );
            ImageTask imageTask = new ImageTask( currentModel, imageNum++, animationFolder );
            imageTaskQ.add( imageTask );
        }
        
        return imageNum;
    }

    void setModel( Model model ) { this.model = model; }
    
    /**
     * @param args the command line arguments - unused
     */
    public static void main( String[] args )
    {
        EventQueue.invokeLater( new Runnable()
        {
            public void run() { new Main().setVisible( true ); }
        }
        );
    }
}
