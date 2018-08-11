package org.springframework.social.cafe24.util.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.social.cafe24.connect.Cafe24OAuth2Connection;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * 1시간마다 AccessToken 갱신.
 * @author aimpugn
 * @since 2018-08-02
 */
@Component
public class RefreshConnectService {
    private static final Logger logger = LoggerFactory.getLogger(RefreshConnectService.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private ConnectService connectService;

    // fixedRate: 이전 실행부터 다음 실행 전 N 밀리세컨드를 기다린다. 만약 현재 실행이 fixedRate를 초과하면, 다음 실행 하나만 대기(queue) 상태가 된다.
    // fixedDelay: 이전 실행부터 다음 실행 전 N 밀리세컨드를 기다린다. 현재 실행이 얼만큼의 시간이 걸리든, (현재 실행의 끝 시간 + fixedDelay) 후에 다음 실행이 시작된다. 대기
    @Scheduled(fixedDelay = 3600000)
    public void doRefresh() {
        String now = dateFormat.format(new Date());
        logger.info("doRefresh start when: {}", now);
        if (connectService != null) {
            Map<String, ConnectionRepository> usersConnectionRepositoryMap = connectService.getConnectionRepositoryHashMap();
            Map<String, Cafe24OAuth2Connection> cafe24OAuth2ConnectionMap = connectService.getConnectionHashMap();

            Set<String> keys = cafe24OAuth2ConnectionMap.keySet();
            for (String key : keys) {
                logger.debug("doRefresh mallId: {}", key);
                Cafe24OAuth2Connection cafe24OAuth2Connection = cafe24OAuth2ConnectionMap.get(key);

                logger.debug("doRefresh {}", cafe24OAuth2Connection);
                cafe24OAuth2Connection.refresh();
                usersConnectionRepositoryMap.get(key).updateConnection(cafe24OAuth2Connection);
            }
        }

    }
}
