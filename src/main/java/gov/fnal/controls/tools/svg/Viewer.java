//  (c) 2010 Fermi Research Alliance
//  $Id: Viewer.java,v 1.6 2010/02/12 21:03:20 apetrov Exp $
package gov.fnal.controls.tools.svg;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Viewer extends JFrame implements ActionListener {

    private static final Logger log = Logger.getLogger( Viewer.class.getName());
    private static final TransformerFactory trf = TransformerFactory.newInstance();
    private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    private final SVGSyntaxHandler handler = new SVGSyntaxHandler();
    private final JPanel canvas = new JPanel( new BorderLayout());
    private final JButton buBrowse = new JButton( "Browse" );
    private final JButton buRepaint = new JButton( "Reload" );
    private final JButton buOp1 = new JButton( "XForm" );
    private final JButton buOp2 = new JButton( "Rotate" );
    private final JTextField fileName = new JTextField(
        "/home/apetrov/projects/svg/examples/test.svg"
    );
    private final SAXParser parser;
    private Transformer xform;
    private DocumentBuilder builder;
    private SVGComponentWrapper wrapper;

    private JFileChooser fileChooser;

    public static void main( String[] args ) {
        new Viewer();
    }

    public Viewer() {

        try {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parser = parserFactory.newSAXParser();
            parser.getXMLReader().setFeature( "http://xml.org/sax/features/namespaces", true );
            xform = trf.newTransformer();
            builder = dbf.newDocumentBuilder();
        } catch (Exception ex) {
            throw new RuntimeException( ex );
        }
        
        setDefaultCloseOperation( EXIT_ON_CLOSE );
        setTitle( "Simple SVG Viewer" );
        setSize( 800, 600 );
        setLocationRelativeTo( null );

        canvas.setBorder( new CompoundBorder(
                new EmptyBorder( 13, 3, 13, 3 ),
                new EtchedBorder( EtchedBorder.LOWERED )
        ));

        JLabel jLabel1 = new JLabel( "File:" );
        buBrowse.addActionListener( this );
        buRepaint.addActionListener( this );
        buOp1.addActionListener( this );
        buOp2.addActionListener( this );

        JPanel tools = new JPanel();
        tools.setLayout( new GridBagLayout());
        tools.add( jLabel1,   new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets( 6, 6, 0, 6 ), 0, 0 ) );
        tools.add( fileName,  new GridBagConstraints( 1, 0, 1, 1, 1.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets( 6, 0, 0, 6 ), 0, 0 ) );
        tools.add( buBrowse,  new GridBagConstraints( 2, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets( 6, 0, 0, 6 ), 0, 0 ) );
        tools.add( buRepaint, new GridBagConstraints( 3, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets( 6, 0, 0, 6 ), 0, 0 ) );
        tools.add( buOp1,     new GridBagConstraints( 4, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets( 6, 0, 0, 6 ), 0, 0 ) );
        tools.add( buOp2,     new GridBagConstraints( 5, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets( 6, 0, 0, 6 ), 0, 0 ) );

        getContentPane().setLayout( new BorderLayout());
        getContentPane().add( tools, BorderLayout.NORTH );
        getContentPane().add( canvas, BorderLayout.CENTER );

        setVisible( true );
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        Object o = e.getSource();
        if (o.equals( buBrowse )) {
            chooseFile();
        } else if (o.equals( buRepaint )) {
            load();
        } else if (o.equals( buOp1 )) {
            op1();
        } else if (o.equals( buOp2 )) {
            op2();
        }
    }

    private void chooseFile() {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
            fileChooser.setFileFilter( new SVGFileFilter());
        }
        int res = fileChooser.showOpenDialog( this );
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile();
            fileName.setText( f.getAbsolutePath());
            load();
        }
    }

    private void load() {
        try {
            handler.reset();
            parser.parse( new File( fileName.getText()), handler );
            SVGComponent svg = handler.getResult();
            canvas.removeAll();
            wrapper = new SVGComponentWrapper( svg );
            canvas.add( wrapper, BorderLayout.CENTER );
            format( svg, System.out );
        } catch (Exception ex) {
            log.log( Level.SEVERE, "Can't load file", ex );
            JOptionPane.showMessageDialog(
                    this,
                    (ex.getMessage() != null) ? ex.getMessage() : ex.getClass().getName(),
                    ex.getClass().getName(),
                    JOptionPane.ERROR_MESSAGE
            );
        } finally {
            canvas.revalidate();
        }
    }

    private void op1() {
        if (wrapper == null) {
            return;
        }
        SVGComponent svg = wrapper.getSVGComponent();
        Rectangle2D r = svg.getBounds();
        if (r == null) {
            return;
        }
        Rectangle2D r2 = new Rectangle2D.Double(
                r.getX() + 20,
                r.getY() + 10,
                r.getWidth() * 1.25,
                r.getHeight() * 1.25
        );
        try {
            //svg.setBounds( r2 );
            //svg.transformBounds();
            format( svg, System.out );
        } catch (Exception ex) {
            log.log( Level.SEVERE, "Can't move SVG", ex );
            JOptionPane.showMessageDialog(
                    this,
                    (ex.getMessage() != null) ? ex.getMessage() : ex.getClass().getName(),
                    ex.getClass().getName(),
                    JOptionPane.ERROR_MESSAGE
            );
        } finally {
            canvas.revalidate();
        }
    }

    private void op2() {
        if (wrapper == null) {
            return;
        }
        SVGComponent svg = wrapper.getSVGComponent();
        Rectangle2D r = svg.getBounds();
        if (r == null) {
            return;
        }
        Rectangle2D r2 = new Rectangle2D.Double(
                r.getX(),
                r.getY(),
                r.getWidth() * 1.25,
                r.getHeight() * 1.25
        );
        try {
            //svg.setBounds( r2 );
            AffineTransform zform = svg.getTransform();
            if (zform == null) {
                zform = new AffineTransform();
            }
            zform.rotate( Math.PI / 6, 100, 10 );
            svg.setTransform( zform );
            format( svg, System.out );
        } catch (Exception ex) {
            log.log( Level.SEVERE, "Can't scale SVG", ex );
            JOptionPane.showMessageDialog(
                    this,
                    (ex.getMessage() != null) ? ex.getMessage() : ex.getClass().getName(),
                    ex.getClass().getName(),
                    JOptionPane.ERROR_MESSAGE
            );
        } finally {
            canvas.revalidate();
        }
    }

    private void format( SVGComponent comp, OutputStream out ) throws TransformerException {
        Document doc = builder.newDocument();
        Element e = comp.getXML( doc );
        xform.reset();
        xform.setOutputProperty( OutputKeys.METHOD, "xml" );
        xform.setOutputProperty( OutputKeys.INDENT, "yes" );
        xform.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
        xform.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "4" );
        xform.transform( new DOMSource( e ), new StreamResult( out ));
    }

    class SVGFileFilter extends FileFilter {

        @Override
        public boolean accept( File f ) {
            return f.isDirectory() || f.getName().endsWith( ".svg" );
        }

        @Override
        public String getDescription() {
            return "SVG Files (*.svg)";
        }

    }

}
