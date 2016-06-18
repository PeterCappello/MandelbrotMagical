package mandelbrotmagic;

import java.awt.*;

import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;

/**
 *
 * @author Peter Cappello
 */
final class Model
{
    private static final int    ITERATION_LIMIT = 512;
    private static final int    N_PIXELS = 740;
    
//    private static final double EDGE_LENGTH = 2.5;
//    private static final double CENTER_X = -0.5;
//    private static final double CENTER_Y =  0.0;
    private static final double CENTER_X = -0.7440975859375;
    private static final double CENTER_Y = 0.1385680625;
    private static final double EDGE_LENGTH = 0.01611;    
    private static final int COLOR_TABLE_OFFSET = 0;
    private static final int ROTATION_ANGLE = 0;
    private static int[] colorArray = new int[ 360 ];

    private double centerReal;
    private double centerImag;
    private double edgeLength;
    private int    iterationLimit;
    private int    nPixels;
    private int    colorTableOffset;
    private int    rotationAngle;

    private BufferedImage bufferedImage;

    // save sine & cosine of -rotationAngle
    private double sinRotationAngle;
    private double cosRotationAngle;

    Model( double centerReal, double centerImag, double edgeLength, int iterationLimit,
           int nPixels, int colorTableOffset, int rotationAngle )
    {
        this.centerReal       = centerReal;
        this.centerImag       = centerImag;
        this.edgeLength       = edgeLength;
        this.iterationLimit   = iterationLimit;
        this.nPixels          = nPixels;
        this.colorTableOffset = colorTableOffset;
        this.rotationAngle    = rotationAngle;
    }

    Model()
    {
        this( CENTER_X, CENTER_Y, EDGE_LENGTH, ITERATION_LIMIT, N_PIXELS, COLOR_TABLE_OFFSET, ROTATION_ANGLE );
    }

    Model( Model model )
    {
        centerReal       = model.getCenterReal();
        centerImag       = model.getCenterImag();
        edgeLength       = model.getEdgeLength();
        iterationLimit   = model.getIterationLimit();
        nPixels          = model.getNPixels();
        colorTableOffset = model.getColorTableOffset();
        rotationAngle    = model.getRotationAngle();
    }

    Model( Model model, double centerReal, double centerImag, double edgeLength,
            int iterationLimit, int  colorTableOffset, int rotationAngle )
    {
        this.centerReal       = centerReal;
        this.centerImag       = centerImag;
        this.edgeLength       = edgeLength;
        this.iterationLimit   = iterationLimit;
        this.colorTableOffset = colorTableOffset;
        this.rotationAngle    = rotationAngle;
//        iterationLimit        = model.getIterationLimit();
        nPixels               = model.getNPixels();
    }

    double getCenterReal()         { return centerReal; }
    double getCenterImag()         { return centerImag; }
    double getEdgeLength()         { return edgeLength; }
    int    getIterationLimit()     { return iterationLimit; }
    int    getNPixels()            { return nPixels; }
    int    getColorTableOffset()   { return colorTableOffset; }
    int    getRotationAngle()      { return rotationAngle; }

    void setCenterReal( double centerReal ) { this.centerReal = centerReal; }
    void setCenterImag( double centerImag ) { this.centerImag = centerImag; }
    void setEdgeLength( double edgeLength ) { this.edgeLength = edgeLength; }
    void setIterationLimit( int iterationLimit ) { this.iterationLimit = iterationLimit; }
    void setNPixels( int nPixels ) { this.nPixels = nPixels; }
    void setColorTableOffset( int colorTableOffset) { this.colorTableOffset = colorTableOffset; }
    void setRotationAngle( int rotationAngle ) { this.rotationAngle = rotationAngle; }

    @SuppressWarnings("empty-statement")
    void recenter( int X, int Y )
    {
        Y = nPixels - Y;
        double dPixels = nPixels;
        centerImag += edgeLength * ( Y / dPixels - 0.5 );
        centerReal += edgeLength * ( X / dPixels - 0.5 );;
    }

    void zoom( boolean isZoomIn )
    {
        edgeLength = isZoomIn ? edgeLength / 2.0 : edgeLength * 2.0;
    }

    void nullImage() { bufferedImage = null; }

    void drawImage( Graphics graphics, JPanel panel )
    {
        graphics.drawImage( getImage(), 0, 0 , nPixels, nPixels, panel );
    }

    BufferedImage getImage()
    {
        if ( bufferedImage != null )
        {
            return bufferedImage;
        }
        double angleRadians = rotationAngle * 2.0 * Math.PI / 360.0;
        sinRotationAngle = Math.sin( -angleRadians );
        cosRotationAngle = Math.cos( -angleRadians );
        bufferedImage = new BufferedImage( nPixels, nPixels, BufferedImage.TYPE_INT_RGB );
        double delta = edgeLength / nPixels;
        double x = centerReal - edgeLength / 2.0;
        for ( int i = 0; i < nPixels; i++, x += delta )
        {
            double y = centerImag - edgeLength / 2.0;
            for ( int j = 0; j < nPixels; j++, y += delta )
            {
                Point2D.Double point = new Point2D.Double( x, y );
                bufferedImage.setRGB( i, nPixels - 1 - j, getColor( rotate( point ) ) );
            }
        }
        return bufferedImage;
    }

    /*
     * rotate point -rotationAngle radians around the center
     */
    private Point2D.Double rotate( Point2D.Double point )
    {
        double x = point.getX();
        double y = point.getY();

        // translate point so that center is the origin
        x -= centerReal;
        y -= centerImag;

        // rotate (x, y ) -rotationAngle radians
        double rotatedX = cosRotationAngle * x - sinRotationAngle * y;
        double rotatedY = sinRotationAngle * x + cosRotationAngle * y;

        // translate rotated point back
        rotatedX += centerReal;
        rotatedY += centerImag;
        return new Point2D.Double( rotatedX, rotatedY );
    }

    static final double ONE_FOURTH = 1.0 / 4.0;
    static final double ONE_SIXTEENTH = ONE_FOURTH * ONE_FOURTH;
    static final double LOG_2 = Math.log( 2.0 );

    private int getColor( Point2D.Double point )
    {
        double x0 = point.getX();
        double y0 = point.getY();
        double x = x0;
        double y = y0;

        // in cardoid ?
        double xMinusOneFourth = x - ONE_FOURTH;
        double ySquared = y * y;
        double q = xMinusOneFourth * xMinusOneFourth + ySquared;
        if ( q * ( q + xMinusOneFourth ) < ONE_FOURTH * ySquared )
        {
            return 0; // color = black
        }

        // in period-2 bulb ?
        double xPlusOne = x + 1.0;
        if ( xPlusOne * xPlusOne + ySquared < ONE_SIXTEENTH )
        {
            return 0; // color = black
        }

        double xtemp;
        int iteration = 0;
        /*
         * For renormalized iteration count, use escape value of 4 instead of 2.
         * See http://linas.org/art-gallery/escape/escape.html
         *     http://math.unipa.it/~grim/Jbarrallo.PDF
         */
        for ( ; x*x + y*y <= 16.0 && iteration < iterationLimit; iteration++ )
        {
            xtemp = x*x - y*y + x0;
            y = 2*x*y + y0;
            x = xtemp;
        }

        if ( iteration == iterationLimit )
        {
            return 0; // color = black
        }

        // renormalize iteration count
        xtemp = x*x - y*y + x0;
        y = 2*x*y + y0;
        x = xtemp;
        xtemp = x*x - y*y + x0;
        y = 2*x*y + y0;
        x = xtemp;
        double modulus = Math.sqrt( x*x + y*y );
        double mu = (iteration + 2) - Math.log( Math.log ( modulus ) ) / LOG_2;
        int index = (int) mu;
        return colorArray[ (index + colorTableOffset) % colorArray.length ];
    }

    @Override
    public String toString()
    {
        StringBuffer string = new StringBuffer();
        string.append( "Model: centerReal: ");
        string.append( centerReal );
        string.append( " centerImag: ");
        string.append( centerImag );
        string.append( " edgeLength: ");
        string.append( edgeLength );
        string.append( " nPixels: ");
        string.append( nPixels );
        string.append( " rotationAngle: ");
        string.append( rotationAngle );
        return new String( string );
    }

    static void initColorTable()
    {
        float floatOne = (float) 1.0;
        float dRotation = floatOne / colorArray.length;
        for ( int i = 0; i < colorArray.length; i++ )
        {
            float hue = i * dRotation;
            colorArray[ i ] = Color.HSBtoRGB( hue, floatOne, floatOne );
        }
    }

//    private static final int nColorBits = 6;
//    private static final int unusedColorBits = 8 - nColorBits;
//    private static int twoToNColorBits = exp();
//    private static final int maxColorLevel = ( twoToNColorBits - 1) << unusedColorBits;
//    private static int[] colorArray = new int[ 8 * twoToNColorBits ];

//    static private int exp()
//    {
//        twoToNColorBits = 1;
//        for ( int i = 0; i < nColorBits; i++ )
//        {
//            twoToNColorBits *= 2;
//        }
//        System.out.println( "twoToNColorBits: " + twoToNColorBits);
//        return twoToNColorBits;
//    }

//    static int getColorTableLength() { return colorArray.length; }
    
//    private static final int ADD      = 0;
//    private static final int SUBTRACT = 1;
//    private static final int RED   = 2;
//    private static final int GREEN = 1;
//    private static final int BLUE  = 0;
//    private static int index = 0;
//    private static int color = 0;
//    private static int base = 0;

    /*
     * @param colorComponent 0: blue, 1: green, 2: red
     */
//    static void setColorTable( int operation, int colorComponent )
//    {
//        color = 0;
//        if ( operation == ADD )
//        {
//            for ( int i = 0; i < twoToNColorBits; i++ )
//            {
//                setColor( i, colorComponent );
//            }
//        }
//        else
//        {
//            assert operation == SUBTRACT;
//            // put 0s into base position of base
//            int mask = maxColorLevel << (8 * colorComponent ); // put 1s into mask's colorComponent position
//            mask ^= -1; // take 1s complement
//            base &= mask; // put 0s into colorComponent position
//            for ( int i = twoToNColorBits - 1; i >= 0; i-- )
//            {
//                setColor( i, colorComponent );
//            }
//        }
//        base =  color;
//        index += twoToNColorBits;
//    }

//    private static void setColor( int i, int colorComponent )
//    {
//        color = base;
//        int colorComponentLevel = i << ( 8 * colorComponent + unusedColorBits );
//        color |= colorComponentLevel;
//        colorArray[index + i] = color;
//    }

//    static void setColorTable()
//    {
//        // Path: 000 - 00B - 0GB - 0G0 - RG0 - RGB - R0B - 00R - 000
//        setColorTable( ADD, BLUE ); // 000 -> 00B
//        assert color == maxColorLevel << 16; // color = blue
//        setColorTable( ADD, GREEN ); // 00B -> 0GB
//        assert color == (maxColorLevel << 16) + (maxColorLevel << 8); // color = blue + green
//        setColorTable( SUBTRACT, BLUE ); // 0GB -> 0G0
//        assert color == maxColorLevel << 8; // color = green
//        setColorTable( ADD, RED ); // 0G0 -> RG0
//        assert color == (maxColorLevel << 8) + maxColorLevel; // color = red + green
//        setColorTable( ADD, BLUE ); // RG0 -> RGB
//        assert color == (maxColorLevel << 16) + (maxColorLevel << 8) + maxColorLevel; // color = red + green + blue
//        setColorTable( SUBTRACT, GREEN ); // RGB -> R0B
//        assert color == (maxColorLevel << 16) + maxColorLevel; // color = red + blue
//        setColorTable( SUBTRACT, BLUE ); // R0B -> R00
//        assert color == maxColorLevel; // color = red
//        setColorTable( SUBTRACT, RED ); // R00 -> 000
//    }
}
