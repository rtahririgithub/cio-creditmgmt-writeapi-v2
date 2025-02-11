package com.telus.credit.dao;

import static com.telus.credit.dao.entity.OrganizationEntity.Cols.party_id;
import static com.telus.credit.dao.entity.OrganizationEntity.Cols.updated_ts;
import static com.telus.credit.dao.entity.OrganizationEntity.Cols.version;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import com.telus.credit.dao.entity.OrganizationEntity;
import com.telus.credit.dao.operation.OrganizationOperation;
import com.telus.credit.dao.rowmapper.OrganizationRowMapper;

@Repository
public class OrganizationDao extends AbstractDao {

    public OrganizationDao(DataSource dataSource) {
        super(dataSource);
    }

    public void insert(OrganizationEntity entity) {
        OrganizationOperation.insert(entity)
                .setParam("partyId", UUID.fromString(entity.getPartyId()))
                .execute(this);
    }
    /**
     * Get a list of Individuals of a list of party uids
     *
     * @param partyUids a list of Party UIDs
     * @return
     */
    public List<OrganizationEntity> getByPartyIds(Collection<String> partyUids) {
        if (partyUids == null || partyUids.isEmpty()) {
            return Collections.emptyList();
        }

        StringJoiner inJoiner = new StringJoiner(",", "(", ")");
        partyUids.forEach(uid -> inJoiner.add("?"));
        String sql = OrganizationOperation.SELECT_ALL_STATEMENT
                + " WHERE " + party_id + " IN " + inJoiner.toString();

        return getJdbcTemplate().query(sql, new OrganizationRowMapper(), partyUids.stream().map(UUID::fromString).toArray());
    }
    
    
    /**
     * Update  by party UID
     *
     * @param partyId UID string of the party
     * @param entity
     * @return number of row updated
     */
    public int update(String partyId, OrganizationEntity entity) {
        if (StringUtils.isBlank(partyId) || entity == null) {
            return 0;
        }

        DaoHelper.removeNullAttributes(entity.getUpdateMap());
        
        SimpleUpdate update = updateWithVersioning(entity.getUpdateMap(),
                UUID.fromString(partyId), party_id.name(), version.name());
        return update != null ? update.set(updated_ts.name(), NOW).execute(OrganizationOperation.UPDATE_PREFIX, this) : 0;
    }    
    /**
     * Remove Organizations of a list of party uids
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
        String sql = "DELETE FROM organization WHERE " + party_id + " IN " + inJoiner.toString();

        return getJdbcTemplate().update(sql, partyUids.stream().map(UUID::fromString).toArray());
    }
}
