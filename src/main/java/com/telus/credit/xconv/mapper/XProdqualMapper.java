package com.telus.credit.xconv.mapper;

import org.springframework.stereotype.Component;

import com.telus.credit.dao.entity.ProdQualEntity;
import com.telus.credit.dao.entity.XProdqualEntity;
import com.telus.credit.model.ProductCategoryQualification;

@Component
public class XProdqualMapper {


    public static ProdQualEntity toProdQualEntity(XProdqualEntity xProdqualEntity) {
        ProdQualEntity prodQualEntity = new ProdQualEntity();
        prodQualEntity
        		.productQualLegacyId(xProdqualEntity.getProductQualLegacyId())
        		.creditApprvdProdCatgyCd(xProdqualEntity.getCreditApprvdProdCatgyCd())
        		.creditProfileId(xProdqualEntity.getCreditProfileId())
        		.productQualInd(xProdqualEntity.getProductQualInd())
        		.validStartTs(xProdqualEntity.getEffStartTs())
        		.validEndTs(xProdqualEntity.getEffStopTs())
        		.originatorAppId(xProdqualEntity.getDataSrcId())
        		
        		.createdBy(xProdqualEntity.getCreateUserId())
        		.createdOnTs(xProdqualEntity.getCreateTs())
        		.updatedBy(xProdqualEntity.getCreateUserId())
        		.updatedOnTs(xProdqualEntity.getCreateTs())
        		;
        return prodQualEntity;
    }
    
    
    public static ProductCategoryQualification toProdQual(XProdqualEntity xProdqualEntity) {
    	ProductCategoryQualification prodQualEntity = new ProductCategoryQualification();
    	prodQualEntity.setCategoryCd(xProdqualEntity.getCreditApprvdProdCatgyCd());
    	prodQualEntity.setQualified(xProdqualEntity.getProductQualInd());
        return prodQualEntity;
    }    
}
