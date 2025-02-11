package com.telus.credit.model.mapper;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

import com.telus.credit.dao.entity.ProdQualEntity;
import com.telus.credit.model.ProductCategoryQualification;
import com.telus.credit.model.TelusChannel;

public class ProdQualModelMapper {
    public static List<ProdQualEntity> toEntity(String creditProfileId, List<ProductCategoryQualification> qualificationList, TelusChannel auditCharacteristic) {
        if (CollectionUtils.isEmpty(qualificationList)) {
            return new LinkedList<>();
        }
        Timestamp nowts = Timestamp.from(Instant.now());
        return qualificationList.stream().map(
                productCategoryQualification -> new ProdQualEntity()
                        .productQualInd(Boolean.valueOf(productCategoryQualification.getQualified()))
                        .creditApprvdProdCatgyCd(productCategoryQualification.getCategoryCd())
                        .createdBy(auditCharacteristic.getUserId()).updatedBy(auditCharacteristic.getUserId())
                        .createdOnTs(nowts)
                        .updatedBy(auditCharacteristic.getUserId()).updatedBy(auditCharacteristic.getUserId())
                        .updatedOnTs(nowts)
                        .originatorAppId(auditCharacteristic.getOriginatorAppId())
                        .creditProfileId(creditProfileId)
                        .validStartTs(nowts)
                        .validEndTs(null)
        ).collect(Collectors.toList());

    }

    public static ProdQualEntity toEntity(String creditProfileId, ProductCategoryQualification productCategoryQualification, TelusChannel auditCharacteristic) {
        if (Objects.isNull(productCategoryQualification)) {
            return null;
        }
        return new ProdQualEntity()
                .productQualInd(Boolean.valueOf(productCategoryQualification.getQualified()))
                .creditApprvdProdCatgyCd(productCategoryQualification.getCategoryCd())
                .createdBy(auditCharacteristic.getUserId()).updatedBy(auditCharacteristic.getUserId())
                .originatorAppId(auditCharacteristic.getOriginatorAppId())
                .creditProfileId(creditProfileId);
    }

    public static ProductCategoryQualification toDto(ProdQualEntity prodQualEntity) {
        if (Objects.isNull(prodQualEntity)) {
            return null;
        }
        ProductCategoryQualification productCategoryQualification = new ProductCategoryQualification();
        productCategoryQualification.setCategoryCd(prodQualEntity.getCreditApprvdProdCatgyCd());
        productCategoryQualification.setProductQualId(prodQualEntity.getProductQualId());
        productCategoryQualification.setQualified(prodQualEntity.getProductQualInd());
        return productCategoryQualification;
    }

}
