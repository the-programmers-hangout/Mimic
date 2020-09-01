/*
 * This file is generated by jOOQ.
 */
package uk.co.markg.mimic.db.tables.records;


import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;

import uk.co.markg.mimic.db.tables.Channels;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ChannelsRecord extends UpdatableRecordImpl<ChannelsRecord> implements Record3<Long, Boolean, Boolean> {

    private static final long serialVersionUID = 240773967;

    /**
     * Setter for <code>channels.channelid</code>.
     */
    public void setChannelid(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>channels.channelid</code>.
     */
    public Long getChannelid() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>channels.read_perm</code>.
     */
    public void setReadPerm(Boolean value) {
        set(1, value);
    }

    /**
     * Getter for <code>channels.read_perm</code>.
     */
    public Boolean getReadPerm() {
        return (Boolean) get(1);
    }

    /**
     * Setter for <code>channels.write_perm</code>.
     */
    public void setWritePerm(Boolean value) {
        set(2, value);
    }

    /**
     * Getter for <code>channels.write_perm</code>.
     */
    public Boolean getWritePerm() {
        return (Boolean) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<Long, Boolean, Boolean> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<Long, Boolean, Boolean> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<Long> field1() {
        return Channels.CHANNELS.CHANNELID;
    }

    @Override
    public Field<Boolean> field2() {
        return Channels.CHANNELS.READ_PERM;
    }

    @Override
    public Field<Boolean> field3() {
        return Channels.CHANNELS.WRITE_PERM;
    }

    @Override
    public Long component1() {
        return getChannelid();
    }

    @Override
    public Boolean component2() {
        return getReadPerm();
    }

    @Override
    public Boolean component3() {
        return getWritePerm();
    }

    @Override
    public Long value1() {
        return getChannelid();
    }

    @Override
    public Boolean value2() {
        return getReadPerm();
    }

    @Override
    public Boolean value3() {
        return getWritePerm();
    }

    @Override
    public ChannelsRecord value1(Long value) {
        setChannelid(value);
        return this;
    }

    @Override
    public ChannelsRecord value2(Boolean value) {
        setReadPerm(value);
        return this;
    }

    @Override
    public ChannelsRecord value3(Boolean value) {
        setWritePerm(value);
        return this;
    }

    @Override
    public ChannelsRecord values(Long value1, Boolean value2, Boolean value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ChannelsRecord
     */
    public ChannelsRecord() {
        super(Channels.CHANNELS);
    }

    /**
     * Create a detached, initialised ChannelsRecord
     */
    public ChannelsRecord(Long channelid, Boolean readPerm, Boolean writePerm) {
        super(Channels.CHANNELS);

        set(0, channelid);
        set(1, readPerm);
        set(2, writePerm);
    }
}
