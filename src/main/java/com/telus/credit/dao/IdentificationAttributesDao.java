package com.telus.credit.dao;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.telus.credit.dao.entity.IdentificationAttributesEntity;
import com.telus.credit.dao.operation.IdentificationAttributesOperation;
import com.telus.credit.dao.rowmapper.IdentificationAttributesRowMapper;

@Repository
public class IdentificationAttributesDao extends AbstractDao {

    public IdentificationAttributesDao(DataSource dataSource) {
        super(dataSource);
    }

    public List<IdentificationAttributesEntity> selectAll() {
        return this.getJdbcTemplate().query(IdentificationAttributesOperation.SELECT_ALL_STATEMENT,
                new IdentificationAttributesRowMapper());
    }
}
