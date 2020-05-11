package uk.co.markg.bertrand;

import org.flywaydb.core.Flyway;
import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.Configuration;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.Generate;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Jdbc;
import org.jooq.meta.jaxb.Target;

/**
 * Hello world!
 *
 */
public class App {
  public static void main(String[] args) throws Exception {
    initDatabase();
    if (args.length == 1 && "--generate".equals(args[0])) {
      executeJooqGeneration();
    }
  }

  private static void initDatabase() {
    Flyway.configure()
        .dataSource(System.getenv("B_HOST"), System.getenv("B_USER"), System.getenv("B_PASS"))
        .load().migrate();
  }

  private static void executeJooqGeneration() throws Exception {
    Configuration configuration =
        new Configuration()
            .withJdbc(new Jdbc().withDriver(System.getenv("B_DRIVER"))
                .withUrl(System.getenv("B_HOST")).withUser(
                    System.getenv("B_USER"))
                .withPassword(System.getenv("B_PASS")))
            .withGenerator(new Generator()
                .withDatabase(
                    new Database().withExcludes("flyway_schema_history|information_schema.*|pg_.*")
                        .withInputSchema("public").withOutputSchemaToDefault(Boolean.TRUE))
                .withGenerate(new Generate().withPojos(Boolean.TRUE)
                    .withDeprecationOnUnknownTypes(Boolean.FALSE))
                .withTarget(new Target().withPackageName("uk.co.markg.bertrand.db")
                    .withDirectory("src/main/java")));

    GenerationTool.generate(configuration);
  }
}
