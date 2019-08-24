<%-- (c) 2001-2010 Fermi Research Alliance --%>
<%-- $Id: performance-log.jsp,v 1.2 2010/09/14 20:50:44 apetrov Exp $ --%>

<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="gov.fnal.controls.webapps.syndi.LogDb" %>
<%!
    private static final int MAX_ROW_COUNT = 60;
    private static final DateFormat FORMAT = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );
    private static final String SQL = 
            "SELECT INSTANT,HEAP_SIZE,SVG_REQ_COUNT,BMP_REQ_COUNT,DISPLAY_COUNT,START_COUNT," +
                "STOP_COUNT,ABORT_COUNT,WORK_TIME " +
            "FROM SYN_STAT " +
            "WHERE INSTANT BETWEEN TIMESTAMP('%1$s:00') AND TIMESTAMP('%2$s:59') " +
            "ORDER BY 1 DESC";
%>
<%
    LogDb logDb = (LogDb)application.getAttribute( "log-db" );
    if (logDb == null) {
        response.sendError( 500, "Logging Database Unavaliable" );
        return;
    }
    String p_t1 = request.getParameter( "t1" );
    if (p_t1 == null) {
        p_t1 = "";
    }
    String p_t2 = request.getParameter( "t2" );
    if (p_t2 == null) {
        p_t2 = "";
    }
%>    

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>Synoptic - Performance Log</title>
    <link rel="stylesheet" type="text/css" href="../style.css">
    <style type="text/css">
        td, th { width: 11%; }
        tr.restart { background-color: #f99; }
        td.restart { letter-spacing: 1em; }
    </style>
</head>
    
<body>
    <div id="container">
        
        <h1>Performance Log</h1>
        
        <div id="navigation">
            <a href="..">Home</a> |
            <a href="performance-log.jsp" class="current">Performance Log</a> |
            <a href="performance-plot.jsp">Performance Plot</a> |
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
        
<%
    Connection con = logDb.getConnection();
    try {
        String sql = String.format( SQL, 
            "".equals( p_t1 ) ? "1970-01-01 00:00" : p_t1,
            "".equals( p_t2 ) ? FORMAT.format( new Date()) : p_t2
        );
        ResultSet rs = con.createStatement().executeQuery( sql );
        if (rs.next()) {
            int count = 0;
%>        
            <table>
                <tr>
                    <th>Time</th>
                    <th>Heap Size, kB</th>
                    <th>SVG Request Count</th>
                    <th>Bitmap Request Count</th>
                    <th>Display Count</th>
                    <th>Start Count</th>
                    <th>Stop Count</th>
                    <th>Abort Count</th>
                    <th>Workload, %</th>
                </tr>
<%
            Date lastTime;
            do {
                lastTime =  new Date( rs.getTimestamp( 1 ).getTime());
                if (rs.getObject( 2 ) != null) {
%>        
                <tr>
                    <td class="center"><%= FORMAT.format( lastTime ) %></td>
                    <td class="right"><%= rs.getInt( 2 ) %></td>
                    <td class="right"><%= rs.getInt( 3 ) %></td>
                    <td class="right"><%= rs.getInt( 4 ) %></td>
                    <td class="right"><%= rs.getInt( 5 ) %></td>
                    <td class="right"><%= rs.getInt( 6 ) %></td>
                    <td class="right"><%= rs.getInt( 7 ) %></td>
                    <td class="right"><%= rs.getInt( 8 ) %></td>
                    <td class="right"><%= rs.getInt( 9 ) %></td>
                </tr>
<%  
                } else {
%>        
                <tr class="restart">
                    <td class="center"><%= FORMAT.format( lastTime ) %></td>
                    <td colspan="8" class="center restart">RESTART</td>
                </tr>
<%  
                }
            } while (++count < MAX_ROW_COUNT && rs.next());
%>        
            </table>
            <div class="reccount">1&ndash;<%= count %></div>
<%
        } else {
%>            
            <div class="warning">No Records</div>
<%            
        }
    } catch (SQLException ex) {
%>        
            <div class="warning"><%= ex.getMessage() %></div>
<%            
    }
%>

        <div id="footer"><%= System.getProperty( "Synoptic.disclaimer", "" ) %></div>

    </div>
</body>
</html>