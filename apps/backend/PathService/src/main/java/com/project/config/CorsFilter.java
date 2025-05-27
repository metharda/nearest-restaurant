
package com.project.config;

import jakarta.servlet.*;
import java.io.IOException;

// This filter has been deprecated in favor of using Spring's built-in CORS support via CorsConfig
@Deprecated
public class CorsFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(req, res);
    }
}
