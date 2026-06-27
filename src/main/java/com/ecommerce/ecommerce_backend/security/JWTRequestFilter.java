package com.ecommerce.ecommerce_backend.security;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.ecommerce.ecommerce_backend.dao.LocalUserDao;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

@Component
public class JWTRequestFilter extends OncePerRequestFilter {

    private JWTService jwtService;

    private LocalUserDao userDao;

    public JWTRequestFilter(JWTService jwtService, LocalUserDao userDao) {
        this.jwtService = jwtService;
        this.userDao = userDao;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String tokenHeader = request.getHeader("Authorization");
        if(tokenHeader != null && tokenHeader.startsWith("Bearer ")){
            String token = tokenHeader.substring(7);
            try
            {
                String username = jwtService.getUsername(token);
                Optional<LocalUser> opUser = userDao.findByUsernameIgnoreCase(username);
                if(opUser.isPresent()) {
                    LocalUser user = opUser.get();
                    if (user.isEmailVerified()) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>()); // build authentication object of the user
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); // tell spring this is our authentication | because of it spring security and spring mvc knows about this authentication
                        SecurityContextHolder.getContext().setAuthentication(authentication); // SecurityContextHolder where the authentication get stored
                    }
                }
            } catch (JWTDecodeException ex){}
        }

        filterChain.doFilter(request, response);
    }
}
