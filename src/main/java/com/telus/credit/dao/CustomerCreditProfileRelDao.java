package com.telus.credit.dao;

import static com.telus.credit.dao.entity.CustomerCreditProfileRelEntity.Cols.credit_profile_customer_id;
import static com.telus.credit.dao.entity.CustomerCreditProfileRelEntity.Cols.credit_profile_id;
import static com.telus.credit.dao.entity.CustomerCreditProfileRelEntity.Cols.updated_on_ts;
import static com.telus.credit.dao.entity.CustomerCreditProfileRelEntity.Cols.version;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import com.telus.credit.dao.entity.CustomerCreditProfileRelEntity;
import com.telus.credit.dao.entity.CustomerEntity;
import com.telus.credit.dao.mapper.CompositeRowMapper;
import com.telus.credit.dao.mapper.CompositeRowMapper.CompositeEntity;
import com.telus.credit.dao.operation.CustomerCreditProfileRelOperation;
import com.telus.credit.dao.rowmapper.CreditProfileRowMapper;
import com.telus.credit.dao.rowmapper.CustomerCreditProfileRelRowMapper;
import com.telus.credit.dao.rowmapper.CustomerRowMapper;

@Repository
public class CustomerCreditProfileRelDao extends AbstractDao {

    public CustomerCreditProfileRelDao(DataSource dataSource) {
        super(dataSource);
    }

    public void insert(CustomerCreditProfileRelEntity entity) {
        CustomerCreditProfileRelOperation.insert(entity)
                .setParam("creditProfileCustomerId", UUID.fromString(entity.getCreditProfileCustomerId()))
                .setParam("creditProfileId", UUID.fromString(entity.getCreditProfileId()))
                .execute(this);
    }

    /**
     * Remove all relations based on credit profile uids
     *
     * @param profileUids List of credit profile UID strings
     * @return
     */
    public int deleteByProfileIds(Collection<String> profileUids) {
        if (profileUids == null || profileUids.isEmpty()) {
            return 0;
        }

        StringJoiner inJoiner = new StringJoiner(",", "(", ")");
        profileUids.forEach(uid -> inJoiner.add("?"));
        String sql = "DELETE from CUSTOMER_CREDIT_PROFILE_REL WHERE CREDIT_PROFILE_ID IN " + inJoiner.toString();

        return getJdbcTemplate().update(sql, profileUids.stream().map(UUID::fromString).toArray());
    }

    //TODO fix the issue . it could return list 
    public Optional<CustomerCreditProfileRelEntity> getCustomerCreditProfileRel(String creditProfileId) {
    	final String stmt =
                "SELECT t2.* FROM CUSTOMER_CREDIT_PROFILE_REL t2 " +
                "WHERE t2.CREDIT_PROFILE_ID = ?";   
    	
        return queryForObject(new CustomerCreditProfileRelRowMapper(), stmt, UUID.fromString(creditProfileId));
    }
 
    public Optional<CustomerCreditProfileRelEntity> getCustomerCreditProfileRel_By_CreditProfileId_And_CustomerID(String creditProfileId,String creditProfileCustomerId) {
    	final String stmt =
                "SELECT t2.* FROM CUSTOMER_CREDIT_PROFILE_REL t2 " +
                "WHERE t2.CREDIT_PROFILE_ID = ? AND  t2.credit_profile_customer_id = ?";   
    	
        return queryForObject(new CustomerCreditProfileRelRowMapper(), stmt, UUID.fromString(creditProfileId) , UUID.fromString(creditProfileCustomerId) );
    }   
    
    public Optional<CustomerCreditProfileRelEntity> getCustomerCreditProfileRel_By_CreditProfileCustomerId(String creditProfileCustomerId) {
    	final String stmt =
                "SELECT t2.* FROM CUSTOMER_CREDIT_PROFILE_REL t2 " +
                "WHERE   t2.credit_profile_customer_id = ?";     	
        return queryForObject(new CustomerCreditProfileRelRowMapper(), stmt,  UUID.fromString(creditProfileCustomerId) );
    }     
    public Optional<CustomerCreditProfileRelEntity> getPrimaryCustomerCreditProfileRelByCreditProfileId(String creditProfileId) {    	
    	String sqlSmt= "SELECT t2.* FROM CUSTOMER_CREDIT_PROFILE_REL t2 " +
    				    "  WHERE t2.CREDIT_PROFILE_ID = ? " + 
    				    "  AND ( t2.customer_credit_profile_rel_cd='PRI' OR t2.customer_credit_profile_rel_cd is null  ) "; 
        Optional<List<CustomerCreditProfileRelEntity>> x = query(new CustomerCreditProfileRelRowMapper(), sqlSmt, UUID.fromString(creditProfileId) );      
        return  Optional.of(x.get().get(0));
    }    

    public Optional<List<CustomerCreditProfileRelEntity>> get_CustomerCreditProfileRel_ListByCreditProfileId(String creditProfileId) {    	
    	String sqlSmt= "SELECT t2.* FROM CUSTOMER_CREDIT_PROFILE_REL t2 " +
    				    "  WHERE t2.CREDIT_PROFILE_ID = ? "; 
    	return query(new CustomerCreditProfileRelRowMapper(), sqlSmt, UUID.fromString(creditProfileId) );      

    }    

    public Optional<CustomerCreditProfileRelEntity> getCustomerCreditProfileRel_By_CustomerId_LOB(long customerId,String lineofbusiness) {
        final String stmt =
    	" select " +
        " CUSTOMER_CREDIT_PROFILE_REL.*  " +
        " FROM crprofl.CUSTOMER_CREDIT_PROFILE_REL CUSTOMER_CREDIT_PROFILE_REL   " +
        " inner join CRPROFL.customer customer on customer.credit_profile_customer_id =CUSTOMER_CREDIT_PROFILE_REL.credit_profile_customer_id and customer.line_of_business=? and customer.customer_id =? " +
        "  WHERE ( CUSTOMER_CREDIT_PROFILE_REL.customer_credit_profile_rel_cd='PRI' OR CUSTOMER_CREDIT_PROFILE_REL.customer_credit_profile_rel_cd is null  ) "; 
    	    	
        return queryForObject(new CustomerCreditProfileRelRowMapper(), stmt, lineofbusiness,customerId );
    } 
    
    
    
    
    


	
    public Optional<CustomerEntity> getPrimaryCustomerByCreditProfileId(String creditProfileId) {    	
    	String sqlSmt =    	
		    " SELECT CUSTOMER.* FROM crprofl.CUSTOMER CUSTOMER  "+
		    " INNER JOIN crprofl.CUSTOMER_CREDIT_PROFILE_REL CUSTOMER_CREDIT_PROFILE_REL   "+
		    " ON CUSTOMER.credit_profile_customer_id=CUSTOMER_CREDIT_PROFILE_REL.credit_profile_customer_id    "+
		    " WHERE CUSTOMER_CREDIT_PROFILE_REL.CREDIT_PROFILE_ID = ? " +
		    "  AND ( CUSTOMER_CREDIT_PROFILE_REL.customer_credit_profile_rel_cd='PRI' OR CUSTOMER_CREDIT_PROFILE_REL.customer_credit_profile_rel_cd is null  ) "; 
    	
        Optional<List<CustomerEntity>> x = query(new CustomerRowMapper(), sqlSmt, UUID.fromString(creditProfileId) );      
        return  Optional.of(x.get().get(0));
    }   
    

    

    
    public Optional<List<CompositeEntity>> get_CompositeEntity_Customer_CustomerCreditProfileRel_ByCreditProfileId(String creditProfileId) {
		final String stmt =    	
		   " SELECT  " +
		   "  CUSTOMER.* , " +
		   "  CUSTOMER_CREDIT_PROFILE_REL.* " +
		   " FROM crprofl.CUSTOMER CUSTOMER   " +
		   "  INNER JOIN crprofl.CUSTOMER_CREDIT_PROFILE_REL CUSTOMER_CREDIT_PROFILE_REL  " +
		   "  ON CUSTOMER.credit_profile_customer_id=CUSTOMER_CREDIT_PROFILE_REL.credit_profile_customer_id  " +
		   " WHERE  " +
		   " 	CUSTOMER_CREDIT_PROFILE_REL.CREDIT_PROFILE_ID  = ? "
		   ;
		CompositeRowMapper aCompositeRowMapper = new CompositeRowMapper( new CustomerRowMapper(), new CustomerCreditProfileRelRowMapper());
		 Optional<List<CompositeEntity>> result2 = query( 
				 aCompositeRowMapper
				,  stmt
				, UUID.fromString(creditProfileId) 
				); 
		
		return result2;
    }     
 
    public Optional<List<CompositeEntity>> get_CreditProfile_CustomerCreditProfileRel_ByCreditProfileCustomerId(String creditProfileCustomerId) {
		final String stmt =    	
		   " SELECT  " +
		   "  CREDIT_PROFILE.* , " +
		   "  CUSTOMER_CREDIT_PROFILE_REL.* " +
		   " FROM crprofl.CREDIT_PROFILE CREDIT_PROFILE   " +
		   "  INNER JOIN crprofl.CUSTOMER_CREDIT_PROFILE_REL CUSTOMER_CREDIT_PROFILE_REL  " +
		   "  ON CREDIT_PROFILE.credit_profile_id=CUSTOMER_CREDIT_PROFILE_REL.credit_profile_id  " +
		   " WHERE  " +
		   " 	CUSTOMER_CREDIT_PROFILE_REL.credit_profile_customer_id  = ? "
		   ;
		CompositeRowMapper aCompositeRowMapper = new CompositeRowMapper( new CreditProfileRowMapper(), new CustomerCreditProfileRelRowMapper());
		 Optional<List<CompositeEntity>> result2 = query( 
				 aCompositeRowMapper
				,  stmt
				, UUID.fromString(creditProfileCustomerId) 
				); 
		
		return result2;
    }     
        
 
    /**
     * Update entity
     *
     * @param creditProfileCustomerId UID
     * @param entity
     * @return number of rows updated
     */
    public int update(String creditProfileCustomerId, CustomerCreditProfileRelEntity entity) {
        if (StringUtils.isBlank(creditProfileCustomerId) || entity == null) {
            return 0;
        }  	
        DaoHelper.removeNullAttributes(entity.getUpdateMap());
        Map<String, Object> updateMap = entity.getUpdateMap();
    	UUID creditProfileIdUuid = null;

		if(entity.getCreditProfileId()!=null ) {
			creditProfileIdUuid=UUID.fromString(entity.getCreditProfileId());
			updateMap.replace(credit_profile_id.name(), creditProfileIdUuid);
		}
   	
        SimpleUpdate update = updateWithVersioning(entity.getUpdateMap(), UUID.fromString(creditProfileCustomerId), credit_profile_customer_id.name(), version.name());
        int rslt = update.set(updated_on_ts.name(), NOW).execute("UPDATE customer_credit_profile_rel SET ", this);
        return update != null ? rslt : 0;
     }


}
