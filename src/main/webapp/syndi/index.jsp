<%-- (c) 2001-2010 Fermi Research Alliance --%>
<%-- $Id: index.jsp,v 1.3 2010/09/14 20:50:44 apetrov Exp $ --%>

<%@ page language="java" contentType="text/html; charset=UTF-8" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>Synoptic</title>
    <link rel="stylesheet" type="text/css" href="style.css">
    <style type="text/css">
        iframe { border: 1px solid #ccc; width: 95%; height: 400px; margin: 1em 0; }
    </style>
</head>
<body>
    <div id="container">
        <h1>Synoptic</h1>
        <div id="navigation">
            <a href="" class="current">Home</a> |
            <a href="admin/performance-log.jsp">Performance Log</a> |
            <a href="admin/performance-plot.jsp">Performance Plot</a> |
            <a href="admin/access-log.jsp">Access Log</a> |
            <a href="admin/quarantine.jsp">Quarantine</a>
        </div>
        <div style="text-align:center;">
            <iframe src="display" frameborder="0"></iframe>
        </div>
        <div id="footer"><%= System.getProperty( "Synoptic.disclaimer", "" ) %></div>
    </div>
</body>
</html>