<%-- (c) 2001-2010 Fermi Research Alliance --%>
<%-- $Id: repo.jsp,v 1.3 2010/09/09 21:41:41 apetrov Exp $ --%>

<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%  
    String contextPath = request.getContextPath();
    String collapsedIcon = contextPath + "/images/collapsed.gif";
    String expandedIcon = contextPath + "/images/expanded.gif";
    String displayIcon = contextPath + "/images/display.gif";
%>    

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html> 
<head>
    <title>Display Index - <%= request.getPathInfo() %></title>
    <script type="text/javascript">
        <!--
        var id = 0;
        function init() { 
            var tree = document.getElementById( "tree" );
            tree.innerHTML = "Loading Display Index...";
            try {
                var req = (typeof(XMLHttpRequest) != "undefined")
                    ? new XMLHttpRequest()
                    : new ActiveXObject( "Msxml2.XMLHTTP" );
                req.onreadystatechange = function() {
                    id = 0;
                    try {
                        if (req.readyState == 4 && req.responseXML) {
                            var e = req.responseXML.documentElement;
                            var ctx = e.getAttribute( "context" );
                            if (ctx.length > 0 && ctx.charAt( 0 ) != "/") {
                                ctx = "/" + ctx;
                            }
                            tree.innerHTML = branch( e, ctx );
                        }
                    } catch (zx) {
                        tree.innerHTML = "Failed To Load Display Index";
                    }
                };
                req.open( "GET", "<%= contextPath + request.getServletPath() + request.getPathInfo() %>.xml", true );
                req.send( null );
            } catch (ex) {  
                alert( ex );
            }
        }
        function branch( e, ctx ) {
            var buf = (id == 0) ? "<div>" : "<div class=\"collapsed\">"
            for (var n = e.firstChild; n != null; n = n.nextSibling) {
                if (n.nodeType != 1) {
                    continue;
                }
                var name = n.getAttribute( "name" );
                if (n.nodeName == "directory") {
                    var _id = ++id;
                    var s = branch( n, ctx + "/" + name );
                    if (s) {
                        buf += "<div id=\"" + _id + "\" class=\"dir\">" +
                               "<a href=\"javascript:flip(" + _id + ")\">" +
                               "<img src=\"<%= collapsedIcon %>\">" +  name + "<\/a><\/div>" + s;
                    }
                } else if (n.nodeName == "display") {
                    buf += "<div class=\"item\">" +
                           "<a href=\"javascript:open_display(\'" + ctx + "/" + name + "\')\">" +
                            "<img src=\"<%= displayIcon %>\">" + name + "<\/a><\/div>";
                }
            }
            return buf + "<\/div>";
        }
        function flip( id ) {
            var n0 = document.getElementById( id );
            if (n0 == null) {
                return;
            }
            var n1 = n0.nextSibling;
            if (n1 == null) {
                return;
            }
            var f = n1.className == "expanded";
            n1.className = f ? "collapsed" : "expanded";
            var img = n0.getElementsByTagName( "img" );
            if (img == null || img.length < 1) {
                return;
            }
            img[ 0 ].src = f ? "<%= collapsedIcon %>" : "<%= expandedIcon %>";
        }
        function open_display( name ) {
            var windowName = name.replace( /\//g, "_" );
            window.open( "<%= contextPath + request.getServletPath() %>" + name, windowName, 
                "width=900,height=700,scrollbars=yes,resizable=yes,menubar=yes,toolbar=yes"
            );
        }
    // -->
    </script>
    <style type="text/css">
        html, body { height: 100%; margin: 0; padding: 0; }
        body { margin: 10px; font-family: Verdana,Arial,Sans-Serif; font-size: 10pt;
            background-color: #fff; color: #000; text-align: left; }
        .item { font-weight: bold; padding: 0.15em 0; }
        .dir { font-weight: normal; padding: 0.15em 0; }
        .expanded { padding-left: 2em; display: block; }
        .collapsed { display: none; }
        img { border: none; }
        a { text-decoration: none; }
        .item a { color: #00c; }
        .dir a { color: #333; }
        a:hover { color: #c00; background-color: #ffa;
            outline-width: 1px; outline-style: dotted; outline-color: #c00; }
    </style>
</head>
<body onload="init()">
    <div id="tree"></div>
</body>
</html>
