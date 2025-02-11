package com.telus.credit.dao;




import static com.telus.credit.dao.entity.CproflMappingEntity.Cols.cprofl_mapping_id;
import static com.telus.credit.dao.entity.CproflMappingEntity.Cols.updated_on_ts;
import static com.telus.credit.dao.entity.CproflMappingEntity.Cols.version;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.telus.credit.dao.entity.CproflMappingEntity;
import com.telus.credit.dao.operation.CproflMappingOperation;
import com.telus.credit.dao.rowmapper.CproflMappingRowMapper;;

@Repository
public class CproflMappingDao extends AbstractDao {

    public CproflMappingDao(DataSource dataSource) {
        super(dataSource);
    }

    public String insert(CproflMappingEntity entity) {
    	
    	UUID cproflFromIdUuid=UUID.fromString(entity.getCproflFromId());
    	UUID cproflToIdUuid=UUID.fromString(entity.getCproflToId());

        KeyHolder keyHolder = CproflMappingOperation.insert(entity)
					.setParam("cproflFromId", cproflFromIdUuid)
					.setParam("cproflToId", cproflToIdUuid)
					.execute(this);

        String key = keyHolder.getKeyList().get(0).get(cprofl_mapping_id.name()).toString();
        entity.setCproflMappingId(key);
        return key;
    }


    public Optional<CproflMappingEntity> getById(String uid) {
        String sql = CproflMappingOperation.SELECT_ALL_STATEMENT + " WHERE " + cprofl_mapping_id + " =?";
        try {
			return queryForObject(new CproflMappingRowMapper(), sql, UUID.fromString(uid));
		} catch (Exception e) {
			return Optional.empty(); 
		}

    }    
    public int removeById(String uid) {
        if (StringUtils.trimToNull(uid) == null) {
            return 0;
        }

        String sql = "DELETE from credit_mapping WHERE " + cprofl_mapping_id + " =?";
        return getJdbcTemplate().update(sql, UUID.fromString(uid));
    }
    
  
    
    public int update(String partyId, CproflMappingEntity entity) {
        if (StringUtils.isBlank(partyId) || entity == null) {
            return 0;
        }

        DaoHelper.removeNullAttributes(entity.getUpdateMap());
        Map<String, Object> updateMap = entity.getUpdateMap();

        SimpleUpdate update = updateWithVersioning(updateMap, UUID.fromString(partyId), cprofl_mapping_id.name(), version.name());
        return update != null ? update.set(updated_on_ts.name(), NOW).execute(CproflMappingOperation.UPDATE_PREFIX, this) : 0;
    }
    
}
