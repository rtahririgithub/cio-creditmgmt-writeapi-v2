package com.telus.credit.dao;


import static com.telus.credit.dao.entity.CustomerEntity.Cols.updated_ts;
import static com.telus.credit.dao.entity.CustomerEntity.Cols.version;
import static com.telus.credit.dao.entity.PartyEntity.Cols.party_id;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.telus.credit.dao.entity.PartyEntity;
import com.telus.credit.dao.operation.PartyOperation;
import com.telus.credit.dao.rowmapper.PartyRowMapper;

@Repository
public class PartyDao extends AbstractDao {

    public PartyDao(DataSource dataSource) {
        super(dataSource);
    }

    public String insert(PartyEntity entity) {
        KeyHolder keyHolder = PartyOperation.insert(entity).execute(this);
        String key = keyHolder.getKeyList().get(0).get(party_id.name()).toString();
        entity.setPartyId(key);
        return key;
    }


    public Optional<PartyEntity> getById(String uid) {
        String sql = PartyOperation.SELECT_ALL_STATEMENT + " WHERE " + party_id + " =?";
        try {
			return queryForObject(new PartyRowMapper(), sql, UUID.fromString(uid));
		} catch (Exception e) {
			return Optional.empty(); 
		}

    }    
    public int removeById(String uid) {
        if (StringUtils.trimToNull(uid) == null) {
            return 0;
        }

        String sql = "DELETE from party WHERE " + party_id + " =?";
        return getJdbcTemplate().update(sql, UUID.fromString(uid));
    }
    
  
    
    public int update(String partyId, PartyEntity entity) {
        if (StringUtils.isBlank(partyId) || entity == null) {
            return 0;
        }

        DaoHelper.removeNullAttributes(entity.getUpdateMap());
        Map<String, Object> updateMap = entity.getUpdateMap();

        SimpleUpdate update = updateWithVersioning(updateMap, UUID.fromString(partyId), party_id.name(), version.name());
        return update != null ? update.set(updated_ts.name(), NOW).execute(PartyOperation.UPDATE_PREFIX, this) : 0;
    }
    
}
