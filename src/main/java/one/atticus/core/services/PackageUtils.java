package one.atticus.core.services;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

class PackageUtils {
    static int authenticate(SecurityContext context) {
        if(context == null || context.getAuthenticationScheme() == null || context.getUserPrincipal() == null) {
            throw new NotAuthorizedException("authentication required");
        }
        if(!context.isSecure()) {
            // throw new ForbiddenException("secure protocol (https) required");
        }

        final Principal principal = context.getUserPrincipal();

        return Integer.valueOf(principal.getName());
    }

}