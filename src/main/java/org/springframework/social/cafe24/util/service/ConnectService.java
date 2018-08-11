package org.springframework.social.cafe24.util.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.cafe24.api.Cafe24;
import org.springframework.social.cafe24.connect.Cafe24OAuth2Connection;
import org.springframework.social.cafe24.util.entity.UserConnection;
import org.springframework.social.cafe24.util.repository.UserConnectionRepo;
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

    // 결국 singleton이므로 static으로 만들지 않더라도 하나로 유지된다
    private final HashMap<String, ConnectionRepository> connectionRepositoryHashMap;
    private final HashMap<String, Cafe24OAuth2Connection> connectionHashMap;
    private final UsersConnectionRepository usersConnectionRepository;


    @Autowired
    public ConnectService(UsersConnectionRepository usersConnectionRepository, UserConnectionRepo userConnectionRepo) {
        this.usersConnectionRepository = usersConnectionRepository;
        /* providerId와 userId를 초기에 가져와서 저장해 둔다 */

        connectionRepositoryHashMap = new HashMap<>();
        connectionHashMap = new HashMap<>();

        /* UserConnection 테이블의 데이터를 모두 가져온다 */
        logger.debug("모든 연결 가져와서 connectionRepositoryHashMap과 connectionHashMap에 저장해두기");
        List<UserConnection> userConnections = userConnectionRepo.findAll();

        /* UserConnection에 저장된 정보가 적어도 하나 이상일 때 미리 생성 */
        /* 저장만 해두고 refresh는 1. api 호출할 때, 2. RefreshConnectService.doRefresh 실행될 때 수행 */
        if (userConnections.size() > 0) {
            for (UserConnection userConnection : userConnections) {
                String mallId = userConnection.getUserId();
                logger.debug("ConnectService constructor mallId: " + mallId);

                /* connectionRepositoryHashMap에 userConnection의 uderId, 즉 mallId가 없드면 해당하는 ConnectionRepository를 생성해서 저장한다 */
                ConnectionRepository connectionRepository = makeConnectionRepository(mallId);
                connectionRepositoryHashMap.put(mallId, connectionRepository);
                /* 새로 만들거나 기존에 있던 ConnectionRepository로 mallId에 해당하는 Connection을 받아온다 */
                /* 이때 JdbcConnectionRepository에서 DB로 쿼리를 날리고, 기존 UserConnection 데이터로 Connection을 만들어서 반환 */
                List<Connection<?>> connections = connectionRepository.findConnections(PROVIDER_ID);

                logger.debug("ConnectService connections.size: " + connections.size());

                if (connections.size() > 0) {
                    /* mallId 하나당 한 개의 연결만 유지할 것이므로 size가 0보다 큰 경우 하나의 연결만 가져온다. */
                    Cafe24OAuth2Connection connection = (Cafe24OAuth2Connection) connections.get(0);
                    logger.debug("ConnectService connection.getKey().getProviderUserId(): " + connection.getKey().getProviderUserId());
                    connectionHashMap.put(mallId, connection);
                }

            }
        }



    }

    /**
     * mallId에 해당하는 ConnectionRepository를 생성한다. <br>
     * ConnectionRepository의 쓰임새는 필드에 userId, 즉 mallId를 가지고 있는가이므로 동일 객체인지 여부는 문제가 되지 않는다.
     * <ol>
     * <li>이미 mallId에 해당하는 ConnectionRepository가 connectionRepositoryHashMap에 있다면 해당 ConnectionRepository를 반환</li>
     * <li>mallId에 해당하는 ConnectionRepository가 없다면 ConnectionRepository를 새로 생성</li>
     * </ol>
     * @param mallId
     * @return
     */
    public ConnectionRepository makeConnectionRepository(String mallId) {
        ConnectionRepository connectionRepository = null;
        logger.debug("makeConnectionRepository mallId: " + mallId);
        if (usersConnectionRepository != null && mallId != null) {
            /* mallId에 해당하는 ConnectionRepository가 있으면 있는 것을 반환하고 없으면 새로 만들어서 반환한다 */
            if (connectionRepositoryHashMap.containsKey(mallId)) {
                connectionRepository = connectionRepositoryHashMap.get(mallId);
            } else {
                connectionRepository = usersConnectionRepository.createConnectionRepository(mallId);
                connectionRepositoryHashMap.put(mallId, connectionRepository);
            }
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

    public HashMap<String, Cafe24OAuth2Connection> getConnectionHashMap() {
        return connectionHashMap;
    }

    /**
     * 전달되는 mallId에 해당하는 api를 반환한다
     * <ol>
     * <li> Connection이 connectionHashMap에 있는 경우
     *     <ol>
     *         <li>connectionHashMap에 저장된 Connection이 있다면 그 Connection으로 api를 반환한다</li>
     *         <li>해당 Connection이 만료됐으면 갱신해서 반환한다</li>
     *     </ol>
     * </li>
     *
     * <li> Connection이 connectionHashMap에 없는 경우
     *     <ol>
     *         <li>connectionRepositoryHashMap에 mallId에 해당하는 ConnectionRepository가 있는지 확인하고 없으면 새로 생성한다</li>
     *         <li>DB의 UserConnection 테이블에서 mallId에 해당하는 연결 정보가 있다면 찾아온다</li>
     *         <li>DB로부터 가져온 연결이 만료됐다면 갱신을 하고 업데이트하고 반환한다</li>
     *     </ol>
     * </li>
     * <li>mallId에 해당하는 연결이 connectionHashMap에 없고 DB에도 없는 경우 연결이 없다면 {@link NoSuchConnectionException}을 발생시킨다</li>
     * @param mallId api를 찾기 위한 mallId
     * @return Cafe24 타입의 api를 반환한다.
     * @throws NoSuchConnectionException connectionHashMap에 Connection 없고 DB의 UserConnection에 연결 정보 없는 경우 익세션 발생
     */
    public Cafe24 getApi(String mallId) {
        Cafe24 api = null;
        Cafe24OAuth2Connection cafe24OAuth2Connection;
        ConnectionRepository connectionRepositoryForApi;
        /* 만약 기존에 연결된 Connection이 있다면 그 Connection을 사용하여 api 반환 */
        if (connectionHashMap.containsKey(mallId)) {
            logger.info("mallId에 해당하는 Connection이 있는 경우 ");
            /* 그 연결이 만료됐다면 refresh */
            cafe24OAuth2Connection = connectionHashMap.get(mallId);
            if (cafe24OAuth2Connection.hasExpired()) {
                cafe24OAuth2Connection.refresh();
                /* refresh된 정보는 cafe24OAuth2Connection에 업데이트 되어 있으므로, updateConnection 실행 */
                connectionRepositoryHashMap.get(mallId).updateConnection(cafe24OAuth2Connection);
            }

            /* 최종적으로 Connection으로부터 api 반환 */
            api = cafe24OAuth2Connection.getApi();

        } else {
            logger.info("mallId에 해당하는 Connection이 없는 경우 ");

            /* 우선 connectionHashMap에 연결이 없다면 DB를 확인해야 하므로,
             * connectionRepositoryHashMap에도 ConnectionRepository가 없는지 확인. 없다면 새로 만든다. */
            if (!existConnectionRepository(mallId)) {
                connectionRepositoryForApi = usersConnectionRepository.createConnectionRepository(mallId);
                connectionRepositoryHashMap.put(mallId, connectionRepositoryForApi);
            }
            connectionRepositoryForApi = connectionRepositoryHashMap.get(mallId);
            List<Connection<?>> connections = connectionRepositoryForApi.findConnections(PROVIDER_ID);

            /* ConnectService 초기에 connectionHashMap과 connectionRepositoryHashMap에 모든 Connection과 ConnectionRepository를 저장했음에도,
             * connectionHashMap과 connectionRepositoryHashMap에 mallId에 해당하는 Connection과 ConnectionRepository가 없고 DB에 UserConnection 정보가 있다면,
             * 여기서 새로 만들어 주고 저장한다. */
            if (connections.size() > 0) {
                /* 해당하는 연결이 있다면 Cafe24OAuth2Connection으로 바꾼다 */
                Cafe24OAuth2Connection connection = (Cafe24OAuth2Connection) connections.get(0);
                logger.debug("login connection.getKey().getProviderUserId(): " + connection.getKey().getProviderUserId());

                /* 해당 연결이 만료됐다면 갱신한다.*/
                if (connection.hasExpired()) {
                    logger.debug("cafe24OAuth2Connection가 만료됐다면 refresh");
                    connection.refresh();

                    logger.debug("cafe24OAuth2Connection refresh 완료 후 만료 시간 확인");
                    ConnectionData refreshedConnectionData = connection.createData();
                    logger.debug("refreshedConnectionData getExpireTime: " + refreshedConnectionData.getExpireTime());

                    /* 갱신되면 해당 Cafe24OAuth2Connection에 initAccessToken 메서드 등을 통해 새로운 정보가 저장되고,
                        addConnection으로 새로운 데이터가 저장되므로 다시 DB를 거쳐서 가져올 필요 없다 */
                    /* 기존 연결인 경우 업데이트 해야 하므로 addConnection이 아닌 updateConnection 사용 */
                    connectionRepositoryForApi.updateConnection(connection);
                }

                /* 그리고 이 연결을 connectionHashMap에 저장한다 */
                connectionHashMap.put(mallId, connection);

                /* addConnection을 했으면 api가 갱신됐을 것이므로 api를 가져와 반환한다. */
                api = connection.getApi();

            } else {
                /* mallId에 해당하는 연결이 connectionHashMap에 없고 DB에도 없는 경우, NoSuchConnectionException 발생시킨다 */
                ConnectionKey connectionKey = new ConnectionKey(PROVIDER_ID, mallId);
                throw new NoSuchConnectionException(connectionKey);
            }

        }

        return api;
    }


    public HashMap<String, ConnectionRepository> getConnectionRepositoryHashMap() {
        return connectionRepositoryHashMap;
    }


    /**
     * oauth2Callback 핸들러에서 connectSupport.completeConnection이 수행된 후 전달된 connection을 저장하는 메서드. <br>
     * Connection을 connectionHashMap에 저장해 두고,
     *
     * @param mallId     connectionHashMap 또는 connectionRepositoryHashMap에 저장할 key 값.
     * @param connection 저장할 새로운 Connection
     */
    public void addConnection(String mallId, Connection<?> connection) {
        /* ConnectionRepository 준비 */
        ConnectionRepository connectionRepository;
        if (!existConnectionRepository(mallId)) {
            connectionRepository = usersConnectionRepository.createConnectionRepository(mallId);
            connectionRepositoryHashMap.put(mallId, connectionRepository);
        }
        connectionRepository = connectionRepositoryHashMap.get(mallId);


        /* 실제 DB에 저장하는 단계 */
        if (!existConnection(connectionRepository)) {
            connectionRepository.addConnection(connection);
        } else {
            connectionRepository.updateConnection(connection);
        }
        /* Connection을 저장해두는 단계 */
        connectionHashMap.put(mallId, (Cafe24OAuth2Connection) connection);
    }


    /**
     * userId가 중복되는지 여부 확인
     *
     * @param connectionRepository userId, 즉 mallId를 필드로 가지는 ConnectionRepository
     * @return true: 중복되는 연결 정보 있음 <br> false: 중복되는 연결 정보 없음
     */
    private boolean existConnection(ConnectionRepository connectionRepository) {
        List<Connection<?>> connections = connectionRepository.findConnections(PROVIDER_ID);
        return connections.size() > 0;
    }
}
