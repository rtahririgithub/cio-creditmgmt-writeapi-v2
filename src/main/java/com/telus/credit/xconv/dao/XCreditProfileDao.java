package com.telus.credit.xconv.dao;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.telus.credit.dao.AbstractDao;
import com.telus.credit.dao.entity.XCreditProfileEntity;
import com.telus.credit.dao.operation.XCreditProfileOperation;
import com.telus.credit.dao.rowmapper.XCreditProfileRowMapper;

@Repository
public class XCreditProfileDao extends AbstractDao {

    public XCreditProfileDao(DataSource dataSource) {
        super(dataSource);
    }

    public Optional<XCreditProfileEntity> getXCreditProfileByCustomerID(long custId) {
    	
        return queryForObject(new XCreditProfileRowMapper(),
                XCreditProfileOperation.selectAll(" cprofl_cust_map_typ_cd='PRI' AND credit_class_code is not null AND customer_id=? "), custId);
    }
    
    
    public Optional<List<XCreditProfileEntity>> getAll() {
    	String whereStmt= 
    			  " birth_date is not null "
    			+ " and dl_num is not null "
    			+ " and status='A' "
    			+ " and credit_class_code  is not null "
    			+ " and application_sub_prov_cd is not null " +
    			"  and customer_id < 3366211 " +
    			"  order by  credit_profile_ts  desc "+
    			" LIMIT 15000 ";
        return query(
        		new XCreditProfileRowMapper(),
                XCreditProfileOperation.selectAll(whereStmt), 
                new  Object[0]
                );
    }
        
    
}
