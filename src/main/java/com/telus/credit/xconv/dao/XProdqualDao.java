package com.telus.credit.xconv.dao;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.telus.credit.dao.AbstractDao;
import com.telus.credit.dao.entity.XProdqualEntity;
import com.telus.credit.dao.operation.XProdqualOperation;
import com.telus.credit.dao.rowmapper.XProdqualRowMapper;

@Repository
public class XProdqualDao extends AbstractDao {

    public XProdqualDao(DataSource dataSource) {
        super(dataSource);
    }

    public List<XProdqualEntity> getByCustomerId(long custId) {
        return getJdbcTemplate().query(XProdqualOperation.selectAll(" credit_apprvd_prod_catgy_cd is not null AND customer_id=?"),
                new XProdqualRowMapper(), custId);
    }
}
