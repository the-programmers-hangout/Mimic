package uk.co.markg.bertrand.database;

import static uk.co.markg.bertrand.db.tables.Channels.CHANNELS;
import static uk.co.markg.bertrand.db.tables.Messages.MESSAGES;
import java.util.List;
import org.jooq.DSLContext;
import disparse.parser.reflection.Injectable;
import uk.co.markg.bertrand.db.tables.pojos.Channels;

public class ChannelRepository {

  private DSLContext dsl;

  @Injectable
  public static ChannelRepository getRepository() {
    return new ChannelRepository();
  }

  private ChannelRepository() {
    dsl = JooqConnection.getJooqContext();
  }

  public int save(String channelid) {
    return save(new Channels(Long.parseLong(channelid)));
  }

  public int save(Channels channel) {
    return dsl.executeInsert(dsl.newRecord(CHANNELS, channel));
  }

  public List<Channels> getAll() {
    return dsl.selectFrom(CHANNELS).fetchInto(Channels.class);
  }

  public int delete(String channelid) {
    return delete(Long.parseLong(channelid));
  }

  public int delete(long channelid) {
    dsl.deleteFrom(MESSAGES).where(MESSAGES.CHANNELID.eq(channelid)).execute();
    return dsl.deleteFrom(CHANNELS).where(CHANNELS.CHANNELID.eq(channelid)).execute();
  }
}
