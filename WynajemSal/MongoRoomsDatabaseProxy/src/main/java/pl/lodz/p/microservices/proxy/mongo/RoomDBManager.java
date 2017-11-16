package pl.lodz.p.microservices.proxy.mongo;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;

/**
 * Created by pbubel on 01.01.17.
 */
class RoomDBManager {
    private static final String COLLECTION_ROOMS = "Rooms";
    private static final Logger log = LoggerFactory.getLogger(MongoRoomsDatabaseProxyService.class);

    static void getRoomsList(Message<JsonObject> inMessage, MongoClient mongoClient) {
        FindOptions options = new FindOptions().setFields(new JsonObject().put("_id", 0).put("price", 0).put("description", 0));

        mongoClient.findWithOptions(COLLECTION_ROOMS, new JsonObject(), options, response -> {
            if (response.succeeded()) {
                JsonObject result = new JsonObject().put("list", response.result());
                inMessage.reply(result);
                log.info("Load rooms from database succeeded.");
            } else {
                log.error("Load rooms from database failed, cause: " + response.cause().getMessage());
                inMessage.fail(500, "Database error: " + response.cause().getMessage());
            }
        });
    }

    static void getRoomDetails(Message<JsonObject> inMessage, MongoClient mongoClient) {
        String name = inMessage.body().getString("name");
        FindOptions options = new FindOptions().setFields(new JsonObject().put("_id", 0));
        JsonObject jsonQuery = new JsonObject().put("name", name);

        mongoClient.findWithOptions(COLLECTION_ROOMS, jsonQuery, options, response -> {
            if (response.succeeded()) {
                if (response.result().size() > 0) {
                    JsonObject result = response.result().get(0);
                    inMessage.reply(result);
                    log.info("Load room details from database succeeded.");
                } else {
                    log.info("Load room details from database not succeeded. No room with name: " + name);
                    inMessage.fail(404, "No room with name: " + name);
                }
            } else {
                log.error("Load room details from database failed, cause: " + response.cause().getMessage());
                inMessage.fail(500, "Database error: " + response.cause().getMessage());
            }
        });
    }

    static void editRoom(Message<JsonObject> inMessage, MongoClient mongoClient) {
        String name = inMessage.body().getString("name");
        JsonObject jsonQuery = new JsonObject().put("name", name);
        JsonObject roomData = inMessage.body().getJsonObject("room");
        JsonObject update = new JsonObject().put("$set", roomData);

        mongoClient.update(COLLECTION_ROOMS, jsonQuery, update, response -> {
            if (response.succeeded()) {
                log.info("Edit room data in database succeeded.");
                inMessage.reply(Utils.jsonHttpResponse(200, "Edited"));
            } else {
                log.info("Edit room data in database failed, cause: " + response.cause().getMessage());
                inMessage.fail(500, "Database error: " + response.cause().getMessage());
            }
        });
    }

    static void saveNewRoom(Message<JsonObject> inMessage, MongoClient mongoClient) {
        JsonObject roomJsonObject = inMessage.body();
        mongoClient.insert(COLLECTION_ROOMS, roomJsonObject, response -> {
            if (response.succeeded()) {
                log.info("Save new room to database succeeded.");
                inMessage.reply(Utils.jsonHttpResponse(201, "Created"));
            } else {
                log.error("Save new room to database failed, cause: " + response.cause().getMessage());
                inMessage.fail(500, "Database error: " + response.cause().getMessage());
            }
        });
    }

    static void deleteRoom(Message<JsonObject> inMessage, MongoClient mongoClient) {
        JsonObject jsonQuery = new JsonObject().put("name", inMessage.body().getValue("name"));
        mongoClient.removeOne(COLLECTION_ROOMS, jsonQuery, response -> {
            if (response.succeeded()) {
                inMessage.reply(Utils.jsonHttpResponse(204, "No content"));
                log.info("Load room details from database succeeded.");
            } else {
                log.error("Load room details from database failed, cause: " + response.cause().getMessage());
                inMessage.fail(500, "Database error: " + response.cause().getMessage());
            }
        });
    }
}