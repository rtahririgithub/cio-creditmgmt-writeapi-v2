package com.telus.credit.dao;

import static com.telus.credit.dao.entity.PartyContactMediumEntity.Cols.contact_medium_id;
import static com.telus.credit.dao.entity.PartyContactMediumEntity.Cols.party_id;
import static com.telus.credit.dao.entity.PartyContactMediumEntity.Cols.updated_ts;
import static com.telus.credit.dao.entity.PartyContactMediumEntity.Cols.version;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.telus.credit.dao.entity.PartyContactMediumEntity;
import com.telus.credit.dao.operation.PartyContactMediumOperation;
import com.telus.credit.dao.rowmapper.PartyContactMediumRowMapper;

@Repository
public class PartyContactMediumDao extends AbstractDao {

    public PartyContactMediumDao(DataSource dataSource) {
        super(dataSource);
    }

    public String insert(PartyContactMediumEntity entity) {
        KeyHolder keyHolder = PartyContactMediumOperation.insert(entity)
                .setParam("partyId", UUID.fromString(entity.getPartyId()))
                .execute(this);
        String key = keyHolder.getKeyList().get(0).get(contact_medium_id.name()).toString();
        entity.setContactMediumId(key);
        return key;
    }

    /**
     * Get ContactMedium by UID
     *
     * @param contactUid UID string
     * @return
     */
    public Optional<PartyContactMediumEntity> getById(String contactUid) {
        if (StringUtils.isBlank(contactUid)) {
            return Optional.empty();
        }

        return queryForObject(new PartyContactMediumRowMapper(),
                PartyContactMediumOperation.selectAll(contact_medium_id + "=?"), UUID.fromString(contactUid));
    }

    /**
     * Update contact medium by UID
     *
     * @param contactUid UID string
     * @param entity
     * @return
     */
    public int update(String contactUid, PartyContactMediumEntity entity) {
        if (StringUtils.isBlank(contactUid) || entity == null) {
            return 0;
        }
        DaoHelper.removeNullAttributes(entity.getUpdateMap());
        
        SimpleUpdate update = updateWithVersioning(entity.getUpdateMap(),
                UUID.fromString(contactUid), contact_medium_id.name(), version.name());
        return update != null ? update.set(updated_ts.name(), NOW).execute(PartyContactMediumOperation.UPDATE_PREFIX, this) : 0;
    }

    /**
     * Get ContactMedium of a list of UIDs
     *
     * @param uids a list of party uid strings
     * @return
     */
    public List<PartyContactMediumEntity> getByPartyIds(Collection<String> uids) {
        if (uids == null || uids.isEmpty()) {
            return Collections.emptyList();
        }

        StringJoiner inJoiner = new StringJoiner(",", "(", ")");
        uids.forEach(uid -> inJoiner.add("?"));
        String sql = PartyContactMediumOperation.SELECT_ALL_STATEMENT
                + " WHERE " + party_id + " IN " + inJoiner.toString();

        return getJdbcTemplate().query(sql, new PartyContactMediumRowMapper(), uids.stream().map(UUID::fromString).toArray());
    }

    /**
     * Remove ContactMedium of a list of party uids
     *
     * @param partyUids a list of party uids
     * @return number of rows removed
     */
    public int removeByPartyIds(Collection<String> partyUids) {
        if (partyUids == null || partyUids.isEmpty()) {
            return 0;
        }

        StringJoiner inJoiner = new StringJoiner(",", "(", ")");
        partyUids.forEach(uid -> inJoiner.add("?"));
        String sql = "DELETE FROM party_contact_medium WHERE " + party_id + " IN " + inJoiner.toString();

        return getJdbcTemplate().update(sql, partyUids.stream().map(UUID::fromString).toArray());
    }
}
