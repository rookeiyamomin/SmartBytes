package com.smartcanteen.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final AuthEntryPointJwt unauthorizedHandler;
    private final JwtTokenUtil jwtTokenUtil;

    public SecurityConfig(
            CustomUserDetailsService userDetailsService,
            AuthEntryPointJwt unauthorizedHandler,
            JwtTokenUtil jwtTokenUtil
    ) {
        this.userDetailsService = userDetailsService;
        this.unauthorizedHandler = unauthorizedHandler;
        this.jwtTokenUtil = jwtTokenUtil;
    }
    
    
    //This filter checks every incoming request for a JWT token in the Authorization header.
    //If valid, it authenticates the user and puts their details into the Spring Security context.
    @Bean
    public JwtAuthenticationFilter authenticationJwtTokenFilter() {
        return new JwtAuthenticationFilter(jwtTokenUtil, userDetailsService);
    }

    //Uses BCrypt hashing to encrypt passwords.
    //During registration: password is hashed.
    //During login: hashed input is matched with stored hash.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    DaoAuthenticationProvider is a Spring Security class that helps authenticate a user from a database (DAO = Data Access Object).
//    In simple terms, this provider tells Spring:
//    “When a user logs in, fetch their record from the database, check the password, and verify roles.”
//	  @Bean : Marks this method as a Spring-managed bean. This object is created and managed by Spring and is available to inject wherever needed.
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
//    	Creates a new instance of DaoAuthenticationProvider, which is Spring Security’s built-in implementation of AuthenticationProvider.
//    	It does two main things:
//    	Loads user by username/email.
//    	Compares raw input password with encoded password from DB.
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        //Use my custom logic to fetch user data from the database. So when someone logs in, this service fetches the user from your DB.
        authProvider.setUserDetailsService(userDetailsService);
		//This sets the password encoder (usually BCryptPasswordEncoder) so Spring can compare the password:
		//Takes the password entered by the user
		//Hashes it
		//Compares it with the hashed password stored in DB
		//If it matches → Authentication successful.
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

//    AuthenticationManager is a core Spring Security interface responsible for:
//    Taking a username & password and checking if they're valid.
//    It delegates the process to configured AuthenticationProviders like DaoAuthenticationProvider.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // <<< CRITICAL: This is the correct allowed origin for your frontend >>>
        config.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setMaxAge(3600L);
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .securityContext(securityContext -> securityContext.requireExplicitSave(false))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers("/api/test/**").permitAll()
                        .requestMatchers("/api/food/all", "/api/food/available").permitAll()

                        .requestMatchers("/api/orders/place").hasRole("STUDENT")
                        .requestMatchers("/api/orders/my").hasRole("STUDENT")
                        .requestMatchers("/api/orders/all").hasAnyRole("CANTEEN_MANAGER", "ADMIN")
                        .requestMatchers("/api/orders/{id}").authenticated()
                        .requestMatchers("/api/orders/{id}/status").hasRole("CANTEEN_MANAGER")

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class)
                .cors(Customizer.withDefaults()); // Enable CORS using the bean defined above

        return http.build();
    }
}