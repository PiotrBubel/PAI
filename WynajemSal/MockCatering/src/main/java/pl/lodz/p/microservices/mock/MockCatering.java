package pl.lodz.p.microservices.mock;

import com.google.common.net.MediaType;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MockCatering extends AbstractVerticle {

    private static final String STATUS_ENDPOINT = "/api/status";
    private static final String ORDERS_ENDPOINT = "/api/orders";
    private static final String MENU_ENDPOINT = "/api/menu";

    private static final Logger log = LoggerFactory.getLogger(MockCatering.class);

    private static EventBus eventBus;
    private static Router router;

    @Override
    public void start() {
        log.info("MockCatering start method");

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

        Random generator = new Random();
        String statusIdResponse = "{\"status\": \"W realizacji\"}";
        String createOrderResponse = "{\"id\": " + (generator.nextInt(1000) + 1) + "}";
        String menuResponse = "[\n" +
                "   {\n" +
                "      \"Id\":1,\n" +
                "      \"Name\":\"Spaghetti\",\n" +
                "      \"Price\":20,\n" +
                "      \"Weight\":\"300g\"\n" +
                "   },\n" +
                "   {\n" +
                "      \"Id\":2,\n" +
                "      \"Name\":\"Kurczak Tajski XL\",\n" +
                "      \"Price\":25,\n" +
                "      \"Weight\":\"400g\"\n" +
                "   },\n" +
                "   {\n" +
                "      \"Id\":3,\n" +
                "      \"Name\":\"Pizza\",\n" +
                "      \"Price\":18,\n" +
                "      \"Weight\":\"500g\"\n" +
                "   },\n" +
                "   {\n" +
                "      \"Id\":4,\n" +
                "      \"Name\":\"Kurczak Tajski XS\",\n" +
                "      \"Price\":18,\n" +
                "      \"Weight\":\"250g\"\n" +
                "   },\n" +
                "   {\n" +
                "      \"Id\":5,\n" +
                "      \"Name\":\"Tortilla\",\n" +
                "      \"Price\":15,\n" +
                "      \"Weight\":\"300g\"\n" +
                "   },\n" +
                "   {\n" +
                "      \"Id\":6,\n" +
                "      \"Name\":\"Burger\",\n" +
                "      \"Price\":15,\n" +
                "      \"Weight\":\"300g\"\n" +
                "   },\n" +
                "   {\n" +
                "      \"Id\":7,\n" +
                "      \"Name\":\"Mała przystawka\",\n" +
                "      \"Price\":15,\n" +
                "      \"Weight\":\"300g\"\n" +
                "   },\n" +
                "   {\n" +
                "      \"Id\":8,\n" +
                "      \"Name\":\"Duża przystawka\",\n" +
                "      \"Price\":25,\n" +
                "      \"Weight\":\"500g\"\n" +
                "   },\n" +
                "   {\n" +
                "      \"Id\":9,\n" +
                "      \"Name\":\"Talerz owoców\",\n" +
                "      \"Price\":10,\n" +
                "      \"Weight\":\"259g\"\n" +
                "   },\n" +
                "   {\n" +
                "      \"Id\":10,\n" +
                "      \"Name\":\"Jabłecznik\",\n" +
                "      \"Price\":10,\n" +
                "      \"Weight\":\"200g\"\n" +
                "   },\n" +
                "   {\n" +
                "      \"Id\":11,\n" +
                "      \"Name\":\"Kurczak Tikka Masala\",\n" +
                "      \"Price\":25,\n" +
                "      \"Weight\":\"400g\"\n" +
                "   },\n" +
                "   {\n" +
                "      \"Id\":12,\n" +
                "      \"Name\":\"Kurczak Korma\",\n" +
                "      \"Price\":25,\n" +
                "      \"Weight\":\"400g\"\n" +
                "   },\n" +
                "   {\n" +
                "      \"Id\":13,\n" +
                "      \"Name\":\"Sałatka z kurczakiem\",\n" +
                "      \"Price\":15,\n" +
                "      \"Weight\":\"350g\"\n" +
                "   },\n" +
                "   {\n" +
                "      \"Id\":14,\n" +
                "      \"Name\":\"Stek\",\n" +
                "      \"Price\":30,\n" +
                "      \"Weight\":\"220g\"\n" +
                "   },\n" +
                "   {\n" +
                "      \"Id\":15,\n" +
                "      \"Name\":\"Zupa pomidorowa\",\n" +
                "      \"Price\":5,\n" +
                "      \"Weight\":\"250g\"\n" +
                "   },\n" +
                "   {\n" +
                "      \"Id\":16,\n" +
                "      \"Name\":\"Rosół\",\n" +
                "      \"Price\":5,\n" +
                "      \"Weight\":\"250g\"\n" +
                "   }\n" +
                "]";
        String statusResponse = "[{\"Id\":1,\"Name\":\"nowe zamówienie\"},{\"Id\":2,\"Name\":\"w realizacji\"},{\"Id\":3,\"Name\":\"zatwierdzone\"},{\"Id\":4,\"Name\":\"zrealizowane\"},{\"Id\":5,\"Name\":\"odrzucone\"}]";


        router.get(STATUS_ENDPOINT).handler(context ->
                context.response()
                        .putHeader("Content-Type", MediaType.JSON_UTF_8.toString())
                        .end(statusResponse));

        router.get(ORDERS_ENDPOINT + "/:id").handler(context ->
                context.response()
                        .putHeader("Content-Type", MediaType.JSON_UTF_8.toString())
                        .end(statusIdResponse));

        router.get(MENU_ENDPOINT).handler(context ->
                context.response()
                        .putHeader("Content-Type", MediaType.JSON_UTF_8.toString())
                        .end(menuResponse));

        router.post(ORDERS_ENDPOINT).handler(context ->
                context.response()
                .putHeader("Content-Type", MediaType.JSON_UTF_8.toString())
                .end(createOrderResponse));

        vertx.createHttpServer().requestHandler(router::accept).listen(8081);
    }
}
