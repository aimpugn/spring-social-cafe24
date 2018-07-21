package org.springframework.social.cafe24.util.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.social.cafe24.util.entity.UserConnection;
import org.springframework.social.cafe24.util.entity.UserConnectionId;

import java.util.List;

public interface UserConnectionRepo extends JpaRepository<UserConnection, UserConnectionId> {
    List<UserConnection> findByProviderUserId(String userId);

    UserConnection findByUserId(String userId);

    UserConnection findUserConnectionByUserId(String userId);
}

