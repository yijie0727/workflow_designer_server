package cz.zcu.kiv.server.sqlite.Model;

import org.json.JSONObject;

public class User {
    private String email;
    private String password;
    private String username;
    private String token;
    private Long id;
    private Boolean active;
    private Boolean reset;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getReset() {
        return reset;
    }

    public void setReset(Boolean reset) {
        this.reset = reset;
    }

    public JSONObject toJSON() {
        JSONObject userObject =  new JSONObject();
        userObject.put("email", email);
        //userObject.put("password",password);
        userObject.put("id",id);
        userObject.put("token",token);
        userObject.put("reset",reset);
        //userObject.put("active",active);
        return userObject;
    }
}
