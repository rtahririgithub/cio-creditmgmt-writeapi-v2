package com.telus.credit.dao;

import static com.telus.credit.dao.entity.PartyIdentificationEntity.Cols.id_type;
import static com.telus.credit.dao.entity.PartyIdentificationEntity.Cols.identificaton_id;
import static com.telus.credit.dao.entity.PartyIdentificationEntity.Cols.party_id;
import static com.telus.credit.dao.entity.PartyIdentificationEntity.Cols.updated_ts;
import static com.telus.credit.dao.entity.PartyIdentificationEntity.Cols.version;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.telus.credit.dao.entity.PartyIdentificationEntity;
import com.telus.credit.dao.entity.PartyIdentificationExEntity;
import com.telus.credit.dao.operation.PartyIdentificationOperation;
import com.telus.credit.dao.rowmapper.PartyIdentificationRowMapper;

@Repository
public class PartyIdentificationDao extends AbstractDao {

    public PartyIdentificationDao(DataSource dataSource) {
        super(dataSource);
    }

    public String insert(PartyIdentificationEntity entity) {
        KeyHolder keyHolder = PartyIdentificationOperation.insert(entity)
                .setParam("partyId", UUID.fromString(entity.getPartyId()))
                .execute(this);
        String key = keyHolder.getKeyList().get(0).get(identificaton_id.name()).toString();
        entity.setIdentificatonId(key);
        return key;
    }

    /**
     * Update entity
     *
     * @param identificationId Identification UID
     * @param entity
     * @return number of rows updated
     */
    public int update(String identificationId, PartyIdentificationEntity entity) {
        if (StringUtils.isBlank(identificationId) || entity == null) {
            return 0;
        }

        SimpleUpdate update = updateWithVersioning(entity.getUpdateMap(),
                UUID.fromString(identificationId), identificaton_id.name(), version.name());
        return update != null ? update.set(updated_ts.name(), NOW).execute(PartyIdentificationOperation.UPDATE_PREFIX, this) : 0;
    }

    /**
     * Get PartyIdentification by party UID and ID type (DL, HC... see @IdentificationType)
     *
     * @param partyId party UID
     * @param idType see @com.telus.credit.model.common.IdentificationType
     * @return
     */
    public Optional<PartyIdentificationExEntity> getByPartyIdAndIdType(String partyId, String idType) {
        if (StringUtils.isBlank(partyId) || StringUtils.isBlank(idType)) {
            return Optional.empty();
        }

        return queryForObject(new PartyIdentificationRowMapper(PartyIdentificationExEntity::new),
                PartyIdentificationOperation.selectAll(party_id + "=? AND " + id_type + "=?"),
                UUID.fromString(partyId), idType).map(e -> (PartyIdentificationExEntity)e);
    }

    /**
     * Get PartyIdentificationEntity of a list of UIDs
     *
     * @param partyUids a list of party uid strings
     * @return
     */
    public List<PartyIdentificationExEntity> getByPartyIds(Collection<String> partyUids) {
        if (partyUids == null || partyUids.isEmpty()) {
            return Collections.emptyList();
        }

        StringJoiner inJoiner = new StringJoiner(",", "(", ")");
        partyUids.forEach(uid -> inJoiner.add("?"));
        String sql = PartyIdentificationOperation.selectAll(party_id + " IN " + inJoiner);

        List<PartyIdentificationEntity> result = getJdbcTemplate().query(sql,
                new PartyIdentificationRowMapper(PartyIdentificationExEntity::new),
                partyUids.stream().map(UUID::fromString).toArray());

        return result.stream().map(e -> (PartyIdentificationExEntity)e).collect(Collectors.toList());
    }

    /**
     * Remove PartyIdentification of a list of party uids
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
        String sql = "DELETE FROM party_identification WHERE " + party_id + " IN " + inJoiner;

        return getJdbcTemplate().update(sql, partyUids.stream().map(UUID::fromString).toArray());
    }
}
