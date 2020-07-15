package eu.beezig.core.net.profile;

import java.util.Date;

public class OwnProfile {
    private int id;
    private UserRole role;
    private Date firstLogin;
    private Region region;

    public OwnProfile(int id, UserRole role, long firstLogin, String regionId, String regionName) {
        this.id = id;
        this.role = role;
        this.firstLogin = new Date(firstLogin);
        if(regionId != null) {
            this.region = new Region(regionId, regionName);
        }
    }

    public int getId() {
        return id;
    }

    public UserRole getRole() {
        return role;
    }

    public Date getFirstLogin() {
        return firstLogin;
    }

    public Region getRegion() {
        return region;
    }
}
