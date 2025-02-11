package com.telus.credit.dao;

import static com.telus.credit.dao.entity.CustomerEntity.Cols.credit_profile_customer_id;
import static com.telus.credit.dao.entity.CustomerEntity.Cols.party_id;
import static com.telus.credit.dao.entity.CustomerEntity.Cols.updated_ts;
import static com.telus.credit.dao.entity.CustomerEntity.Cols.version;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.telus.credit.dao.entity.CustomerEntity;
import com.telus.credit.dao.operation.CustomerOperation;
import com.telus.credit.dao.rowmapper.CustomerRowMapper;

@Repository
public class CustomerDao extends AbstractDao {
    public CustomerDao(DataSource dataSource) {
        super(dataSource);
    }


    public String insert(CustomerEntity entity) {
    	UUID partyIdUuid = null;

		if(entity.getPartyId()!=null ) {
			partyIdUuid=UUID.fromString(entity.getPartyId());
		}
        KeyHolder keyHolder = CustomerOperation.insert(entity)
                .setParam("partyId", partyIdUuid)
                .execute(this);
        String key = keyHolder.getKeyList().get(0).get(credit_profile_customer_id.name()).toString();
        entity.setCreditProfileCustomerId(key);
        return key;
    }
    
	/*
	 * public Optional<CustomerEntity> findCustomerById(Long customerId) { return
	 * queryForObject(new CustomerRowMapper(),
	 * "SELECT * FROM CUSTOMER WHERE CUSTOMER_ID = ?", customerId); }
	 */

	/*
	 * public Optional<CustomerEntity> findCustomerEntityByIdForUpdate(Long
	 * customerId) { return queryForObject(new CustomerRowMapper(),
	 * SELECT_CUSTOMER_BY_ID_UPDATE, customerId); }
	 */

 
    public Optional<CustomerEntity> findCustomerEntityByIdForUpdate(Long customerId,String lineOfBusiness) {
    	String stmt = "SELECT * FROM CRPROFL.CUSTOMER WHERE CUSTOMER_ID = ? AND line_of_business = ? FOR UPDATE";
     	Optional<List<CustomerEntity>> customerEntityList = query(new CustomerRowMapper(), stmt, customerId, lineOfBusiness);
    	
     	CustomerEntity latestCustomer = customerEntityList.orElse(Collections.emptyList())
    	    .stream()
    	    .max(Comparator.comparing(CustomerEntity::getCreatedTs))
    	    .orElse(null);
    	Optional<CustomerEntity> latestOptionalCustomer = Optional.ofNullable(latestCustomer);
    	
    	return latestOptionalCustomer;
    }    
    
    public Optional<List<CustomerEntity>> xx(String creditProfileId) {    	
    	String sqlSmt =    	
		    " SELECT CUSTOMER.* FROM crprofl.CUSTOMER CUSTOMER  "+
		    " INNER JOIN crprofl.CUSTOMER_CREDIT_PROFILE_REL CUSTOMER_CREDIT_PROFILE_REL   "+
		    " ON CUSTOMER.credit_profile_customer_id=CUSTOMER_CREDIT_PROFILE_REL.credit_profile_customer_id    "+
		    " WHERE CUSTOMER_CREDIT_PROFILE_REL.CREDIT_PROFILE_ID = ? " 
		    ; 
    	
    	return query(new CustomerRowMapper(), sqlSmt, UUID.fromString(creditProfileId) );      

    }    
    /**
     * Update entity
     *
     * @param customerUid UID string of the customer
     * @param entity
     * @return number of rows updated
     */
    public int update(String customerUid, CustomerEntity entity) {
        if (StringUtils.isBlank(customerUid) || entity == null) {
            return 0;
        }
        DaoHelper.removeNullAttributes(entity.getUpdateMap());
        Map<String, Object> updateMap = entity.getUpdateMap();
    	UUID partyIdUuid = null;
		if(entity.getPartyId()!=null ) {
			partyIdUuid=UUID.fromString(entity.getPartyId());
			updateMap.replace(party_id.name(), partyIdUuid);
		}
        SimpleUpdate update = updateWithVersioning(updateMap, UUID.fromString(customerUid), credit_profile_customer_id.name(), version.name());
        return update != null ? update.set(updated_ts.name(), NOW).execute(CustomerOperation.UPDATE_PREFIX, this) : 0;
    }
    


    public Optional<List<CustomerEntity>> get_Customer_CustomerCreditProfileRel_ByCreditProfileId(String creditProfileId) {    	
    	String sqlSmt =    	
		    " SELECT CUSTOMER.* FROM crprofl.CUSTOMER CUSTOMER  "+
		    " INNER JOIN crprofl.CUSTOMER_CREDIT_PROFILE_REL CUSTOMER_CREDIT_PROFILE_REL   "+
		    " ON CUSTOMER.credit_profile_customer_id=CUSTOMER_CREDIT_PROFILE_REL.credit_profile_customer_id    "+
		    " WHERE CUSTOMER_CREDIT_PROFILE_REL.CREDIT_PROFILE_ID = ? " 
		    ; 
    	
    	return query(new CustomerRowMapper(), sqlSmt, UUID.fromString(creditProfileId) );      

    }     
}
