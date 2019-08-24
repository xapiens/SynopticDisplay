<%-- (c) 2001-2010 Fermi Research Alliance --%>
<%-- $Id: performance-plot.jsp,v 1.2 2010/09/14 20:50:44 apetrov Exp $ --%>

<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.ParseException" %>
<%!
    private static final DateFormat FORMAT = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );
    private static final long DEFAULT_PERIOD = 1 * 24 * 60 * 60 * 1000; // 1 days in milliseconds
%>
<%
    String p_t1 = request.getParameter( "t1" );
    if (p_t1 == null) {
        p_t1 = "";
    }
    String p_t2 = request.getParameter( "t2" );
    if (p_t2 == null) {
        p_t2 = "";
    }
    Date t1,t2;
    try {
        t1 = (p_t1 != null && !p_t1.isEmpty())
            ? FORMAT.parse( p_t1 )
            : null;
    } catch (ParseException ex) {
        response.sendError( 400, "Invalid time format: " + p_t1 );
        return;
    }
    try {
        t2 = (p_t2 != null && !p_t2.isEmpty())
            ? FORMAT.parse( p_t2 )
            : null;
    } catch (ParseException ex) {
        response.sendError( 400, "Invalid time format: " + p_t2 );
        return;
    }
    if (t2 == null) {
        t2 = new Date();
    }
    if (t1 == null) {
        t1 = new Date( t2.getTime() - DEFAULT_PERIOD );
    }
    if (!t2.after( t1 )) {
        response.sendError( 400, "Invalid time period" );
        return;
    }
    long period = t2.getTime() - t1.getTime();
    String tp1 = FORMAT.format( new Date( t1.getTime() - period ));
    String tp2 = FORMAT.format( new Date( t2.getTime() - period ));
    String tn1 = FORMAT.format( new Date( t1.getTime() + period ));
    String tn2 = FORMAT.format( new Date( t2.getTime() + period ));
    String td1 = FORMAT.format( new Date( System.currentTimeMillis() - 24 * 60 * 60 * 1000 ));
    String th1 = FORMAT.format( new Date( System.currentTimeMillis() -      60 * 60 * 1000 ));
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>Synoptic - Performance Plot</title>
    <link rel="stylesheet" type="text/css" href="../style.css">
    <style type="text/css">
        table.navi, table.navi td, table.navi th { border: none; padding: 0; }
        tr.restart { background-color: #f99; }
        td.restart { letter-spacing: 1em; }
    </style>
</head>
    
<body>
    <div id="container">
        
        <h1>Performance Plot</h1>
        
        <div id="navigation">
            <a href="..">Home</a> |
            <a href="performance-log.jsp">Performance Log</a> |
            <a href="performance-plot.jsp" class="current">Performance Plot</a> |
            <a href="access-log.jsp">Access Log</a> |
            <a href="quarantine.jsp">Quarantine</a>
        </div>
        
        <form method="get" action="">
            <table class="param">
                <tr>
                    <th>Time:</th>
                    <td><input type="text" name="t1" size="16" maxlength="16" value="<%= p_t1 %>"></td>
                    <td>&mdash;</td>
                    <td><input type="text" name="t2" size="16" maxlength="16" value="<%= p_t2 %>"></td>
                    <td class="tip">YYYY-MM-DD HH:mm</td>
                </tr>
                <tr>
                    <th>&nbsp;</th>
                    <td><input type="submit" value="Submit Query"></td>
                </tr>
            </table>
        </form>

        <table class="navi">
            <tr>
                <td style="text-align:left;">
                    <a href="?t1=<%= tp1 %>&amp;t2=<%= tp2 %>">&lt;&lt;&nbsp;Previous&nbsp;Period</a>
                </td>
                <td style="text-align:center;">
                    <a href="?t1=<%= td1 %>">Last&nbsp;Day</a>
                    |
                    <a href="?t1=<%= th1 %>">Last&nbsp;Hour</a>
                </td>
                <td style="text-align:right;">
                    <a href="?t1=<%= tn1 %>&amp;t2=<%= tn2 %>">Next&nbsp;Period&nbsp;&gt;&gt;</a>
                </td>
            </tr>
        </table>

        <embed id="display" type="image/svg+xml" height="500" width="700"
               src="performance-plot.svg?t1=<%= p_t1 %>&amp;t2=<%= p_t2 %>"></embed>

        <div id="footer"><%= System.getProperty( "Synoptic.disclaimer", "" ) %></div>

    </div>

</body>
</html>