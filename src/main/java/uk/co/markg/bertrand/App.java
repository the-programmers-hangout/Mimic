package uk.co.markg.bertrand;

import org.flywaydb.core.Flyway;

/**
 * Hello world!
 *
 */
public class App {
  public static void main(String[] args) {
    initDatabase();
  }

  private static void initDatabase() {
    Flyway.configure()
        .dataSource(System.getenv("B_HOST"), System.getenv("B_USER"), System.getenv("B_PASS"))
        .load().migrate();
  }
}
