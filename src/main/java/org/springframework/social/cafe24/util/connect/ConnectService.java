package org.springframework.social.cafe24.util.connect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.cafe24.api.Cafe24;
import org.springframework.social.cafe24.connect.Cafe24OAuth2Connection;
import org.springframework.social.connect.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * @author aimpugn
 * @since 2018-07-20
 */
@Service
public class ConnectService {
    private static final Logger logger = LoggerFactory.getLogger(ConnectService.class);
    private static final String PROVIDER_ID = "cafe24";

    private final HashMap<String, ConnectionRepository> connectionRepositoryHashMap = new HashMap<>();
    private final UsersConnectionRepository usersConnectionRepository;


    @Autowired
    public ConnectService(UsersConnectionRepository usersConnectionRepository) {
        this.usersConnectionRepository = usersConnectionRepository;
    }

    /**
     * mallId에 해당하는 ConnectionRepository를 생성한다. <br>
     * <ul>
     *     <li>이미 mallId에 해당하는 ConnectionRepository가 connectionRepositoryHashMap에 있다면 해당 ConnectionRepository를 반환</li>
     *     <li>mallId에 해당하는 ConnectionRepository가 없다면 ConnectionRepository를 새로 생성</li>
     * </ul>
     * @param mallId
     * @return
     */
    public ConnectionRepository makeConnectionRepository(String mallId) {
        ConnectionRepository connectionRepository = null;
        logger.debug("makeConnectionRepository mallId: " + mallId);
        if (usersConnectionRepository != null && mallId != null) {

            if (connectionRepositoryHashMap.containsKey(mallId)) return connectionRepositoryHashMap.get(mallId);

            connectionRepository = usersConnectionRepository.createConnectionRepository(mallId);
            connectionRepositoryHashMap.put(mallId, connectionRepository);
        }
        return connectionRepository;
    }


    /**
     * mallId에 해당하는 ConnectionRepository가 connectionRepositoryHashMap에 있는지 확인
     *
     * @return not null: true <br> null: false
     */
    public boolean existConnectionRepository(String mallId) {

        return connectionRepositoryHashMap.containsKey(mallId);
    }

    public UsersConnectionRepository getUsersConnectionRepository() {
        return usersConnectionRepository;
    }

    /**
     * 전달되는 mallId에 해당하는 api를 반환한다
     * <ol>
     *     <li>connectionRepositoryHashMap에 mallId에 해당하는 ConnectionRepository가 있는지 확인하고 없으면 새로 생성한다</li>
     *     <li>DB의 UserConnection 테이블에서 mallId와 cafe24에 해당하는 연결 정보를 찾아온다</li>
     *     <li>DB로부터 가져온 연결이 만료됐다면 갱신을 하고 업데이트한다</li>
     * </ol>
     * @param mallId api를 찾기 위한 mallId
     * @return Cafe24 타입의 api를 반환한다.
     * @throws NoSuchConnectionException
     */
    public Cafe24 getApi(String mallId) {
        Cafe24 api = null;
        ConnectionRepository connectionRepositoryForApi;
        if (!existConnectionRepository(mallId)) {
            connectionRepositoryForApi = usersConnectionRepository.createConnectionRepository(mallId);
            connectionRepositoryHashMap.put(mallId, connectionRepositoryForApi);
        } else {
            connectionRepositoryForApi = connectionRepositoryHashMap.get(mallId);
        }


        List<Connection<?>> connections = connectionRepositoryForApi.findConnections(PROVIDER_ID);

        logger.debug("login connections.size: " + connections.size());

        if (connections.size() > 0) {
            /* 언제나 한 개의 연결만 유지할 것이므로 size가 0보다 큰 경우 하나의 연결만 가져온다. */
            Connection connection = connections.get(0);
            logger.debug("login connection.getKey().getProviderUserId(): " + connection.getKey().getProviderUserId());

            /* 가져온 연결의 getProviderUserId와 mallId가 같은지 확인 */
            if (connection.getKey().getProviderUserId().equals(mallId)) {

                /* 해당하는 연결이 있다면 Cafe24OAuth2Connection으로 바꾼다 */
                Cafe24OAuth2Connection cafe24OAuth2Connection = (Cafe24OAuth2Connection) connection;

                /* 해당 연결이 만료됐다면 갱신한다.*/
                if (cafe24OAuth2Connection.hasExpired()) {
                    logger.debug("cafe24OAuth2Connection가 만료됐다면 refresh");
                    cafe24OAuth2Connection.refresh();
                    logger.debug("cafe24OAuth2Connection refresh 완료");
                    ConnectionData refreshedConnectionData = cafe24OAuth2Connection.createData();
                    logger.debug("refreshedConnectionData getProviderUserId: " + refreshedConnectionData.getProviderUserId());
                    logger.debug("refreshedConnectionData getAccessToken: " + refreshedConnectionData.getAccessToken());
                    logger.debug("refreshedConnectionData getRefreshToken: " + refreshedConnectionData.getRefreshToken());
                    logger.debug("refreshedConnectionData getExpireTime: " + refreshedConnectionData.getExpireTime());

                    /* 갱신되면 해당 Cafe24OAuth2Connection에 새로운 정보가 저장되어 있고,
                    addConnection으로 새로운 데이터가 저장되므로 다시 DB를 거쳐서 가져올 필요 없다 */
                    /* 기존 연결인 경우 업데이트 해야 하므로 addConnection이 아닌 updateConnection 사용 */
                    connectionRepositoryForApi.updateConnection(cafe24OAuth2Connection);
                    logger.debug("refresh된 connectionRepository.updateConnection(cafe24OAuth2Connection) 완료");
                }
                /* addConnection을 했으면 api가 갱신됐을 것이므로 api를 가져와 반환한다. */
                api = cafe24OAuth2Connection.getApi();
            }

        } else {
            /* mallId에 해당하는 연결이 없는 경우 */
            ConnectionKey connectionKey = new ConnectionKey(PROVIDER_ID, mallId);
            throw new NoSuchConnectionException(connectionKey);
        }


        return api;
    }

    public HashMap<String, ConnectionRepository> getConnectionRepositoryHashMap() {
        return connectionRepositoryHashMap;
    }



}
