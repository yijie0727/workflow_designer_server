package cz.zcu.kiv.server.sqlite.Model;

import java.util.Date;

public class Module {

    private String jarName;
    private String packageName;
    private Boolean publicJar;
    private String author;
    private Long id;
    private Date lastUpdate;

    public String getJarName() {
        return jarName;
    }

    public void setJarName(String jarName) {
        this.jarName = jarName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Boolean isPublicJar() {
        return publicJar;
    }

    public void setPublicJar(Boolean publicJar) {
        this.publicJar = publicJar;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
