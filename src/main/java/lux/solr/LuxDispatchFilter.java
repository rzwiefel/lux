package lux.solr;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * rewrite URLs of the form:
 * 
 * [[app-prefix][/core-name]]/lux[/xquery-path]?query-string
 * 
 * TO
 * 
 * [[app-prefix][/core-name]]/lux?query-string&lux.xquery=[/xquery-path]
 * 
 */
public class LuxDispatchFilter implements Filter {
    
    private String baseURI;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        baseURI = filterConfig.getInitParameter("base-uri");
        if (baseURI == null) {
            String path;
            if (File.separatorChar == '\\') {
                path = "///" + filterConfig.getServletContext().getRealPath("/").replace('\\', '/');
            } else {
                path = "//" + filterConfig.getServletContext().getRealPath("/");
            }
            // Create a URI since that is supposed to handle quoting of non-URI characters in the path (like spaces)
            URI uri;
            try {
                uri = new URI ("file", path, null);
            } catch (URISyntaxException e) {
                throw new ServletException ("Malformed URI for path: " + path, e);
            }
            baseURI = uri.toString();
        }
        
        // Arrange for initialization of EXPath repository by setting the appropriate system
        // property, if a path is configured using JNDI:

        String expathRepo = filterConfig.getInitParameter("org.expath.pkg.saxon.repo");
        if (expathRepo != null) {
            System.setProperty ("org.expath.pkg.saxon.repo", expathRepo);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        if( request instanceof HttpServletRequest) {
            HttpServletRequest req = (HttpServletRequest)request;
            String path = req.getServletPath();
            int pathInfoOffset = path.indexOf('/', 1);
            if (pathInfoOffset > 0) {
                String servletPath = path.substring(0, pathInfoOffset);
                String pathInfo = path.substring(pathInfoOffset);
                
                Request wrapper = new Request(req);
                wrapper.setServletPath (servletPath + "/lux");
                
                @SuppressWarnings("unchecked")
                HashMap<String,String[]> params = new HashMap<String, String[]>(req.getParameterMap());
                // load from classpath
                // TODO: some way of configuring to load from db
                params.put ("lux.xquery", new String[] { baseURI + pathInfo });
                wrapper.setParameterMap(params);
                chain.doFilter(wrapper, response);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
    
    public class Request extends HttpServletRequestWrapper {
        
        private Map<String,String[]> parameterMap;
        
        private String servletPath; 
        
        public Request (HttpServletRequest req) {
            super (req);
        }
        
        @Override
        public String getServletPath () {
            return servletPath;
        }
        
        @Override
        public String getPathInfo () {
            return null;
        }

        @Override 
        public Map<String,String[]> getParameterMap () {
            return parameterMap;
        }
        
        public void setParameterMap (Map<String,String[]> map) {
            parameterMap = map;
        }
        
        public void setServletPath (String path) {
            servletPath = path;
        }
        
    }

}
