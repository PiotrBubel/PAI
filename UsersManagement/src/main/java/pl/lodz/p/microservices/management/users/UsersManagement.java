package pl.lodz.p.microservices.management.users;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import org.apache.commons.lang3.EnumUtils;

public class UsersManagement extends AbstractVerticle {

    private static final String USERS_MANAGEMENT_SERVICE_ADDRESS = "pl.lodz.p.microservices.management.users.UsersManagement";

    private static final String METHOD_KEY = "method";

    private static final Logger log = LoggerFactory.getLogger(UsersManagement.class);

    private static EventBus eventBus;

    private JsonObject user1 = new JsonObject()
            .put("login", "user1")
            .put("name", "Jan")
            .put("surname", "Kowalski")
            .put("id", 1);

    private JsonObject user2 = new JsonObject()
            .put("login", "user2")
            .put("name", "Joanna")
            .put("surname", "Nowak")
            .put("id", 2);

    private JsonObject user3 = new JsonObject()
            .put("login", "user3")
            .put("name", "Janusz")
            .put("surname", "Kwiatkowski")
            .put("id", 3);

    private JsonObject user4 = new JsonObject()
            .put("login", "user4")
            .put("name", "Kinga")
            .put("surname", "Kikowska")
            .put("id", 4);

    private JsonArray usersList = new JsonArray()
            .add(user1)
            .add(user2)
            .add(user3)
            .add(user4);

    @Override
    public void start() {
        eventBus = vertx.eventBus();
        eventBus.consumer(USERS_MANAGEMENT_SERVICE_ADDRESS, this::messageHandler);
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
            case GET_USERS_LIST:
                getUsersList(inMessage);
                break;
            case GET_USER_DETAILS:
                getUserDetails(inMessage);
                break;
        }
    }

    private void getUsersList(Message<JsonObject> inMessage) {

        JsonObject replyList = new JsonObject().put("list", usersList);
        inMessage.reply(replyList);

    }

    private void getUserDetails(Message<JsonObject> inMessage) {
        if (inMessage.body() == null) {
            log.error("Received GET_USER_DETAILS command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        } else if (!inMessage.body().containsKey("login")) {
            log.error("Received GET_USER_DETAILS command without user login");
            inMessage.fail(400, "Bad request. Field 'login' is required.");
            return;
        }

        if (inMessage.body().getString("login").equals("user1")) {
            inMessage.reply(user1);
        } else if (inMessage.body().getString("login").equals("user2")) {
            inMessage.reply(user2);
        } else if (inMessage.body().getString("login").equals("user3")) {
            inMessage.reply(user3);
        } else if (inMessage.body().getString("login").equals("user4")) {
            inMessage.reply(user4);
        } else {
            inMessage.fail(404, "User with given login not found.");
        }

    }
}