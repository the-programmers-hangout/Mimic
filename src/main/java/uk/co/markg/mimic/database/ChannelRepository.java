package uk.co.markg.mimic.database;

import static uk.co.markg.mimic.db.tables.Channels.CHANNELS;
import java.util.List;
import org.jooq.DSLContext;
import disparse.parser.reflection.Injectable;
import uk.co.markg.mimic.db.tables.pojos.Channels;

public class ChannelRepository {

  private DSLContext dsl;

  /**
   * {@link disparse.parser.reflection.Injectable Injectable} method used by disparse upon command
   * invocation.
   * 
   * @return a new channel respository instance
   */
  @Injectable
  public static ChannelRepository getRepository() {
    return new ChannelRepository();
  }

  private ChannelRepository() {
    dsl = JooqConnection.getJooqContext();
  }

  /**
   * Constructs a {@link uk.co.markg.mimic.db.tables.pojos.Channels Channels} Object to save to the
   * database.
   * 
   * @param channelid the discord channel id
   * @param read      whether the bot has read access to the channel
   * @param write     whether the bot has write access to the channel
   * @return the number of inserted rows
   */
  public int save(String channelid, Boolean read, Boolean write, Long serverid) {
    return save(new Channels(Long.parseLong(channelid), read, write, serverid));
  }

  /**
   * Saves a channel and it's permissions to the database
   * 
   * @param channel the channel object to save
   * @return the number of inserted rows
   */
  public int save(Channels channel) {
    return dsl.executeInsert(dsl.newRecord(CHANNELS, channel));
  }

  /**
   * Returns whether a channel exists in the database
   * 
   * @param channelid the target channel
   * @return true if the channel exists in the database
   */
  public boolean isChannelAdded(long channelid) {
    return dsl.selectFrom(CHANNELS).where(CHANNELS.CHANNELID.eq(channelid)).fetchOne(0,
        int.class) != 0;
  }

  /**
   * Returns whether a channel exists in the database and has read permission for the bot.
   * 
   * @param channelid the target channel
   * @return true if the channel has read permission
   */
  public boolean hasReadPermission(long channelid) {
    return dsl.selectFrom(CHANNELS).where(CHANNELS.CHANNELID.eq(channelid).and(CHANNELS.READ_PERM))
        .fetchOne(0, int.class) != 0;
  }

  /**
   * Returns whether a channel exists in the database and has write permission for the bot.
   * 
   * @param channelid the target channel
   * @return true if the channel has write permission
   */
  public boolean hasWritePermission(long channelid) {
    return dsl.selectFrom(CHANNELS).where(CHANNELS.CHANNELID.eq(channelid).and(CHANNELS.WRITE_PERM))
        .fetchOne(0, int.class) != 0;
  }

  /**
   * Sets bot's read and write permissions to an existing channel in the database.
   * 
   * @param channelid the target channel
   * @param read      whether the bot should have read access to the channel
   * @param write     whether the bot should have write access to the channel
   */
  public void updatePermissions(long channelid, boolean read, boolean write) {
    dsl.update(CHANNELS).set((CHANNELS.READ_PERM), read).set((CHANNELS.WRITE_PERM), write)
        .where(CHANNELS.CHANNELID.eq(channelid)).execute();
  }

  /**
   * Retrieves all channels in the database
   * 
   * @return list of all channels
   */
  public List<Channels> getAll(long serverid) {
    return dsl.selectFrom(CHANNELS).where(CHANNELS.SERVERID.eq(serverid)).fetchInto(Channels.class);
  }

  public List<Channels> getAllReadable(long serverid) {
    return dsl.selectFrom(CHANNELS).where(CHANNELS.READ_PERM).and(CHANNELS.SERVERID.eq(serverid))
        .fetchInto(Channels.class);
  }

  /**
   * Convenience method to delete a channel
   * 
   * @param channelid the target channel to delete
   * @return the number of rows deleted in the channel table
   */
  public int delete(String channelid) {
    return delete(Long.parseLong(channelid));
  }

  /**
   * Delete all data related to a channel
   * 
   * @param channelid the target channel to delete
   * @return the number of rows deleted in the channel table
   */
  public int delete(long channelid) {
    return dsl.deleteFrom(CHANNELS).where(CHANNELS.CHANNELID.eq(channelid)).execute();
  }
}
