package com.smartcanteen.security;

import com.smartcanteen.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartcanteen.login.enity.ERole;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;

//This CustomUserDetailsService class is a critical part of your Spring Security authentication system. 
//It tells Spring Security how to load user data from your database during the login process. 

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    // Note: We are fetching the role from the database because after the authentication when user tries to access the protected APIs, 
    // then spring will check whether user has authority to access this api or not. 
    // Without roles in UserDetailsImpl, Spring can’t apply these restrictions.

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        // Get the String name of the ERole enum (e.g., "ROLE_NGO", "ROLE_STUDENT")
        String roleNameFromDb = user.getRole().getName().name(); // This now directly gives "ROLE_NGO"

        // Convert the String name to the ERole enum constant (e.g., ERole.ROLE_NGO)
        ERole eRole = ERole.fromName(roleNameFromDb); // fromName handles both prefixed and non-prefixed input

        // Build UserDetailsImpl with the correct format for GrantedAuthority
        // Since ERole.name() now returns "ROLE_NGO", we pass it directly.
        return UserDetailsImpl.build(user, Collections.singletonList(new SimpleGrantedAuthority(eRole.name())));
    }
    
    /*
    Example Scenario
    Imagine if you didn’t fetch the role and didn’t set any authority:

    Ex: return UserDetailsImpl.build(user, Collections.emptyList());
    Spring would authenticate the user successfully (if password matched).

    But then any endpoint that required a role like hasRole('STUDENT') would be forbidden (403), because Spring sees the user has no authorities.
    */
    
    // Flow of this execution:
//    Frontend sends login request (username/password)
//    → Spring Security calls loadUserByUsername()
//    → Fetch User + Role from DB
//    → Convert to UserDetailsImpl (contains username, password, roles)
//    → Spring compares password using encoder
//    → On success: Authenticated & stored in SecurityContext

    
}