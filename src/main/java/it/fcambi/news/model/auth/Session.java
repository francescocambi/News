package it.fcambi.news.model.auth;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Francesco on 08/10/15.
 */
@Entity
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private User user;

    @Temporal(TemporalType.TIMESTAMP)
    private Date loginTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date logoutTime;

    @PrePersist
    protected void onCreate() {
        loginTime = new Date();
    }

    public Session() {}

    public Session(User u) {
        user = u;
    }

    public void logout() {
        if (logoutTime == null)
            logoutTime = new Date();
    }

    public User getUser() {
        return user;
    }

    public long getId() {
        return id;
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public Date getLogoutTime() {
        return logoutTime;
    }

    public boolean isOpen() {
        return logoutTime == null;
    }

    public boolean isValid() {
        // Valid if open and login is done max 2hours ago
        return isOpen()
                && (new Date().getTime() - loginTime.getTime()) < 7200001;
    }
}
