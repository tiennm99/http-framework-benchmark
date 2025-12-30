package ph.benchmark.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;

public class Server extends AbstractVerticle {

    public static void main(String[] args) {
        //Launcher.executeCommand("run", Server.class.getName(), "-instances 4");
        Vertx vertx = Vertx.vertx();

        DeploymentOptions options = new DeploymentOptions().setInstances(4);
        vertx.deployVerticle(Server.class.getName(), options);
    }

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);

        router.route().handler(routingContext -> {
            HttpServerRequest req = routingContext.request();

            double result = Double.parseDouble(req.getParam("init"));
            int loop = Integer.parseInt(req.getParam("loop_count"));
            double add = Double.parseDouble(req.getParam("add"));
            double mul = Double.parseDouble(req.getParam("mul"));
            double sub = Double.parseDouble(req.getParam("sub"));
            double div = Double.parseDouble(req.getParam("div"));

            int line = Integer.parseInt(req.getParam("line"));

            for (int i = 0; i < loop; i++) {
                result += add;
            }

            for (int i = 0; i < loop; i++) {
                result *= mul;
            }

            for (int i = 0; i < loop; i++) {
                result -= sub;
            }

            for (int i = 0; i < loop; i++) {
                result /= div;
            }

            String result_str = Double.toString(result).substring(0, 10);

            //build resp content
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < line; i++) {
                sb.append("result=").append(result_str).append('\n');
            }

            routingContext.response().putHeader("content-type", "text/plain").end(sb.toString());
        });

        vertx.createHttpServer().requestHandler(router).listen(8080);
    }
}
