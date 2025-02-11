package com.telus.credit.xconv.dao;

import static com.telus.credit.dao.entity.XSyncStatusEntity.Cols.customer_id;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.telus.credit.dao.AbstractDao;
import com.telus.credit.dao.SimpleUpdate;
import com.telus.credit.dao.entity.XSyncStatusEntity;
import com.telus.credit.dao.operation.XSyncStatusOperation;
import com.telus.credit.dao.rowmapper.XSyncStatusRowMapper;

@Repository
public class XSyncStatusDao extends AbstractDao {

    public XSyncStatusDao(DataSource dataSource) {
        super(dataSource);
    }

    public int update(long custId, XSyncStatusEntity entity) {
        return new SimpleUpdate(entity.getUpdateMap())
                .where(customer_id.name() + "=?", custId)
                .execute(XSyncStatusOperation.UPDATE_PREFIX, this);
    }

    public Optional<XSyncStatusEntity> getByCustomerId(long custId) {
    	
    	return queryForObject(new XSyncStatusRowMapper(), XSyncStatusOperation.selectAll(" need_to_sync is TRUE  AND customer_id = ?"), custId);
    }
}
