<%-- (c) 2001-2010 Fermi Research Alliance --%>
<%-- $Id: quarantine.jsp,v 1.2 2010/09/14 20:50:44 apetrov Exp $ --%>

<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="java.sql.Statement" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="gov.fnal.controls.webapps.syndi.LogDb" %>
<%!
    private static final int MAX_ROW_COUNT = 200;
    private static final DateFormat FORMAT = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );
    private static final String SELECT_SQL = "SELECT ADD_TIME, DISPLAY FROM SYN_QUARANTINE ORDER BY 1";
    private static final String DELETE_SQL = "DELETE FROM SYN_QUARANTINE WHERE DISPLAY='%s'";
%>
<%
    LogDb logDb = (LogDb)application.getAttribute( "log-db" );
    if (logDb == null) {
        response.sendError( 500, "Logging Database Unavaliable" );
        return;
    }
    if ("post".equalsIgnoreCase( request.getMethod())) {
        String[] displays = request.getParameterValues( "d" );
        if (displays != null && displays.length > 0) {
            Connection con = logDb.getConnection();
            try {
                Statement stt = con.createStatement();
                try {
                    for (String name : displays) {
                        stt.executeUpdate( String.format( DELETE_SQL, name ));
                    }
                } finally {
                    stt.close();
                }
            } catch (SQLException ex) {
                response.sendError( 500, ex.getMessage());
                return;
            } finally {
                con.close();
            }
        }
    }
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>Synoptic - Quarantine</title>
    <link rel="stylesheet" type="text/css" href="../style.css">
</head>
    
<body>
    <div id="container">
        
        <h1>Quarantine</h1>
        
        <div id="navigation">
            <a href="..">Home</a> |
            <a href="performance-log.jsp">Performance Log</a> |
            <a href="performance-plot.jsp">Performance Plot</a> |
            <a href="access-log.jsp">Access Log</a> |
            <a href="quarantine.jsp" class="current">Quarantine</a>
        </div>
        
<%
    Connection con = logDb.getConnection();
    try {
        ResultSet rs = con.createStatement().executeQuery( SELECT_SQL );
        if (rs.next()) {
            int count = 0;
%>        
            <form action="" method="post">
                <table>
                    <tr class="header">
                        <th style="width:5%">&nbsp;</th>
                        <th style="width:10%">Time</th>
                        <th style="width:85%">Display Name</th>
                    </tr>
<%
            Date time;
            do {
                time =  new Date( rs.getTimestamp( 1 ).getTime());
%>        
                    <tr class="<%= (count % 2 == 0) ? "even" : "odd" %>">
                        <td class="center"><input type="checkbox" name="d" value="<%= rs.getString( 2 ) %>"></td>
                        <td class="center"><%= FORMAT.format( time ) %></td>
                        <td class="left"><%= rs.getString( 2 ) %></td>
                    </tr>
<%  
            } while (++count < MAX_ROW_COUNT && rs.next());
%>        
                </table>
                <div class="reccount">1&ndash;<%= count %></div>
                <div style="margin-bottom:10px;margin-top:10px;">
                    <input type="submit" value="Release Selected Displays">
                </div>    
            </form>
<%
        } else {
%>            
            <div class="warning">Quarantine Is Empty</div>
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