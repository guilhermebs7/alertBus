package alertbus.user_service.security;

import alertbus.user_service.domain.entity.User;
import alertbus.user_service.repository.UserRepository;
import alertbus.user_service.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter  extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;
    @Autowired
    private UserRepository userRepository;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var token= recoverToken(request);

        if(token != null){
            var email = tokenService.validateToken(token);
            if (!email.isEmpty()){
                User user = userRepository.findByEmail(email)
                        .orElseThrow(()-> new UsernameNotFoundException("Usuário não encontrado"));


                var authentication = new UsernamePasswordAuthenticationToken(user, null, java.util.Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request,response);

    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader= request.getHeader("Authorization");
        if(authHeader == null || !authHeader.startsWith("Bearer "))return null;
        return authHeader.replace("Bearer ","");
    }
}
