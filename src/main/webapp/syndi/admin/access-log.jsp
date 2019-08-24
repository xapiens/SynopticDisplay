<%-- (c) 2001-2010 Fermi Research Alliance --%>
<%-- $Id: access-log.jsp,v 1.2 2010/09/14 20:50:44 apetrov Exp $ --%>

<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="java.sql.Timestamp" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="gov.fnal.controls.webapps.syndi.LogDb" %>
<%!
    private static final int MAX_ROW_COUNT = 200;
    private static final long MAX_IDLE_TIME = 60; // seconds
    private static final DateFormat FORMAT = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );
    private static final String SQL = 
            "SELECT START_TIME,HOST,DISPLAY," +
                "CASE WHEN END_TIME IS NULL AND {FN TIMESTAMPDIFF(SQL_TSI_SECOND,HEARTBEAT,CURRENT_TIMESTAMP)}>" + 
                    MAX_IDLE_TIME + " THEN HEARTBEAT ELSE END_TIME END " +
            "FROM SYN_ACCESS " +
            "WHERE START_TIME BETWEEN TIMESTAMP('%1$s:00') AND TIMESTAMP('%2$s:59') " +
            "  AND HOST LIKE '%%'||'%3$s'||'%%' " +
            "  AND LOWER(DISPLAY) LIKE LOWER('%%'||'%4$s'||'%%') " +
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
    String p_h = request.getParameter( "h" );
    if (p_h == null) {
        p_h = "";
    }
    String p_d = request.getParameter( "d" );
    if (p_d == null) {
        p_d = "";
    }
%>    

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>Synoptic - Access Log</title>
    <link rel="stylesheet" type="text/css" href="../style.css">
    <style type="text/css">
        .on { font-weight: bold; }
        .off { font-weight: normal; }
    </style>
</head>
    
<body>
    <div id="container">
        
        <h1>Access Log</h1>
        
        <div id="navigation">
            <a href="..">Home</a> |
            <a href="performance-log.jsp">Performance Log</a> |
            <a href="performance-plot.jsp">Performance Plot</a> |
            <a href="access-log.jsp" class="current">Access Log</a> |
            <a href="quarantine.jsp">Quarantine</a>
        </div>
        
        <form method="get" action="">
            <table class="param">
                <tr>
                    <th>Start Time:</th>
                    <td><input type="text" name="t1" size="16" maxlength="16" value="<%= p_t1 %>"></td>
                    <td>&mdash;</td>
                    <td><input type="text" name="t2" size="16" maxlength="16" value="<%= p_t2 %>"></td>
                    <td class="tip">YYYY-MM-DD HH:mm</td>
                </tr>
                <tr>
                    <th>Client Address:</th>
                    <td colspan="3"><input type="text" name="h" size="16" maxlength="15" value="<%= p_h %>"></td>
                    <td class="tip">partial match</td>
                </tr>
                <tr>
                    <th>Display Name:</th>
                    <td colspan="3"><input type="text" name="d" size="32" maxlength="99" value="<%= p_d %>"></td>
                    <td class="tip">partial match</td>
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
            "".equals( p_t2 ) ? FORMAT.format( new Date()) : p_t2,
            p_h, p_d
        );
        ResultSet rs = con.createStatement().executeQuery( sql );
        if (rs.next()) {
            int count = 0;
%>        
            <table>
                <tr class="header">
                    <th>Start Time</th>
                    <th>End Time</th>
                    <th>Uptime, min</th>
                    <th>Client Address</th>
                    <th>Display Name</th>
                </tr>
<%
            Date startTime, endTime;
            Timestamp endTimeTS;
            int workTime;
            String clazz;
            do {
                startTime =  new Date( rs.getTimestamp( 1 ).getTime());
                endTimeTS = rs.getTimestamp( 4 );
                if (endTimeTS == null) {
                    endTime = null;
                    workTime = (int)(System.currentTimeMillis()/60000L - startTime.getTime()/60000L);
                    clazz = "on";
                } else {
                    endTime = new Date( endTimeTS.getTime());
                    workTime = (int)(endTime.getTime()/60000L - startTime.getTime()/60000L);
                    clazz = "off";
                }
%>        
                <tr class="<%= (count % 2 == 0) ? "even" : "odd" %>">
                    <td class="center"><%= FORMAT.format( startTime ) %></td>
                    <td class="center"><%= (endTime == null) ? "&nbsp;" : FORMAT.format( endTime ) %></td>
                    <td class="center <%= clazz %>"><%= workTime %></td>
                    <td class="center"><%= rs.getString( 2 ) %></td>
                    <td class="left"><%= rs.getString( 3 ) %></td>
                </tr>
<%  
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