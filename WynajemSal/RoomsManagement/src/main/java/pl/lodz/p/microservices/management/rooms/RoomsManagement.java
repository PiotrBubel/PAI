package pl.lodz.p.microservices.management.rooms;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

public class RoomsManagement extends AbstractVerticle {

    private static final String ROOMS_MANAGEMENT_ROOM_ADDRESS = "pl.lodz.p.microservices.management.rooms.RoomsManagement";
    private static final String DATABASE_ROOMS_PROXY_ROOM_ADDRESS = "pl.lodz.p.microservices.proxy.mongo.DatabaseRoomsProxyRoom";

    private static final String METHOD_KEY = "method";
    private static final int TIMEOUT = 4000;

    private static final Logger log = LoggerFactory.getLogger(RoomsManagement.class);

    private static EventBus eventBus;

    @Override
    public void start() {
        eventBus = vertx.eventBus();
        eventBus.consumer(ROOMS_MANAGEMENT_ROOM_ADDRESS, this::messageHandler);
    }

    private void messageHandler(Message<JsonObject> inMessage) {
        String requestedMethod = inMessage.headers().get(METHOD_KEY);

        if (!EnumUtils.isValidEnum(Methods.class, requestedMethod)) {
            log.error("Method" + requestedMethod + " not found");
            inMessage.fail(500, "Method" + requestedMethod + " not found");
            return;
        }

        log.info("Received message. Method " + requestedMethod + " will be called.");
        switch (Methods.valueOf(requestedMethod)) {
            case DELETE_ROOM:
                deleteRoom(inMessage);
                break;
            case SAVE_NEW_ROOM:
                saveNewRoom(inMessage);
                break;
            case GET_ROOMS_LIST:
                getRoomsList(inMessage);
                break;
            case GET_ROOM_DETAILS:
                getRoomDetails(inMessage);
                break;
            case EDIT_ROOM:
                editRoom(inMessage);
                break;
        }
    }

    private void getRoomsList(Message<JsonObject> inMessage) {
        eventBus.send(DATABASE_ROOMS_PROXY_ROOM_ADDRESS, inMessage.body(),
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "GET_ROOMS_LIST_FROM_DATABASE"),
                (AsyncResult<Message<JsonObject>> response) -> {
                    if (response.succeeded()) {
                        JsonObject replyList = new JsonObject().put("list", jsonObjectToArray(response.result().body()));
                        inMessage.reply(replyList);
                    } else {
                        ReplyException cause = (ReplyException) response.cause();
                        inMessage.fail(cause.failureCode(), cause.getMessage());
                    }
                });
    }

    private void getRoomDetails(Message<JsonObject> inMessage) {
        if (inMessage.body() == null) {
            log.error("Received GET_ROOM_DETAILS command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        } else if (!inMessage.body().containsKey("name")) {
            log.error("Received GET_ROOM_DETAILS command without room name");
            inMessage.fail(400, "Bad request. Field 'name' is required.");
            return;
        }

        eventBus.send(DATABASE_ROOMS_PROXY_ROOM_ADDRESS, inMessage.body(),
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "GET_ROOM_DETAILS_FROM_DATABASE"),
                (AsyncResult<Message<JsonObject>> response) -> {
                    if (response.succeeded()) {
                        inMessage.reply(response.result().body());
                    } else {
                        ReplyException cause = (ReplyException) response.cause();
                        inMessage.fail(cause.failureCode(), cause.getMessage());
                    }
                });
    }

    private void saveNewRoom(Message<JsonObject> inMessage) {
        if (inMessage.body() == null) {
            log.error("Received SAVE_NEW_ROOM command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        } else if (!inMessage.body().containsKey("room")) {
            inMessage.fail(400, "Bad Request. Field 'room' is required.");
            return;
        }
        JsonObject newRoom = inMessage.body().getJsonObject("room");
        if (!newRoom.containsKey("name") || !newRoom.containsKey("price") ||
                StringUtils.isBlank(newRoom.getString("name"))) {
            log.error("Received SAVE_NEW_ROOM command without room name or price");
            inMessage.fail(400, "Bad Request. Fields 'name' and 'price' are required.");
            return;
        }

        Utils.addCreatedDate(newRoom);
        eventBus.send(DATABASE_ROOMS_PROXY_ROOM_ADDRESS, newRoom,
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "SAVE_NEW_ROOM_IN_DATABASE"),
                (AsyncResult<Message<JsonObject>> response) -> {
                    if (response.succeeded()) {
                        inMessage.reply(response.result().body());
                    } else {
                        ReplyException cause = (ReplyException) response.cause();
                        inMessage.fail(cause.failureCode(), cause.getMessage());
                    }
                });
    }

    private void editRoom(Message<JsonObject> inMessage) {
        if (inMessage.body() == null) {
            log.error("Received EDIT_ROOM command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        } else if (!inMessage.body().containsKey("room")) {
            log.error("Received EDIT_ROOM command without room data");
            inMessage.fail(400, "Bad Request. Room data is required.");
            return;
        } else if (!inMessage.body().containsKey("name")) {
            log.error("Received EDIT_ROOM command without room name");
            inMessage.fail(400, "Bad Request. Room name is required.");
            return;
        }

        eventBus.send(DATABASE_ROOMS_PROXY_ROOM_ADDRESS, inMessage.body(),
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "EDIT_ROOM_IN_DATABASE"),
                (AsyncResult<Message<JsonObject>> response) -> {
                    if (response.succeeded()) {
                        inMessage.reply(response.result().body());
                    } else {
                        ReplyException cause = (ReplyException) response.cause();
                        inMessage.fail(cause.failureCode(), cause.getMessage());
                    }
                });
    }

    private void deleteRoom(Message<JsonObject> inMessage) {
        if (inMessage.body() == null) {
            log.error("Received DELETE_ROOM command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        } else if (!inMessage.body().containsKey("name")) {
            log.error("Received DELETE_ROOM command without room name");
            inMessage.fail(400, "Bad Request. Room name is required.");
            return;
        }

        eventBus.send(DATABASE_ROOMS_PROXY_ROOM_ADDRESS, inMessage.body(),
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "DELETE_ROOM_FROM_DATABASE"),
                (AsyncResult<Message<JsonObject>> response) -> {
                    if (response.succeeded()) {
                        inMessage.reply(response.result().body());
                    } else {
                        ReplyException cause = (ReplyException) response.cause();
                        inMessage.fail(cause.failureCode(), cause.getMessage());
                    }
                });
    }

    // TODO move it to separate Helpers class
    private JsonArray jsonObjectToArray(JsonObject object) {
        JsonArray array = new JsonArray();
        JsonArray given = object.getJsonArray("list");
        for (int i = 0; i < given.size(); i++) {
            array.add(given.getJsonObject(i).getValue("name"));
        }
        return array;
    }
}