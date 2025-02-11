package com.telus.credit.xconv.dao;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.telus.credit.dao.AbstractDao;
import com.telus.credit.dao.entity.XWarningEntity;
import com.telus.credit.dao.operation.XWarningOperation;
import com.telus.credit.dao.rowmapper.XWarningRowMapper;

@Repository
public class XWarningDao extends AbstractDao {

    public XWarningDao(DataSource dataSource) {
        super(dataSource);
    }

    public List<XWarningEntity> getByCustomerId(long custId) {
        return getJdbcTemplate().query(XWarningOperation.selectAll("customer_id=?"),
                new XWarningRowMapper(), custId);
    }
}
