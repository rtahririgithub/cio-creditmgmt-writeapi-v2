package com.telus.credit.dao;

import static com.telus.credit.dao.entity.CreditProfileEntity.Cols.credit_profile_id;
import static com.telus.credit.dao.entity.CreditProfileEntity.Cols.updated_ts;
import static com.telus.credit.dao.entity.CreditProfileEntity.Cols.version;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.telus.credit.common.CommonHelper;
import com.telus.credit.dao.entity.CreditProfileEntity;
import com.telus.credit.dao.operation.CreditProfileOperation;
import com.telus.credit.dao.rowmapper.CreditProfileRowMapper;

@Repository
public class CreditProfileDao extends AbstractDao {

    private static final String SELECT_CREDIT_PROFILE_JOIN_REL =
            " SELECT t1.* from CREDIT_PROFILE t1, CUSTOMER_CREDIT_PROFILE_REL t2 "
            + " WHERE t1.CREDIT_PROFILE_ID = t2.CREDIT_PROFILE_ID ";


    
    public CreditProfileDao(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * Insert entity into credit profile table.
     *
     * @param entity
     * @return primary key
     */
    public String insert(CreditProfileEntity entity) {
        KeyHolder keyHolder = CreditProfileOperation.insert(entity).execute(this);
        String key = keyHolder.getKeyList().get(0).get(credit_profile_id.name()).toString();
        entity.setCreditProfileId(key);
        return key;
    }

    /**
     * Update entity
     *
     * @param creditProfileId credit profile UID
     * @param entity
     * @return number of rows updated
     */
    public int update(String creditProfileId, CreditProfileEntity entity) {
        if (StringUtils.isBlank(creditProfileId) || entity == null) {
            return 0;
        }
       //null values in request represents no change
       DaoHelper.removeNullAttributes(entity.getUpdateMap());
       //clean up CreditProgram per creditprogram code 
       CommonHelper.refineCreditProgram(entity);
       SimpleUpdate update = updateWithVersioning(entity.getUpdateMap(), UUID.fromString(creditProfileId), credit_profile_id.name(), version.name());
       return update != null ? update.set(updated_ts.name(), NOW).execute(CreditProfileOperation.UPDATE_PREFIX, this) : 0;
    }


    /**
     * Get all credit profiles belonging to customerUid
     *
     * @param customerTblPk CUSTOMER_CREDIT_PROFILE_REL.CREDIT_PROFILE_CUSTOMER_ID
     * @return list of credit profiles
     */ 
    public List<CreditProfileEntity> getCreditProfilesByCustomerTblPk(String customerTblPk) {
    	
    	return getJdbcTemplate().query(
					SELECT_CREDIT_PROFILE_JOIN_REL
			        + " AND t2.CREDIT_PROFILE_CUSTOMER_ID = ?", new CreditProfileRowMapper(), UUID.fromString(customerTblPk)
			        );

    }


    public List<CreditProfileEntity> getCreditProfilesAndRelByCustomerTblPk(String customerTblPk) {
    //TODO return creditprofile and relationship
    String stmt =
                " SELECT t1.*,t2.* from CREDIT_PROFILE t1, CUSTOMER_CREDIT_PROFILE_REL t2 "
                + " WHERE t1.CREDIT_PROFILE_ID = t2.CREDIT_PROFILE_ID "
                + " AND t2.CREDIT_PROFILE_CUSTOMER_ID = ?";

    	return getJdbcTemplate().query(
    				stmt
			        , new CreditProfileRowMapper(), UUID.fromString(customerTblPk)
			        );		
		   	
    }
    
    /**
     * Get most recently updated credit profile for a customer
     *
     * @param customerUid
     * @return
     */
    public Optional<CreditProfileEntity> getLatestByCustomerUid(String customerUid) {
        return queryForObject(new CreditProfileRowMapper(), SELECT_CREDIT_PROFILE_JOIN_REL
                + " AND t2.CREDIT_PROFILE_CUSTOMER_ID = ? "
                + " ORDER BY t1.updated_ts desc limit 1", UUID.fromString(customerUid));
    }
    

    /**
     * Get all credit profiles belonging to customerUid
     *
     * @param customerUid
     * @return list of credit profiles
     */
    public List<CreditProfileEntity> getByCustomerUid(String customerUid) {
        return getJdbcTemplate().query(SELECT_CREDIT_PROFILE_JOIN_REL
                + " AND t2.CREDIT_PROFILE_CUSTOMER_ID = ?", new CreditProfileRowMapper(), UUID.fromString(customerUid));
    }
    
    /**
     * Get a credit profile by its uid and customerUid
     *
     * @param customerUid
     * @param profileId
     * @return
     */
    public Optional<CreditProfileEntity> getByCustomerUidAndProfileId(String customerUid, String profileId) {
        if (StringUtils.isBlank(profileId)) {
            return Optional.empty();
        }

        return queryForObject(new CreditProfileRowMapper(),SELECT_CREDIT_PROFILE_JOIN_REL
                        + " AND t2.CREDIT_PROFILE_CUSTOMER_ID = ? AND t2.CREDIT_PROFILE_ID= ?",
                UUID.fromString(customerUid), UUID.fromString(profileId));
    }

    /**
     * Remove credit profiles
     *
     * @param profileUids List of credit profile uids to remove
     * @return number of rows removed
     */
    public int deleteByProfileIds(Collection<String> profileUids) {
        if (profileUids == null || profileUids.isEmpty()) {
            return 0;
        }

        StringJoiner inJoiner = new StringJoiner(",", "(", ")");
        profileUids.forEach(uid -> inJoiner.add("?"));
        String sql = "DELETE from CREDIT_PROFILE WHERE CREDIT_PROFILE_ID IN " + inJoiner.toString();

        return getJdbcTemplate().update(sql, profileUids.stream().map(UUID::fromString).toArray());
    }
    
    /**
     * Get most recently updated credit profile a customer
     *
     * @param customerUid
     * @return
     */
    public Optional<CreditProfileEntity> getByCreditProfileUid(String cpUidStr) {
    	UUID cpUidUUID = new UUID(0, 0);
		try {
			cpUidUUID = UUID.fromString(cpUidStr);
		} catch (Exception e) {
			//invalid credit profile UUID 
			return Optional.empty();
		}
    	
        return queryForObject(
        		new CreditProfileRowMapper()
        		,"SELECT t1.* from CREDIT_PROFILE t1 WHERE t1.CREDIT_PROFILE_ID = ? " 
        		, cpUidUUID);
    }
    
    
/*
 * 
 * find customer Ids with matching cardID(any) , dob, Primary/Active(null)/no_valid_end_ts creditprofile , 
 *
 */  
public Optional<List<Long>> findCustomerIdsMatchingMergeCriteria(String inputDL, String inputSin, String inputCC, String inputPSP, String inputPRV, String inputHC,String inputDate) {
	
	String dl = (inputDL.isEmpty()? "" :  " (pi.id_type='DL'  and ich.value='"+inputDL+"' )  "+"\n"  ); 
	String sin =(inputSin.isEmpty()? "" : " (pi.id_type='SIN' and ich.value='"+inputSin+"' )  " +"\n" );
	String cc = (inputCC.isEmpty()? "" :  " (pi.id_type='CC'  and ich.value='"+inputCC+"' )  " +"\n" );
	String psp =(inputPSP.isEmpty()? "" : " (pi.id_type='PSP' and ich.value='"+inputPSP+"' )  " +"\n" );
	String prv =(inputPRV.isEmpty()? "" : " (pi.id_type='PRV' and ich.value='"+inputPRV+"' )  " +"\n" );
	String hc = (inputHC.isEmpty()? "" :  " (pi.id_type='HC'  and ich.value='"+inputHC+"' )  " +"\n" );
	String[] ids= {dl,sin,cc,psp,prv,hc};
	String idSqlStmt = "";
	for (int i = 0; i < ids.length; i++) {
		if ( ids[i]!=null && !ids[i].isEmpty()) {
			idSqlStmt= idSqlStmt + ids[i] + " or ";
		}		
	}
	idSqlStmt=idSqlStmt.trim();
	if(idSqlStmt.length()>=2) {
		idSqlStmt= idSqlStmt.substring(0, idSqlStmt.length() - 2);	
	}
	
	String hardmatchQuery=
	"select  \n"+
	 " distinct(cust.customer_id)  \n"+
	" from crprofl.customer cust  \n"+  
	" inner join crprofl.party_identification pi 		on pi.party_id=cust.party_id  \n"+ 
	" inner join crprofl.identification_char_hash ich 	on ich.identificaton_id  = pi.identificaton_id  " +	" and \n"+ 
  " ( "+ "\n"+
  
  	idSqlStmt +

  " ) " +	"\n"+
	" inner join crprofl.individual individual on individual.party_id  = cust.party_id and individual.birth_date =TO_DATE('" + inputDate+ "' ,'YYYY-MM-DD')   " + "\n"+
	" inner join crprofl.party party  on party.party_id  = cust.party_id and (party.status_cd is null or party.status_cd != 'C') "	+ "\n"+
	" inner join crprofl.customer_credit_profile_rel customer_credit_profile_rel	on customer_credit_profile_rel.credit_profile_customer_id  = cust.credit_profile_customer_id and customer_credit_profile_rel.customer_credit_profile_rel_cd='PRI' "	+ "\n"+
    " inner join crprofl.credit_profile credit_profile	on credit_profile.credit_profile_id  = customer_credit_profile_rel.credit_profile_id and credit_profile.valid_end_ts is null  and ( credit_profile.credit_profile_status_cd  = 'A'  or credit_profile.credit_profile_status_cd  is null  ) "	+ "\n"+
	" where cust.role <> 'Guarantor' --and cust.line_of_business='WIRELINE' " + "\n"; 

	Optional<List<Long>> rslt = query(new SingleColumnRowMapper<Long>(), hardmatchQuery );
	
	
    return rslt;
}


/*
 * get Today Changed Customers Candidate For Merging. having a change in :
 * CP (credit_class_ts/risk_level_ts/created_ts),ID.updated_ts,individual.updated_ts
 */
public Optional<List<Long>> getTodayChangedCustomersCandidateForMerging() {
	String query=	
	"select  \n"+
	"	distinct cust.customer_id \n"+
	"from crprofl.customer cust   \n"+
	"inner join crprofl.customer_credit_profile_rel customer_credit_profile_rel	on  customer_credit_profile_rel.credit_profile_customer_id  = cust.credit_profile_customer_id  and customer_credit_profile_rel.customer_credit_profile_rel_cd='PRI'  	\n"+
	"inner join crprofl.credit_profile credit_profile	on credit_profile.credit_profile_id  = customer_credit_profile_rel.credit_profile_id  and credit_profile.valid_end_ts is null   and ( credit_profile.credit_profile_status_cd  = 'A'  or credit_profile.credit_profile_status_cd  is null  )  \n"+
	"inner join crprofl.party_identification pi 		    on pi.party_id=cust.party_id \n"+
	"inner join crprofl.identification_char_hash ich 	on ich.identificaton_id  = pi.identificaton_id    \n"+
	"inner join crprofl.individual individual		 	on individual.party_id  = cust.party_id   \n"+
	"where  \n"+
	 
    "cust.line_of_business='WIRELINE' and \n"+	 
	"(\n"+
	"   (\n"+
	"		( credit_profile.credit_class_ts at time zone 'utc' <= NOW() and credit_profile.credit_class_ts at time zone 'utc' >= (NOW() - INTERVAL '1 DAY') ) \n"+
	"		or \n"+
	"		( credit_profile.risk_level_ts   at time zone 'utc' <= NOW() and credit_profile.risk_level_ts   at time zone 'utc' >= (NOW() - INTERVAL '1 DAY') ) \n"+
	"		or \n"+
	"		( credit_profile.created_ts      at time zone 'utc' <= NOW( )and credit_profile.created_ts      at time zone 'utc' >= (NOW() - INTERVAL '1 DAY') )		\n"+
	"	)\n"+
	"	or\n"+
	"	( ich.updated_ts at time zone 'utc' <= NOW()and  ich.updated_ts  at time zone 'utc' >= (NOW() - INTERVAL '1 DAY') )\n"+
	"	\n"+
	"	or \n"+
	"	( individual.updated_ts at time zone 'utc' <= NOW()and  individual.updated_ts at time zone 'utc'  >= (NOW() - INTERVAL '1 DAY') )\n"+
	")\n"
	;
	

	Optional<List<Long>> rslt = query(new SingleColumnRowMapper<Long>(), query );
	return rslt;
}





}