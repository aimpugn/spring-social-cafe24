package org.springframework.social.cafe24.util.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table
@IdClass(UserConnectionId.class)
public class UserConnection {
    // create table UserConnection (
    // userId varchar(255) not null,
    // providerId varchar(255) not null,
    // providerUserId varchar(255),
    // rank int not null,
    // displayName varchar(255),
    // profileUrl varchar(512),
    // imageUrl varchar(512),
    // accessToken varchar(1024) not null,
    // secret varchar(255),
    // refreshToken varchar(255),
    // expireTime bigint,
    // primary key (providerId, providerUserId));
    //create unique index UserConnectionRank on UserConnection(userId, providerId, rank);
    @Id
    @Column(name = "userId", nullable = false)
    private String userId;

    @Id
    @Column(name = "providerId", nullable = false)
    private String providerId;


    @Column(name = "providerUserId", nullable = false)
    private String providerUserId;

    @Column(name = "rank", nullable = false)
    private Long rank;

    @Column(name = "displayName")
    private String displayName;

    @Column(name = "profileUrl", length = 512)
    private String profileUrl;

    @Column(name = "imageUrl", length = 512)
    private String imageUrl;

    @Column(name = "accessToken", nullable = false, length = 512)
    private String accessToken;

    @Column(name = "secret", length = 512)
    private String secret;

    @Column(name = "refreshToken", length = 512)
    private String refreshToken;

    @Column(name = "expireTime")
    private Long expireTime;

    public UserConnection() {
    }

    public UserConnection(String userId, String providerId, String providerUserId, Long rank, String displayName, String profileUrl, String imageUrl, String accessToken, String secret, String refreshToken, Long expireTime) {
        this.userId = userId;
        this.providerId = providerId;
        this.providerUserId = providerUserId;
        this.rank = rank;
        this.displayName = displayName;
        this.profileUrl = profileUrl;
        this.imageUrl = imageUrl;
        this.accessToken = accessToken;
        this.secret = secret;
        this.refreshToken = refreshToken;
        this.expireTime = expireTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public void setProviderUserId(String providerUserId) {
        this.providerUserId = providerUserId;
    }

    public Long getRank() {
        return rank;
    }

    public void setRank(Long rank) {
        this.rank = rank;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

    @Override
    public String toString() {
        return "UserConnection{" +
                "userId='" + userId + '\'' +
                ", providerId='" + providerId + '\'' +
                ", providerUserId='" + providerUserId + '\'' +
                ", rank=" + rank +
                ", displayName='" + displayName + '\'' +
                ", profileUrl='" + profileUrl + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", secret='" + secret + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", expireTime=" + expireTime +
                '}';
    }
}
