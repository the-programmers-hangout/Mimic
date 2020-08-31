package uk.co.markg.mimic.database;

import static org.jooq.impl.DSL.count;
import static uk.co.markg.mimic.db.tables.Messages.MESSAGES;
import static uk.co.markg.mimic.db.tables.Users.USERS;
import java.util.List;
import org.jooq.DSLContext;
import disparse.parser.reflection.Injectable;
import uk.co.markg.mimic.db.tables.pojos.Users;

public class UserRepository {

  private DSLContext dsl;

  /**
   * {@link disparse.parser.reflection.Injectable Injectable} method used by disparse upon command
   * invocation.
   * 
   * @return a new user repository instance
   */
  @Injectable
  public static UserRepository getRepository() {
    return new UserRepository();
  }

  private UserRepository() {
    dsl = JooqConnection.getJooqContext();
  }

  public int getCount() {
    return dsl.selectCount().from(USERS).fetchOne(0, int.class);
  }
  
  /**
   * Returns a list of all users in the database
   * 
   * @return the list of users
   */
  public List<Users> getAll() {
    return dsl.selectFrom(USERS).fetchInto(Users.class);
  }

  /**
   * Retrieves the list of opted in users that have messages saved into the messages table. This
   * means they are a candidate for markov generation
   * 
   * @return the list of users
   */
  public List<Users> getAllMarkovCandidates() {
    return dsl.select(MESSAGES.USERID).from(MESSAGES).groupBy(MESSAGES.USERID).having(count().ge(5))
        .fetchInto(Users.class);
  }

  /**
   * Retrieves the list of opted in users that have messages saved into the messages table. This
   * means they are a candidate for markov generation
   * 
   * @return the list of users
   */
  public List<Long> getAllMarkovCandidateIds() {
    return dsl.select(MESSAGES.USERID).from(MESSAGES).groupBy(MESSAGES.USERID).having(count().ge(5))
        .fetchInto(Long.class);
  }

  /**
   * Save a user to the database
   * 
   * @param userid the userid to save
   * @return the number of rows inserted
   */
  public int save(long userid) {
    return dsl.insertInto(USERS).values(userid).execute();
  }

  /**
   * Returns whether a user exists in the database
   * 
   * @param userid the userid to find
   * @return true if the user exists
   */
  public boolean isUserOptedIn(long userid) {
    return dsl.selectFrom(USERS).where(USERS.USERID.eq(userid)).fetchOne(0, int.class) != 0;
  }
  
  public boolean isMarkovCandidate(long userid) {
    return getAllMarkovCandidateIds().contains(userid);
  }

  /**
   * Delete the user entry and any message content associated with the user
   * 
   * @param userid the user id to remove
   * @return the number of rows deleted from the user table
   */
  public int delete(long userid) {
    return dsl.deleteFrom(USERS).where(USERS.USERID.eq(userid)).execute();
  }
}