package com.telus.credit.dao;

import com.telus.credit.dao.entity.CustomerCreditProfileRelEntity;
import com.telus.credit.dao.entity.ProdQualEntity;
import com.telus.credit.dao.operation.ProdQualOperation;
import com.telus.credit.dao.rowmapper.ProdQualRowMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

import java.util.UUID;

import static com.telus.credit.dao.entity.ProdQualEntity.Cols.credit_profile_id;
import static com.telus.credit.dao.entity.ProdQualEntity.Cols.product_qual_id;
import static com.telus.credit.dao.entity.ProdQualEntity.Cols.valid_end_ts;
import static com.telus.credit.dao.entity.ProdQualEntity.Cols.version;
import static com.telus.credit.dao.entity.ProdQualEntity.Cols.updated_on_ts;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.telus.credit.dao.entity.ProdQualEntity;
import com.telus.credit.dao.operation.ProdQualOperation;
import com.telus.credit.dao.rowmapper.ProdQualRowMapper;

@Repository
public class ProdQualDao extends AbstractDao {

    public ProdQualDao(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * Insert entity
     *
     * @param entity
     * @return autogenerated primary key
     */
    public String insert(ProdQualEntity entity) {
        KeyHolder keyHolder = ProdQualOperation.insert(entity)
                .setParam("creditProfileId", UUID.fromString(entity.getCreditProfileId()))
                .execute(this);
        String key = keyHolder.getKeyList().get(0).get(product_qual_id.name()).toString();
        entity.setProductQualId(key);
        return key;
    }

    /**
     * Get warning by uid
     *
     * @param uid UID string
     * @return
     */
    public Optional<ProdQualEntity> getById(String uid) {
        if (StringUtils.isBlank(uid)) {
            return Optional.empty();
        }

        return queryForObject(new ProdQualRowMapper(),
                ProdQualOperation.selectAll(product_qual_id + "=?"), UUID.fromString(uid));
    }

    /**
     * Get warning by uid
     *
     * @param uid UID string
     * @return
     */

public List<ProdQualEntity> getByProfileIds(Collection<String> profileUids) {
        if (profileUids == null || profileUids.isEmpty()) {
            return Collections.emptyList();
        }

        StringJoiner inJoiner = new StringJoiner(",", "(", ")");
        profileUids.forEach(uid -> inJoiner.add("?"));
        String sql = ProdQualOperation.SELECT_ALL_STATEMENT
                + " WHERE " +  ProdQualEntity.Cols.credit_profile_id + " IN " + inJoiner.toString();

        return getJdbcTemplate().query(sql, new ProdQualRowMapper(), profileUids.stream().map(UUID::fromString).toArray());
        }


    public Optional<List<ProdQualEntity>> getByProfileUuid(String uid) {
        if (StringUtils.isBlank(uid)) {
            return Optional.empty();
        }

        return query(new ProdQualRowMapper(),
                ProdQualOperation.selectAll(credit_profile_id + "=?"), UUID.fromString(uid));
    }
    /**
     * Update entity
     *
     * @param productQualId UUID string
     * @param entity
     * @return number of rows updated
     */
    public int update(String productQualId, ProdQualEntity entity) {
        if (StringUtils.isBlank(productQualId) || entity == null) {
            return 0;
        }
        DaoHelper.removeNullAttributes(entity.getUpdateMap());
        Map<String, Object> updateMap = entity.getUpdateMap();
        UUID creditProfileIdUuid = null;

        if(entity.getCreditProfileId()!=null ) {
            creditProfileIdUuid=UUID.fromString(entity.getCreditProfileId());
            updateMap.replace(ProdQualEntity.Cols.credit_profile_id.name(), creditProfileIdUuid);
        }
        SimpleUpdate update = updateWithVersioning(entity.getUpdateMap(),UUID.fromString(productQualId), product_qual_id.name(), version.name());
        return update.set(ProdQualEntity.Cols.updated_on_ts.name(), NOW).execute("UPDATE prod_qual SET ", this);
    }

    /**
     * Update entity
     *
     * @param productQualId UUID string
     * @param entity
     * @return number of rows updated
     */
    public int remove(String productQualId, ProdQualEntity entity) {
        if (StringUtils.isBlank(productQualId) || entity == null) {
            return 0;
        }
        DaoHelper.removeNullAttributes(entity.getUpdateMap());
        Map<String, Object> updateMap = entity.getUpdateMap();
        UUID creditProfileIdUuid = null;

        if(entity.getCreditProfileId()!=null ) {
            creditProfileIdUuid=UUID.fromString(entity.getCreditProfileId());
            updateMap.replace(ProdQualEntity.Cols.credit_profile_id.name(), creditProfileIdUuid);
        }
        SimpleUpdate update = updateWithVersioning(entity.getUpdateMap(),
                UUID.fromString(productQualId), product_qual_id.name(), version.name());
        return update != null ? update.set(updated_on_ts.name(), NOW).set(valid_end_ts.name(), NOW).execute(ProdQualOperation.UPDATE_PREFIX, this) : 0;
    }
    
    public int deleteByProfileId(String profileUid) {
        if (profileUid == null || profileUid.isEmpty()) {
            return 0;
        }

        String sql = "DELETE from prod_qual " +  " WHERE " + credit_profile_id  + "=?" ;
        return getJdbcTemplate().update(sql, UUID.fromString(profileUid));
    }    
}
