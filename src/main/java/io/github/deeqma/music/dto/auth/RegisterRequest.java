package io.github.deeqma.music.dto.auth;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank
    @Size(min = 2, max = 40, message = "Username must be between 2 and 40 characters")
    private String username;

    @NotBlank
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    public RegisterRequest() {
    }

    public RegisterRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

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
