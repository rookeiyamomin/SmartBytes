// E:\Salman(29-06-2025)\smart-canteen-backend\src\main\java\com\smartcanteen\login\enity\LoginRequest.java

package com.smartcanteen.login.enity; // <<< KEEPING THIS PACKAGE AS PER YOUR STRUCTURE

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    
}