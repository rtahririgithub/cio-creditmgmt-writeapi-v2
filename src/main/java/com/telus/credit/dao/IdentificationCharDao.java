package com.telus.credit.dao;

import static com.telus.credit.dao.entity.IdentificationCharEntity.Cols.identification_char_id;
import static com.telus.credit.dao.entity.IdentificationCharEntity.Cols.identificaton_id;
import static com.telus.credit.dao.entity.IdentificationCharEntity.Cols.key;
import static com.telus.credit.dao.entity.IdentificationCharEntity.Cols.updated_ts;
import static com.telus.credit.dao.entity.IdentificationCharEntity.Cols.version;

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

import com.telus.credit.dao.entity.IdentificationCharEntity;
import com.telus.credit.dao.operation.IdentificationCharOperation;
import com.telus.credit.dao.rowmapper.IdentificationCharRowMapper;

@Repository
public class IdentificationCharDao extends AbstractDao {

    public IdentificationCharDao(DataSource dataSource) {
        super(dataSource);
    }

    public String insert(IdentificationCharEntity entity) {
        KeyHolder keyHolder = IdentificationCharOperation.insert(entity)
                .setParam("identificatonId", UUID.fromString(entity.getIdentificatonId()))
                .execute(this);
        String key = keyHolder.getKeyList().get(0).get(IdentificationCharEntity.Cols.identification_char_id.name()).toString();
        entity.setIdentificatonId(key);
        return key;
    }

    /**
     * Update entity
     *
     * @param identificationCharUid UID string of the Identification
     * @param entity
     * @return number of rows updated
     */
    public int update(String identificationCharUid, IdentificationCharEntity entity) {
        if (StringUtils.isBlank(identificationCharUid) || entity == null) {
            return 0;
        }
        DaoHelper.removeNullAttributes(entity.getUpdateMap());
        
        SimpleUpdate update = updateWithVersioning(entity.getUpdateMap(),
                UUID.fromString(identificationCharUid), identification_char_id.name(), version.name());
        return update != null ? update.set(updated_ts.name(), NOW).execute(IdentificationCharOperation.UPDATE_PREFIX, this) : 0;
    }

    /**
     * Get a characteristic by Identification UID and the key name (DL, HC... see IdentificationType)
     *
     * @param identificationId
     * @param keyName see @com.telus.credit.model.common.IdentificationType
     * @return
     */
    public Optional<IdentificationCharEntity> getByIdentificationIdAndKey(String identificationId, String keyName) {
        if (StringUtils.isBlank(identificationId) || StringUtils.isBlank(keyName)) {
            return Optional.empty();
        }

        return queryForObject(new IdentificationCharRowMapper(),
                IdentificationCharOperation.selectAll(identificaton_id + "=? AND " + key + "=?"),
                UUID.fromString(identificationId), keyName);
    }

    /**
     * Get characteristic of a list of Identification UIDs
     *
     * @param uids UID Strings of Identification
     * @return List of Identification characteristic
     */
    public List<IdentificationCharEntity> getByIdentificationIds(Collection<String> uids) {
        if (uids == null || uids.isEmpty()) {
            return Collections.emptyList();
        }

        StringJoiner inJoiner = new StringJoiner(",", "(", ")");
        uids.forEach(uid -> inJoiner.add("?"));
        String sql = IdentificationCharOperation.SELECT_ALL_STATEMENT
                + " WHERE " + identificaton_id + " IN " + inJoiner.toString();

        return getJdbcTemplate().query(sql, new IdentificationCharRowMapper(), uids.stream().map(UUID::fromString).toArray());
    }

    /**
     * Delete characteristic belonging to an Identification
     *
     * @param identificationUid UID string of the identification
     * @return
     */
    public int deleteDeleteByIdentificationId(String identificationUid) {
        if (StringUtils.trimToNull(identificationUid) == null) {
            return 0;
        }

        String sql = "DELETE FROM identification_char WHERE " + identificaton_id.name() + "=?";
        return getJdbcTemplate().update(sql, UUID.fromString(identificationUid));
    }
}
