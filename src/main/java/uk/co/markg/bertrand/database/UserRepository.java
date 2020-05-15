package uk.co.markg.bertrand.database;

import static uk.co.markg.bertrand.db.tables.Messages.MESSAGES;
import static uk.co.markg.bertrand.db.tables.Users.USERS;
import java.util.List;
import org.jooq.DSLContext;
import disparse.parser.reflection.Injectable;
import uk.co.markg.bertrand.db.tables.pojos.Users;

public class UserRepository {

  private DSLContext dsl;

  @Injectable
  public static UserRepository getRepository() {
    return new UserRepository();
  }

  private UserRepository() {
    dsl = JooqConnection.getJooqContext();
  }

  public List<Users> getAll() {
    return dsl.selectFrom(USERS).fetchInto(Users.class);
  }

  public int save(long userid) {
    return dsl.insertInto(USERS).values(userid).execute();
  }

  public boolean isUserOptedIn(long userid) {
    return dsl.selectFrom(USERS).where(USERS.USERID.eq(userid)).fetchOne(0, int.class) != 0;
  }

  public int delete(long userid) {
    dsl.deleteFrom(MESSAGES).where(MESSAGES.USERID.eq(userid)).execute();
    return dsl.deleteFrom(USERS).where(USERS.USERID.eq(userid)).execute();
  }
}
