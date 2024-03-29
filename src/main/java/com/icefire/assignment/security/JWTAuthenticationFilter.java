package com.icefire.assignment.security;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icefire.assignment.entity.ApplicationUser;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter{

	private final ObjectMapper mapper;
	private AuthenticationManager authenticationManager;

	public JWTAuthenticationFilter(final ObjectMapper mapper, AuthenticationManager authenticationManager) {
		this.mapper = mapper;
		this.authenticationManager = authenticationManager; 
	}
	
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		try {
			ApplicationUser appUser = new ObjectMapper().readValue(request.getInputStream(), ApplicationUser.class);
			return this.authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(appUser.getUsername(), appUser.getPassword()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		Long expirationTime = System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME;
		String username = ((User)authResult.getPrincipal()).getUsername();
		String token = Jwts.builder().setSubject(username)
				.setExpiration(new Date(expirationTime))
				.signWith(SignatureAlgorithm.HS512, SecurityConstants.SECRET)
				.compact();
		
		Map<String, String> tokenMap = new HashMap<String, String>();
        tokenMap.put("token", token);
        tokenMap.put("expiration", expirationTime.toString());
        tokenMap.put("username", username);
        
        mapper.writeValue(response.getWriter(), tokenMap);
        
		response.addHeader(SecurityConstants.HEADER_PREFIX, SecurityConstants.TOKEN_PREFIX+token);
	}
	
	
}
