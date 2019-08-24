<%-- (c) 2001-2010 Fermi Research Alliance --%>
<%-- $Id: display.jsp,v 1.4 2010/09/14 20:50:44 apetrov Exp $ --%>

<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.Random" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ page import="java.util.regex.Matcher" %>
<%!
    private static final Random RND = new java.security.SecureRandom();
    private static final Pattern REQUEST_PATTERN = Pattern.compile( ".*?([^/]*?)(\\(.*\\))?" );
%>
<%
    String pathInfo = request.getPathInfo();
    Matcher m = REQUEST_PATTERN.matcher( pathInfo );
    if (!m.matches()) {
        response.sendError( 400, "Bad Request: " + pathInfo );
        return;
    }
    String path = m.group( 1 );
    String params = m.group( 2 );
    if (params == null) {
        params = "";
    }
    String clientId = Long.toString( RND.nextInt() & 0xffffffffl, 16 ).toUpperCase();
    long t0 = System.currentTimeMillis();
    String contextPath = request.getContextPath();
    String logo = System.getProperty( "Synoptic.logo" );
    String logoPath = (logo == null) ? null : contextPath + "/" + logo;
%>    

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html> 
<head>
    <title>Synoptic Display - <%= request.getAttribute( "DISPLAY_TITLE" ) %></title>
    <script type="text/javascript">
        <!--
        var default_rate = 3000, responded = true, uri = "<%= path %>.xml<%= params %>?cid=<%= clientId %>";
        function import_node( doc, node ) {
            if (document.importNode) {
                return doc.importNode( node, true );
            }
            switch (node.nodeType) {
                case 1: // ELEMENT
                    var node1 = doc.createElement( node.nodeName );
                    for (var i = 0, len = node.attributes.length; i < len; i++) {
                        var a = node.attributes[ i ];
                        node1.setAttribute( a.nodeName, a.nodeValue );
                    }
                    for (var n = node.firstChild; n != null; n = n.nextSibling ) {
                        var n1 = import_node( doc, n );
                        if (n1 != null) {
                            node1.appendChild( n1 );
                        }
                    }
                    return node1;
                case 3: // TEXT
                    return doc.createTextNode( node.nodeValue );
                case 4: //CDATA_SECTION_NODE
                    return doc.createCDATASection( node.nodeValue );
            }
            return null;
        }
        function init() {
            try {
                var plugin_uri = document.getElementById( "display" ).getSVGDocument().documentURI;
                if (plugin_uri != null) {
                    uri = plugin_uri.replace( /\.svg/, ".xml" );
                }
            } catch (ex) {}
            setTimeout( request_diff, default_rate );
        }
        function request_diff() {
            try {
                var rate = null;
                var req  = new_request();
                if (req && req.status == 200 && req.responseXML) { 
                    if (!responded) {
                        responded = true;
                        document.getElementById( "warning" ).style.display = "none";
                    }
                    rate = req.getResponseHeader( "X-Update-Rate" );
                    if (!apply( req.responseXML )) {
                        location.reload( true );
                        return;
                    }
                } else if (responded) {
                    responded = false;
                    document.getElementById( "warning" ).style.display = "inline";
                }
                setTimeout( request_diff, rate ? rate : default_rate );
            } catch (ex) {  
                alert( ex );
            }
        }
        function new_request() {
            var req = (typeof(XMLHttpRequest) != "undefined")
                ? new XMLHttpRequest()
                : new ActiveXObject( "Msxml2.XMLHTTP" );
            req.open( "GET", uri, false );
            try {
                req.send( null );
                return req;
            } catch (ex) {
                return null;
            }
        }
        function apply( doc ) {
            var e = doc.documentElement;
            if (e.nodeName == "svg") {
                return apply_full( doc );
            } else if (e.nodeName == "dif") {
                return apply_diff( doc );
            } else {
                return false;
            }
        }
        function apply_full( doc ) {
            var svg_doc = document.getElementById( "display" ).getSVGDocument();
            var e1 = import_node( svg_doc, doc.documentElement );
            svg_doc.replaceChild( e1, svg_doc.documentElement );
            return true;
        }
        function apply_diff( doc ) {
            var svg_doc = document.getElementById( "display" ).getSVGDocument();
            for (var n = doc.documentElement.firstChild; n != null; n = n.nextSibling) {
                if (n.nodeType != 1) continue; // 1 is for elements
                var e = svg_doc.getElementById( n.getAttribute( "id" ));
                if (!e) return false;
                if (n.nodeName == "text") {
                    var text = n.firstChild.nodeValue;
                    var textNode = e.lastChild;
                    if (textNode == null) {
                        textNode = svg_doc.createTextNode( value );
                        e.appendChild( textNode );
                    } else {
                        textNode.nodeValue = text;
                    }
                } else if (n.nodeName == "attribute") {
                    e.setAttribute( n.getAttribute( "name" ), n.getAttribute( "value" ));
                }
            }
            return true;
        }
    // -->
    </script>
    <style type="text/css">
        html, body { height: 100%; margin: 0; padding: 0; }
        body { font-family: Verdana,Arial,Sans-Serif; font-size: 10pt; 
            background-color: #fff; color: #000; text-align: center; }
        #footer {  width: 100%; margin: 0; padding: 0; font-size: 0.8em; 
            position: absolute; bottom: 5px; left: 0; }
        #footer div { display: inline; margin: 0 0.25em; }
        a { color: #039; text-decoration: none; }
        a:hover { color: #c00; background-color: #ffa;
            outline-width: 1px; outline-style: dotted; outline-color: #c00; }
        #warning { background-color: #f00; color: #fff;
            position: absolute; bottom: 0; left: 0;
            padding: 0.2em 1em; font-size: 1.2em; font-weight: bold; }
        #logo { position: absolute; bottom: 0; right: 0; padding: 0 1em; }
    </style>
</head>
<body onload="init()">
    <embed id="display" type="image/svg+xml" height="95%" width="95%" src="<%= path %>.svg<%= params %>?cid=<%= clientId %>"></embed>
    <div id="footer">
        <div id="warning" style="display: none">Server Not Responding</div>
        <div>Version <%= System.getProperty( "Synoptic.version", "N/A" ) %></div> |
        <div><a href="<%= path %>.png<%= params %>">Make Snapshot</a></div> |
        <div><%= System.getProperty( "Synoptic.disclaimer", "" ) %></div>
<%
    if (logoPath != null) {
%>
        <div id="logo"><img src="<%= logoPath %>" alt="" hspace="0" vspace="0"/></div>
<%
    }
%>
    </div>
</body>
</html>
