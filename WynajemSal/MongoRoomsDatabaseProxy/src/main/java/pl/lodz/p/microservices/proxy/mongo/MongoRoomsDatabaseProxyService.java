package pl.lodz.p.microservices.proxy.mongo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.EnumUtils;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;

public class MongoRoomsDatabaseProxyService extends AbstractVerticle {

    private static final String DATABASE_ROOMS_PROXY_ROOM_ADDRESS = "pl.lodz.p.microservices.proxy.mongo.DatabaseRoomsProxyRoom";
    private static final String METHOD_KEY = "method";

    private static MongoClient mongoClient;
    private static JsonObject config;

    private static final Logger log = LoggerFactory.getLogger(MongoRoomsDatabaseProxyService.class);

    @Override
    public void start(Future<Void> fut) {
        config = Vertx.currentContext().config();
        mongoClient = MongoClient.createNonShared(vertx, getMongoDBConfig());
        EventBus eventBus = vertx.eventBus();

        eventBus.consumer(DATABASE_ROOMS_PROXY_ROOM_ADDRESS, this::messageHandler);

        mongoClient.runCommand("ping", new JsonObject().put("ping", 1), mongoPingResponse -> {
            if (mongoPingResponse.succeeded()) {
                log.info("Started Mongo Rooms Database Proxy Room with Mongo client");
                fut.complete();
            } else {
                log.error("Cannot connect to database, cause: " + mongoPingResponse.cause().getMessage());
                fut.fail(mongoPingResponse.cause().getMessage());
            }
        });
    }

    private void messageHandler(Message<JsonObject> inMessage) {
        String calledMethod = inMessage.headers().get(METHOD_KEY);

        if (!EnumUtils.isValidEnum(Methods.class, calledMethod)) {
            log.warn("Method +" + calledMethod + " not found");
            inMessage.fail(405, "Method not allowed");
            return;
        }

        Methods method = Methods.valueOf(calledMethod);
        log.info("Received message. Method " + method + " will be called.");
        switch (method) {
            // Rooms
            case GET_ROOMS_LIST_FROM_DATABASE:
                RoomDBManager.getRoomsList(inMessage, mongoClient);
                break;
            case GET_ROOM_DETAILS_FROM_DATABASE:
                RoomDBManager.getRoomDetails(inMessage, mongoClient);
                break;
            case SAVE_NEW_ROOM_IN_DATABASE:
                RoomDBManager.saveNewRoom(inMessage, mongoClient);
                break;
            case DELETE_ROOM_FROM_DATABASE:
                RoomDBManager.deleteRoom(inMessage, mongoClient);
                break;
            case EDIT_ROOM_IN_DATABASE:
                RoomDBManager.editRoom(inMessage, mongoClient);
                break;
        }
    }

    /**
     * Method loads MongoDB configuration from curent verticle configuration
     *
     * @return JsonObiect with MongoDB configuration
     */
    private JsonObject getMongoDBConfig() {
        Integer port = config.getInteger("mongo_port");
        if (port == null) {
            port = 27017;
        }

        String host = config.getString("mongo_host");
        if (StringUtils.isBlank(host)) {
            host = "mongodatabase";
        }

        String db = config.getString("mongo_db");
        if (StringUtils.isBlank(db)) {
            db = "BookingsRoomDB";
        }

        String username = config.getString("mongo_username");
        if (StringUtils.isBlank(username)) {
            username = "RoomsDatabaseProxyUser";
        }

        String password = config.getString("mongo_password");
        if (StringUtils.isBlank(password)) {
            password = "BookingsRoomp@Ssw0rd";
        }

        return new JsonObject()
                .put("db_name", db)
                .put("port", port)
                .put("host", host)
                .put("username", username)
                .put("password", password)
                .put("authMechanism", "SCRAM-SHA-1");
    }
}