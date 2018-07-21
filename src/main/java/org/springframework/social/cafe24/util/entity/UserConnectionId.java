package org.springframework.social.cafe24.util.entity;


import java.io.Serializable;

public class UserConnectionId implements Serializable {

    private String providerId;

    private String userId;

    public UserConnectionId() {
    }

    public UserConnectionId(String providerId, String userId) {
        this.providerId = providerId;
        this.userId = userId;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "UserConnectionId{" +
                "providerId='" + providerId + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserConnectionId)) return false;

        UserConnectionId that = (UserConnectionId) o;

        if (!getProviderId().equals(that.getProviderId())) return false;
        return getUserId().equals(that.getUserId());
    }

    @Override
    public int hashCode() {
        int result = getProviderId().hashCode();
        result = 31 * result + getUserId().hashCode();
        return result;
    }
}
