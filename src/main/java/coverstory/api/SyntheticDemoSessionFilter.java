package coverstory.api;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;

/** Unsigned demo-only identity selector. Never enable outside an isolated local synthetic environment. */
@Component
@Profile("synthetic-demo")
@ConditionalOnProperty(name="coverstory.synthetic-demo.identity.enabled", havingValue="true")
public final class SyntheticDemoSessionFilter extends OncePerRequestFilter {
    public static final UUID OWNER_A=UUID.fromString("a1680000-0000-4000-8000-000000000001");
    public static final UUID OWNER_B=UUID.fromString("b1680000-0000-4000-8000-000000000002");
    private static final Map<String,UUID> ACCOUNTS=Map.of("A",OWNER_A,"B",OWNER_B);

    @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)
            throws ServletException,IOException {
        // A demo selector can never downgrade, replace, or impersonate an already trusted identity.
        if (request.getUserPrincipal() != null) { chain.doFilter(request,response); return; }
        String account=null;
        Cookie[] cookies=request.getCookies();
        if(cookies!=null) for(Cookie cookie:cookies) if("coverstory_demo_owner".equals(cookie.getName())) account=cookie.getValue();
        UUID owner=account==null ? null : ACCOUNTS.get(account);
        if(owner==null) { chain.doFilter(request,response); return; }
        Principal principal=()->owner.toString();
        chain.doFilter(new HttpServletRequestWrapper(request) {
            @Override public Principal getUserPrincipal(){return principal;}
        },response);
    }
}
