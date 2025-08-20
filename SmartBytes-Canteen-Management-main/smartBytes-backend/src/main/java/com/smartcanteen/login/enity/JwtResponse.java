// E:\Salman(29-06-2025)\smart-canteen-backend\src\main\java\com\smartcanteen\login\enity\JwtResponse.java

package com.smartcanteen.login.enity; // <<< KEEPING THIS PACKAGE AS PER YOUR STRUCTURE

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor

public class JwtResponse { // Renamed from AuthResponse to JwtResponse to match your AuthController
    private String token;
    private Long id;
    private String username;
    private String email;
    private String role; // Single role as a string (e.g., "ROLE_STUDENT")

    public JwtResponse(String token, Long id, String username, String email, String role) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    
}