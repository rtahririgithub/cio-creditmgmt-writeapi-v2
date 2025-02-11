package com.telus.credit.dao;

import static com.telus.credit.dao.entity.ReaddbSyncStatusEntity.Cols.credit_profile_customer_id;
import static com.telus.credit.dao.entity.ReaddbSyncStatusEntity.Cols.updated_on_ts;

import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.telus.credit.dao.entity.ReaddbSyncStatusEntity;
import com.telus.credit.dao.operation.ReaddbSyncStatusOperation;
import com.telus.credit.dao.rowmapper.CreditProfileRowMapper;
import com.telus.credit.dao.rowmapper.ReaddbSyncStatusRowMapper;

@Repository
public class ReadDbSyncStatusDao extends AbstractDao {

    public ReadDbSyncStatusDao(DataSource dataSource) {
        super(dataSource);
    }

    public void insert(ReaddbSyncStatusEntity entity) {
    	if(entity.getCreditProfileCustomerId()==null || entity.getCreditProfileCustomerId().trim().isEmpty()) {
    		return;
    	}
    	
    	UUID uuid=null;
		try {
			uuid = UUID.fromString(entity.getCreditProfileCustomerId());
		} catch (Exception e) {
			return;
		}
    	
        Optional<ReaddbSyncStatusEntity> aReaddbSyncStatusEntity = queryForObject(
        		new ReaddbSyncStatusRowMapper()
        		,"SELECT t1.* from readdb_sync_status t1 WHERE t1.credit_profile_customer_id = ?" 
        		, uuid);
    	if(aReaddbSyncStatusEntity==null || !aReaddbSyncStatusEntity.isPresent()) {
				ReaddbSyncStatusOperation.insert(entity).setParam("creditProfileCustomerId", uuid).execute(this);
    	}else {
    		update(entity.getCreditProfileCustomerId(), new ReaddbSyncStatusEntity().needToSync(true));
    	}
    }

    public int update(String customerUid, ReaddbSyncStatusEntity entity) {
    	if(customerUid==null || customerUid.trim().isEmpty()) {
    		return 0;
    	}
    	UUID uuid=null;
		try {
			uuid = UUID.fromString(customerUid);
		} catch (Exception e) {
			return 0;
		}
		
        DaoHelper.removeNullAttributes(entity.getUpdateMap());
        return new SimpleUpdate(entity.getUpdateMap()).set(updated_on_ts.name(), NOW)
                .where(credit_profile_customer_id.name() + "=?", uuid)
                .execute(ReaddbSyncStatusOperation.UPDATE_PREFIX, this);
    }
}
