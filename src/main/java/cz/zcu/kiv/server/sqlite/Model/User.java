package cz.zcu.kiv.server.sqlite.Model;

import org.json.JSONObject;

public class User {
    private String email;
    private String password;
    private String username;
    private Long id;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public JSONObject toJSON() {
        JSONObject userObject =  new JSONObject();
        userObject.put("email", email);
        //userObject.put("password",password);
        userObject.put("id",id);
        return userObject;
    }
}
