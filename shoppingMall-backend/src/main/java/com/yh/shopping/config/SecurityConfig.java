package com.yh.shopping.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.web.filter.CorsFilter;

import com.yh.shopping.config.oauth.PrincipalOauth2UserService;
import com.yh.shopping.jwt.JwtAuthenticationFilter;
import com.yh.shopping.jwt.JwtAuthorizationFilter;
import com.yh.shopping.repository.user.UserRepository;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter{
	

	@Autowired
	private PrincipalOauth2UserService principalOauth2UserService;
	
	@Autowired
	private CorsFilter corsFilter;
	
	@Autowired
	private UserRepository userRepository;
	
	private JwtProperties jwtProperties;
	
	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	 
	 @Bean
	  public HttpFirewall defaultHttpFirewall() {
	    return new DefaultHttpFirewall();
	  }
	 
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션을 사용하지 않겠다.
			.and()
			.addFilter(corsFilter)
			.formLogin().disable()
			.httpBasic().disable()
			.addFilter(new JwtAuthenticationFilter(authenticationManager(), jwtProperties)) // AuthenticationManager
			.addFilter(new JwtAuthorizationFilter(authenticationManager(), userRepository)) // AuthenticationManager
		 	.authorizeRequests()
			.antMatchers("/user/**").authenticated() // 인증만 되면 들어갈 수 있는 주소!!
			.antMatchers("/manager/**").access("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
			.antMatchers("/admin/**").access("hasRole('ROLE_ADMIN')")
			.anyRequest().permitAll() // 위 주소를 제외한 나머지는 로그인 없이 접근 가능
			.and()
			.oauth2Login()
			.authorizationEndpoint()
			.baseUri("/login/oauth2/*")
			.and()
				/*
				 * .redirectionEndpoint() .baseUri("/login/oauth2/*") .and()
				 */
			.userInfoEndpoint()
			.userService(principalOauth2UserService);
	}
	
}