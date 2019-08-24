// (c) 2001-2010 Fermi Research Alliance
// $Id: PerPlotServlet.java,v 1.3 2010/09/15 16:14:05 apetrov Exp $
package gov.fnal.controls.webapps.syndi;

import java.awt.Rectangle;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import static java.lang.String.*;

/**
 *
 * @author  Andrey Petrov, Tim Bolshakov
 * @version $Date: 2010/09/15 16:14:05 $
 */
public class PerPlotServlet extends HttpServlet {

    private static final long DEFAULT_PERIOD = 1 * 24 * 60 * 60 * 1000; // 1 days in milliseconds

    private static final String XMLNS_SVG = "http://www.w3.org/2000/svg";

    // Size of the whole picture in relative units
    private static final Rectangle PICTURE_BOUNDS = new Rectangle( 0, 0, 1000, 750 );

    // Bounds of the plot area withing PICTURE_BOUNDS
    private static final Rectangle PLOT_BOUNDS = new Rectangle( 100, 10, 890, 640 );

    private static final String PICTURE_BACKGROUND = "none";

    private static final String PLOT_BACKGROUND = "ivory";

    private static final String GRID_COLOR = "dimgray";

    private static final double GRID_STROKE = 1.0;

    private static final String BORDER_COLOR = "black";

    private static final double BOEDER_STROKE = 1.0;

    private static final String TICK_COLOR = "black";

    private static final double TICK_STROKE = 1.0;

    private static final int GRID_X_COUNT = 3;

    private static final int GRID_Y_COUNT = 1;

    private static final int TICK_SIZE = 5;

    private static final int PLOT_X_MARGIN = 10;

    private static final int PLOT_Y_MARGIN = 10;

    private static final String LABEL_FONT_FAMILY = "sans-serif";

    private static final int LABEL_FONT_SIZE = 14;

    private static final double TRACE_STROKE = 2.0;

    private static final String HEAP_SIZE_COLOR = "red";

    private static final String SVG_REQ_COUNT_COLOR = "blue";

    private static final String BMP_REQ_COUNT_COLOR = "green";

    private static final String DISP_COUNT_COLOR = "fuchsia";

    private static final String RESTART_COLOR = "orange";

    private static final String TIME_COLOR = "black";

    private static final int LEGEND_WIDTH = 150;

    private static final String SQL =
            "SELECT INSTANT,HEAP_SIZE,SVG_REQ_COUNT,BMP_REQ_COUNT,DISPLAY_COUNT " +
            "FROM SYN_STAT " +
            "WHERE INSTANT BETWEEN TIMESTAMP('%1$s:00') AND TIMESTAMP('%2$s:59') " +
            "ORDER BY 1 DESC";

    private static final Logger log = Logger.getLogger( PerPlotServlet.class.getName());

    private final DateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );
    private final DocumentBuilder builder;
    private LogDb logDb;

    public PerPlotServlet() {
        try {
            DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
            fac.setNamespaceAware( true );
            builder = fac.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException( ex );
        }
    }

    @Override
    public void init( ServletConfig conf ) throws ServletException {
        super.init( conf );
        logDb = (LogDb)getServletContext().getAttribute( "log-db" );
        if (logDb == null) {
            throw new ServletException( "Logging database is not avaliable" );
        }
        log.info( "Initialized " + this );
    }

    @Override
    public void destroy() {
        logDb = null;
        super.destroy();
        log.info( "Destroyed " + this );
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public void doGet( HttpServletRequest req, HttpServletResponse res )
            throws ServletException, IOException {

        Date t1;
        String p_t1 = req.getParameter( "t1" );
        try {
            t1 = (p_t1 != null && !p_t1.isEmpty())
                ? dateFormat.parse( p_t1 )
                : null;
        } catch (ParseException ex) {
            res.sendError( 400, "Invalid time format: " + p_t1 );
            return;
        }

        Date t2;
        String p_t2 = req.getParameter( "t2" );
        try {
            t2 = (p_t2 != null && !p_t2.isEmpty())
                ? dateFormat.parse( p_t2 )
                : null;
        } catch (ParseException ex) {
            res.sendError( 400, "Invalid time format: " + p_t2 );
            return;
        }

        if (t2 == null) {
            t2 = new Date();
        }
        if (t1 == null) {
            t1 = new Date( t2.getTime() - DEFAULT_PERIOD );
        }

        if (!t2.after( t1 )) {
            res.sendError( 400, "Invalid time period" );
            return;
        }
        
        try {

            List<LogRecord> dataSet = readData( t1, t2 );

            Document doc = createGraph( dataSet, t1, t2 );

            res.setContentType( "image/svg+xml" );
            res.setDateHeader( "Last-Modified", System.currentTimeMillis());

            OutputStream out = res.getOutputStream();
            try {
                HTTPUtil.output( doc, out );
            } finally {
                out.close();
            }

        } catch (SQLException ex) {

            log.log( Level.SEVERE, "Data retrieval failed", ex );
            throw new ServletException( ex );

        }

    }

    private List<LogRecord> readData( Date startTime, Date endTime ) throws SQLException {
        Connection con = logDb.getConnection();
        String sql = String.format( SQL,
                dateFormat.format( startTime ),
                dateFormat.format( endTime )
        );
        List<LogRecord> res = new ArrayList<LogRecord>();
        ResultSet rs = con.createStatement().executeQuery( sql );
        try {
            while (rs.next()) {
                res.add( new LogRecord(
                    rs.getTimestamp( 1 ).getTime(),
                    rs.getObject( 2 ) == null,
                    rs.getInt( 2 ),
                    rs.getInt( 3 ),
                    rs.getInt( 4 ),
                    rs.getInt( 5 )
                ));
            }
        } finally {
            rs.close();
        }
        return res;
    }

    private Document createGraph( List<LogRecord> dataSet, Date startTime, Date endTime ) {

        Document doc = builder.newDocument();

        Element root = doc.createElementNS( XMLNS_SVG, "svg" );
        doc.appendChild( root );
        //root.setAttribute( "shape-rendering", "crispEdges" );
        root.setAttribute( "width", "100%" );
        root.setAttribute( "height", "100%" );
        root.setAttribute( "stroke", "none" );
        root.setAttribute( "fill", "none" );
        root.setAttribute( "viewBox", format( "%d %d %d %d",
                PICTURE_BOUNDS.x,
                PICTURE_BOUNDS.y,
                PICTURE_BOUNDS.width,
                PICTURE_BOUNDS.height
        ));

        Element title = doc.createElement( "title" );
        root.appendChild( title );
        title.appendChild( doc.createTextNode(
                "Synoptic Server Performance" +
                " from " + dateFormat.format( startTime ) +
                " to " + dateFormat.format( endTime ) + "."
        ));

        Element pictRect = doc.createElement( "rect" );
        root.appendChild( pictRect );
        pictRect.setAttribute( "fill", PICTURE_BACKGROUND );
        pictRect.setAttribute( "x", "0" );
        pictRect.setAttribute( "y", "0" );
        pictRect.setAttribute( "width", format( "%d", PICTURE_BOUNDS.width ));
        pictRect.setAttribute( "height", format( "%d", PICTURE_BOUNDS.height ));

        Element plot = doc.createElement( "g" );
        root.appendChild( plot );
        plot.setAttribute( "transform", format( "translate(%d,%d)",
                PLOT_BOUNDS.x + PICTURE_BOUNDS.x,
                PLOT_BOUNDS.y + PICTURE_BOUNDS.y
        ));

        Element plotBackground = doc.createElement( "rect" );
        plot.appendChild( plotBackground );
        plotBackground.setAttribute( "fill", PLOT_BACKGROUND );
        plotBackground.setAttribute( "x", "0" );
        plotBackground.setAttribute( "y", "0" );
        plotBackground.setAttribute( "width", format( "%d", PLOT_BOUNDS.width ));
        plotBackground.setAttribute( "height", format( "%d", PLOT_BOUNDS.height ));

        Element xGrid = doc.createElement( "path" );
        plot.appendChild( xGrid );
        xGrid.setAttribute( "stroke", GRID_COLOR );
        xGrid.setAttribute( "stroke-width", format( "%.2f", GRID_STROKE ));
        StringBuilder xGridPath = new StringBuilder();
        double sx = PLOT_BOUNDS.getWidth() / (GRID_X_COUNT + 1);
        for (int i = 1; i <= GRID_X_COUNT; ++i) {
            double x = sx * i;
            xGridPath.append( format( "M%.2f 0v%d ", x, PLOT_BOUNDS.height ));
        }
        xGrid.setAttribute( "d", xGridPath.toString());

        Element yGrid = doc.createElement( "path" );
        plot.appendChild( yGrid );
        yGrid.setAttribute( "stroke", GRID_COLOR );
        yGrid.setAttribute( "stroke-width", format( "%.2f", GRID_STROKE ));
        StringBuilder yGridPath = new StringBuilder();
        double sy = PLOT_BOUNDS.getHeight() / (GRID_Y_COUNT + 1);
        for (int i = 1; i <= GRID_Y_COUNT; ++i) {
            double y = sy * i;
            yGridPath.append( format( "M0 %.2fh%d ", y, PLOT_BOUNDS.width ));
        }
        yGrid.setAttribute( "d", yGridPath.toString());

        TimeLimits timeLimits = getTimeLimits( startTime, endTime );

        Element timeLabels = createTimeLabels( doc, timeLimits );
        plot.appendChild( timeLabels );

        int legendStep = PLOT_BOUNDS.width / 5;
        int legendOffset = (legendStep - LEGEND_WIDTH) / 2;

        plot.appendChild( createRestartBars(
                doc,
                dataSet,
                timeLimits,
                RESTART_COLOR
        ));

        plot.appendChild( createLegend(
                doc,
                legendOffset + legendStep * 0,
                RESTART_COLOR,
                "Restart"
        ));

        LogReader heapSizeReader = new HeapSizeReader();
        Limits heapSizeLimits = getLimits( dataSet, heapSizeReader );

        plot.appendChild( createTrace(
                doc,
                dataSet,
                heapSizeReader,
                heapSizeLimits,
                timeLimits,
                HEAP_SIZE_COLOR
        ));

        plot.appendChild( createLabels(
                doc,
                heapSizeLimits,
                0,
                HEAP_SIZE_COLOR
        ));

        plot.appendChild( createLegend(
                doc,
                legendOffset + legendStep * 1,
                HEAP_SIZE_COLOR,
                "Heap Size"
        ));

        LogReader svgReqCountReader = new SvgReqCountReader();
        Limits svgReqCountLimits = getLimits( dataSet, svgReqCountReader );

        plot.appendChild( createTrace(
                doc,
                dataSet,
                svgReqCountReader,
                svgReqCountLimits,
                timeLimits,
                SVG_REQ_COUNT_COLOR
        ));

        plot.appendChild( createLabels(
                doc,
                svgReqCountLimits,
                LABEL_FONT_SIZE,
                SVG_REQ_COUNT_COLOR
        ));

        plot.appendChild( createLegend(
                doc,
                legendOffset + legendStep * 2,
                SVG_REQ_COUNT_COLOR,
                "SVG Request Count"
        ));

        LogReader bmpReqCountReader = new BmpReqCountReader();
        Limits bmpReqCountLimits = getLimits( dataSet, bmpReqCountReader );

        plot.appendChild( createTrace(
                doc,
                dataSet,
                bmpReqCountReader,
                bmpReqCountLimits,
                timeLimits,
                BMP_REQ_COUNT_COLOR
        ));

        plot.appendChild( createLabels(
                doc,
                bmpReqCountLimits,
                LABEL_FONT_SIZE * 2,
                BMP_REQ_COUNT_COLOR
        ));

        plot.appendChild( createLegend(
                doc,
                legendOffset + legendStep * 3,
                BMP_REQ_COUNT_COLOR,
                "BMP Request Count"
        ));

        LogReader dispCountReader = new DispCountReader();
        Limits dispCountLimits = getLimits( dataSet, dispCountReader );

        plot.appendChild( createTrace(
                doc,
                dataSet,
                dispCountReader,
                dispCountLimits,
                timeLimits,
                DISP_COUNT_COLOR
        ));

        plot.appendChild( createLabels(
                doc,
                dispCountLimits,
                LABEL_FONT_SIZE * 3,
                DISP_COUNT_COLOR
        ));

        plot.appendChild( createLegend(
                doc,
                legendOffset + legendStep * 4,
                DISP_COUNT_COLOR,
                "Display Count"
        ));

        Element plotBorder = doc.createElement( "rect" );
        plot.appendChild( plotBorder );
        plotBorder.setAttribute( "stroke", BORDER_COLOR );
        plotBorder.setAttribute( "stroke-width", format( "%.2f", BOEDER_STROKE ));
        plotBorder.setAttribute( "x", "0" );
        plotBorder.setAttribute( "y", "0" );
        plotBorder.setAttribute( "width", format( "%d", PLOT_BOUNDS.width ));
        plotBorder.setAttribute( "height", format( "%d", PLOT_BOUNDS.height ));

        Element ticks = doc.createElement( "path" );
        plot.appendChild( ticks );
        ticks.setAttribute( "stroke", TICK_COLOR );
        ticks.setAttribute( "stroke-width", format( "%.2f", TICK_STROKE ));
        StringBuilder tickPath = new StringBuilder();
        tickPath.append( format( "M%d %dh%d",
                -TICK_SIZE,
                PLOT_Y_MARGIN,
                2 * TICK_SIZE
        ));
        tickPath.append( format( "M%d %dh%d",
                -TICK_SIZE,
                PLOT_BOUNDS.height - PLOT_Y_MARGIN,
                2 * TICK_SIZE
        ));
        tickPath.append( format( "M%d %dv%d",
                PLOT_X_MARGIN,
                PLOT_BOUNDS.height -TICK_SIZE,
                2 * TICK_SIZE
        ));
        tickPath.append( format( "M%d %dv%d",
                PLOT_BOUNDS.width - PLOT_X_MARGIN,
                PLOT_BOUNDS.height -TICK_SIZE,
                2 * TICK_SIZE ));
        ticks.setAttribute( "d", tickPath.toString());

        return doc;

    }

    private Limits getLimits( List<LogRecord> dataSet, LogReader reader ) {
        if (dataSet.isEmpty()) {
            return new Limits( 0, 1 );
        }
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (LogRecord rec : dataSet) {
            int val = reader.get( rec );
            if (val < min) {
                min = val;
            }
            if (val > max) {
                max = val;
            }
        }
        if (min == max) {
            if (min == 0) {
                return new Limits( 0, 1 );
            }
            min -= 1;
            max += 1;
        }
        return new Limits( min, max );
    }

    private TimeLimits getTimeLimits( Date startTime, Date endTime ) {
        long min = startTime.getTime();
        long max = endTime.getTime();
        if (min == max) {
            min = min - 3600000; // - 1hr
            max = max + 3600000; // + 1hr
        }
        return new TimeLimits( min, max );
    }

    private Element createTrace(
            Document doc,
            List<LogRecord> dataSet,
            LogReader reader,
            Limits limits,
            TimeLimits timeLimits,
            String color ) {

        Element e = doc.createElement( "path" );
        e.setAttribute( "stroke", color );
        e.setAttribute( "stroke-width", format( "%.2f", TRACE_STROKE ));

        if (dataSet.isEmpty()) {
            return e;
        }

        double t0 = timeLimits.min;
        double t1 = timeLimits.max;
        double sx = (PLOT_BOUNDS.width - 2.0 * PLOT_X_MARGIN) / (t1 - t0);

        double v0 = limits.min;
        double v1 = limits.max;
        double sy = (PLOT_BOUNDS.height - 2.0 * PLOT_Y_MARGIN) / (v1 - v0);

        StringBuilder path = new StringBuilder();
        boolean paintOn = false;

        for (LogRecord rec : dataSet) {

            if (rec.restart) {
                paintOn = false;
                continue;
            }

            if (paintOn) {
                path.append( "L" );
            } else {
                path.append( "M" );
                paintOn = true;
            }

            double x = PLOT_X_MARGIN + (rec.instant - t0) * sx;
            path.append( format( "%.2f", x ));

            path.append( ' ' );

            double y = PLOT_BOUNDS.height - PLOT_Y_MARGIN - (reader.get( rec ) - v0) * sy;
            path.append( format( "%.2f", y ));

        }

        e.setAttribute( "d", path.toString());

        return e;

    }

    private Element createRestartBars(
            Document doc,
            List<LogRecord> dataSet,
            TimeLimits timeLimits,
            String color ) {

        Element e = doc.createElement( "g" );
        e.setAttribute( "fill", color );

        if (dataSet.isEmpty()) {
            return e;
        }

        double t0 = timeLimits.min;
        double t1 = timeLimits.max;
        double sx = (PLOT_BOUNDS.width - 2.0 * PLOT_X_MARGIN) / (t1 - t0);
        double w = sx * 60000.0;

        for (LogRecord rec : dataSet) {
            if (!rec.restart) {
                continue;
            }
            Element r = doc.createElement( "rect" );
            e.appendChild( r );
            double x = PLOT_X_MARGIN + (rec.instant - t0) * sx;
            r.setAttribute( "x", format( "%.2f", x ));
            r.setAttribute( "width", format( "%.2f", w ));
            r.setAttribute( "y", "0" );
            r.setAttribute( "height", format( "%d", PLOT_BOUNDS.height ));
        }

        return e;

    }

    private Element createLabels( Document doc, Limits limits, int offset, String color ) {

        Element e = doc.createElement( "g" );
        e.setAttribute( "font-family", LABEL_FONT_FAMILY );
        e.setAttribute( "font-size", format( "%d", LABEL_FONT_SIZE ));
        e.setAttribute( "fill", color );

        Element min = doc.createElement( "text" );
        e.appendChild( min );
        min.setAttribute( "text-anchor", "end" );
        min.setAttribute( "x", format( "%d", -(TICK_SIZE + 1) ));
        min.setAttribute( "y", format( "%d", PLOT_BOUNDS.height - offset + LABEL_FONT_SIZE / 2 ));
        min.appendChild( doc.createTextNode( format( "%d", limits.min )));

        Element max = doc.createElement( "text" );
        e.appendChild( max );
        max.setAttribute( "text-anchor", "end" );
        max.setAttribute( "x", format( "%d", -(TICK_SIZE + 1) ));
        max.setAttribute( "y", format( "%d", 0 + offset + LABEL_FONT_SIZE / 2 ));
        max.appendChild( doc.createTextNode( format( "%d", limits.max )));

        return e;

    }

    private Element createTimeLabels( Document doc, TimeLimits limits ) {

        Element e = doc.createElement( "g" );
        e.setAttribute( "font-family", LABEL_FONT_FAMILY );
        e.setAttribute( "font-size", format( "%d", LABEL_FONT_SIZE ));
        e.setAttribute( "fill", TIME_COLOR );

        Element min = doc.createElement( "text" );
        e.appendChild( min );
        min.setAttribute( "text-anchor", "start" );
        min.setAttribute( "x", format( "%d", PLOT_X_MARGIN ));
        min.setAttribute( "y", format( "%d", PLOT_BOUNDS.height + TICK_SIZE + LABEL_FONT_SIZE ));
        min.appendChild( doc.createTextNode( dateFormat.format( new Date( limits.min ))));

        Element max = doc.createElement( "text" );
        e.appendChild( max );
        max.setAttribute( "text-anchor", "end" );
        max.setAttribute( "x", format( "%d", PLOT_BOUNDS.width - PLOT_X_MARGIN ));
        max.setAttribute( "y", format( "%d", PLOT_BOUNDS.height + TICK_SIZE + LABEL_FONT_SIZE ));
        max.appendChild( doc.createTextNode( dateFormat.format( new Date( limits.max ))));

        return e;

    }

    private Element createLegend( Document doc, int xOffset, String color, String text ) {

        Element e = doc.createElement( "g" );
        e.setAttribute( "font-family", LABEL_FONT_FAMILY );
        e.setAttribute( "font-size", format( "%d", LABEL_FONT_SIZE ));
        e.setAttribute( "transform", format( "translate(%d,%d)",
                xOffset,
                PLOT_BOUNDS.height + 4 * LABEL_FONT_SIZE
        ));

        Element eText = doc.createElement( "text" );
        e.appendChild( eText );
        eText.setAttribute( "text-anchor", "middle" );
        eText.setAttribute( "fill", color );
        eText.setAttribute( "x", format( "%d", LEGEND_WIDTH / 2 ));
        eText.setAttribute( "y", "0" );
        eText.appendChild( doc.createTextNode( text ));

        Element line = doc.createElement( "path" );
        e.appendChild( line );
        line.setAttribute( "stroke", color );
        line.setAttribute( "stroke-width", format( "%.2f", TRACE_STROKE ));
        line.setAttribute( "d", format( "M0 %dh%d", 2 * (int)TRACE_STROKE, LEGEND_WIDTH ));

        return e;

    }
    
    private static class LogRecord {

        final long instant;
        final boolean restart;
        final int heapSize, svgReqCount, bmpReqCount, dispCount;

        LogRecord( long instant, boolean restart,
                int heapSize, int svgReqCount, int bmpReqCount, int dispCount ) {
            this.instant = instant;
            this.restart = restart;
            this.heapSize = heapSize;
            this.svgReqCount = svgReqCount;
            this.bmpReqCount = bmpReqCount;
            this.dispCount = dispCount;
        }

    }

    private static final class Limits {

        final int min, max;

        Limits() {
            this( 0, 1 );
        }

        Limits( int min, int max ) {
            this.min = min;
            this.max = max;
        }

    }

    private static final class TimeLimits {

        final long min, max;

        TimeLimits( long min, long max ) {
            this.min = min;
            this.max = max;
        }

    }

    private static interface LogReader {

        int get( LogRecord data );

    }

    private static class HeapSizeReader implements LogReader {

        @Override
        public int get( LogRecord data ) {
            return data.heapSize;
        }

    }

    private static class SvgReqCountReader implements LogReader {

        @Override
        public int get( LogRecord data ) {
            return data.svgReqCount;
        }

    }

    private static class BmpReqCountReader implements LogReader {

        @Override
        public int get( LogRecord data ) {
            return data.bmpReqCount;
        }

    }

    private static class DispCountReader implements LogReader {

        @Override
        public int get( LogRecord data ) {
            return data.dispCount;
        }

    }

}
