package pl.lodz.p.microservices.api.rest;

import com.google.common.net.MediaType;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import pl.lodz.p.microservices.api.rest.method.UsersManagementMethods;

import java.util.HashSet;
import java.util.Set;

public class RestApi extends AbstractVerticle {
    private static final String USERS_MANAGEMENT_SERVICE_ADDRESS = "pl.lodz.p.microservices.management.users.UsersManagement";

    private static final String METHOD_KEY = "method";

    private static final String USERS_ENDPOINT = "/api/users";

    private static final Logger log = LoggerFactory.getLogger(RestApi.class);

    private static EventBus eventBus;
    private static Router router;

    @Override
    public void start() {
        log.info("RestApi start method");

        eventBus = vertx.eventBus();
        router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // CORS configuration
        Set<HttpMethod> allowedMethods = new HashSet<>();
        allowedMethods.add(HttpMethod.GET);
        allowedMethods.add(HttpMethod.DELETE);
        allowedMethods.add(HttpMethod.PUT);
        allowedMethods.add(HttpMethod.POST);

        Set<String> allowedHeaders = new HashSet<>();
        allowedHeaders.add("Content-Type");
        allowedHeaders.add("Auth-Token");
        router.route().handler(CorsHandler.create("*").allowedMethods(allowedMethods).allowedHeaders(allowedHeaders));

        // Users endpoint
        router.get(USERS_ENDPOINT).handler(context -> requestHandler(context,
                UsersManagementMethods.GET_USERS_LIST,
                USERS_MANAGEMENT_SERVICE_ADDRESS,
                true));

        router.get(USERS_ENDPOINT + "/:login").handler(context -> requestHandler(context,
                UsersManagementMethods.GET_USER_DETAILS,
                USERS_MANAGEMENT_SERVICE_ADDRESS,
                "login",
                true));

        vertx.createHttpServer().requestHandler(router::accept).listen(8094);
    }

    private void requestHandler(RoutingContext context, Enum method, String address, String parameter, boolean permissionCheck) {
        String parameterValue = context.request().getParam(parameter);
        requestHandler(context, method, address, new JsonObject().put(parameter, parameterValue), permissionCheck);
    }

    private void requestHandler(RoutingContext context, Enum method, String address, boolean permissionCheck) {
        requestHandler(context, method, address, new JsonObject(), permissionCheck);
    }

    private void requestHandler(RoutingContext context, Enum method, String address, JsonObject body, boolean permissionCheck) {
        String logInfo = "Passing request to start method: " + method.name() + " to microservice: " + address;
        if (permissionCheck) {
            logInfo += " with permission check first.";
        }
        log.info(logInfo);
        if (body != null) {
            log.info("Incoming message: " + body.encodePrettily());
        } else {
            body = new JsonObject();
            log.info("Without message.");
        }

        final DeliveryOptions options = new DeliveryOptions()
                .setSendTimeout(3500)
                .addHeader(METHOD_KEY, method.name());

        passMessageToService(context, options, address, body);

    }

    private void passMessageToService(RoutingContext routingContext, DeliveryOptions options, String address, JsonObject jsonMessage) {
        eventBus.send(address, jsonMessage, options,
                response -> {
                    if (!response.succeeded()) {
                        ReplyException cause = (ReplyException) response.cause();
                        respond(routingContext, cause.failureCode(), cause.getMessage());
                        return;
                    }
                    JsonObject resultBody;
                    if (response.result().body().getClass().equals(JsonObject.class)) {
                        resultBody = (JsonObject) response.result().body();
                    } else {
                        respond(routingContext, 500, "Internal server error. Server does not respond with application/json");
                        return;
                    }
                    String logInfo = "Responding with ";
                    if (resultBody.containsKey("code")) {
                        routingContext.response().setStatusCode(resultBody.getInteger("code"));
                        logInfo += resultBody.getInteger("code") + " ";
                    }
                    if (resultBody.containsKey("message")) {
                        routingContext.response().setStatusMessage(resultBody.getString("message"));
                        logInfo += resultBody.getString("message");
                    }
                    logInfo += " " + resultBody.encodePrettily();
                    log.info(logInfo);
                    routingContext.response()
                            .putHeader("Content-Type", MediaType.JSON_UTF_8.toString())
                            .end(resultBody.encodePrettily());
                });
    }

    private void respond(RoutingContext routingContext, int code, String message) {
        if (code < 1) {
            code = 500;
        }
        log.info("Responding with: " + code + " " + message);
        routingContext.response()
                .putHeader("Content-Type", MediaType.JSON_UTF_8.toString())
                .setStatusCode(code)
                .end(new JsonObject()
                        .put("code", code)
                        .put("message", message)
                        .encodePrettily());
    }
}
