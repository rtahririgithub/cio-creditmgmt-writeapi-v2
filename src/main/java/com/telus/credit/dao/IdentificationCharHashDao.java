package com.telus.credit.dao;

import static com.telus.credit.dao.entity.IdentificationCharHashEntity.Cols.identification_hash_id;
import static com.telus.credit.dao.entity.IdentificationCharHashEntity.Cols.identificaton_id;
import static com.telus.credit.dao.entity.IdentificationCharHashEntity.Cols.key;
import static com.telus.credit.dao.entity.IdentificationCharHashEntity.Cols.updated_ts;
import static com.telus.credit.dao.entity.IdentificationCharHashEntity.Cols.version;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import com.telus.credit.dao.entity.IdentificationCharHashEntity;
import com.telus.credit.dao.operation.IdentificationCharHashOperation;
import com.telus.credit.dao.rowmapper.IdentificationCharHashRowMapper;

@Repository
public class IdentificationCharHashDao extends AbstractDao {

    public IdentificationCharHashDao(DataSource dataSource) {
        super(dataSource);
    }

    public void insert(IdentificationCharHashEntity entity) {
        IdentificationCharHashOperation.insert(entity)
                .setParam("identificatonId", UUID.fromString(entity.getIdentificatonId()))
                .execute(this);
    }

    /**
     * Update entity
     *
     * @param identificationCharHashUid UID string of the Identification
     * @param entity
     * @return number of rows updated
     */
    public int update(String identificationCharHashUid, IdentificationCharHashEntity entity) {
        if (StringUtils.isBlank(identificationCharHashUid) || entity == null) {
            return 0;
        }
        DaoHelper.removeNullAttributes(entity.getUpdateMap());

        SimpleUpdate update = updateWithVersioning(entity.getUpdateMap(),
                UUID.fromString(identificationCharHashUid), identification_hash_id.name(), version.name());
        return update != null ? update.set(updated_ts.name(), NOW).execute(IdentificationCharHashOperation.UPDATE_PREFIX, this) : 0;
    }

    /**
     * Get a characteristic hash by Identification UID and the key name (DL, HC... see IdentificationType)
     *
     * @param identificationId
     * @param keyName see @com.telus.credit.model.common.IdentificationType
     * @return
     */
    public Optional<IdentificationCharHashEntity> getByIdentificationIdAndKey(String identificationId, String keyName) {
        if (StringUtils.isBlank(identificationId) || StringUtils.isBlank(keyName)) {
            return Optional.empty();
        }

        return queryForObject(new IdentificationCharHashRowMapper(),
                IdentificationCharHashOperation.selectAll(identificaton_id + "=? AND " + key + "=?"),
                UUID.fromString(identificationId), keyName);
    }

    /**
     * Get characteristic hash of a list of Identification UIDs
     *
     * @param uids UID Strings of Identification
     * @return List of Identification characteristic
     */
    public List<IdentificationCharHashEntity> getByIdentificationIds(Collection<String> uids) {
        if (uids == null || uids.isEmpty()) {
            return Collections.emptyList();
        }

        StringJoiner inJoiner = new StringJoiner(",", "(", ")");
        uids.forEach(uid -> inJoiner.add("?"));
        String sql = IdentificationCharHashOperation.SELECT_ALL_STATEMENT
                + " WHERE " + identificaton_id + " IN " + inJoiner.toString();

        return getJdbcTemplate().query(sql, new IdentificationCharHashRowMapper(), uids.stream().map(UUID::fromString).toArray());
    }

    /**
     * Delete characteristic hashes belonging to an Identification
     *
     * @param identificationUid UID string of the identification
     * @return
     */
    public int deleteDeleteByIdentificationId(String identificationUid) {
        if (StringUtils.trimToNull(identificationUid) == null) {
            return 0;
        }

        String sql = "DELETE FROM identification_char_hash WHERE " + identificaton_id.name() + "=?";
        return getJdbcTemplate().update(sql, UUID.fromString(identificationUid));
    }
}
