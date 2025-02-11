package com.telus.credit.validation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.telus.credit.model.OrganizationIdentification;
import com.telus.credit.model.TelusIndividualIdentification;

public class UniqueIdentificationValidation implements ConstraintValidator<UniqueIdentification, Collection<?>> {

    @Override
    public void initialize(UniqueIdentification contactNumber) {
        // no need
    }

    @Override
    public boolean isValid(Collection<?> value, ConstraintValidatorContext cxt) {
        if (CollectionUtils.isEmpty(value)) {
            return true;
        }

        Map<String, Integer> dups = new HashMap<>();
        value.forEach(v -> {
            if (v instanceof TelusIndividualIdentification) {
                TelusIndividualIdentification identification = (TelusIndividualIdentification) v;
                if( StringUtils.isBlank(StringUtils.trimToEmpty(identification.getIdentificationType()))) {
                    return;
                }

                dups.computeIfAbsent(identification.getIdentificationType(), k -> 0);
                dups.computeIfPresent(identification.getIdentificationType(), (k, c) -> c += 1);
            } else if (v instanceof OrganizationIdentification) {
                OrganizationIdentification identification = (OrganizationIdentification) v;
                if( StringUtils.isBlank(StringUtils.trimToEmpty(identification.getIdentificationType()))) {
                    return;
                }

                dups.computeIfAbsent(identification.getIdentificationType(), k -> 0);
                dups.computeIfPresent(identification.getIdentificationType(), (k, c) -> c += 1);
            }
        });

        for (Map.Entry<String, Integer> dup : dups.entrySet()) {
            if (dup.getValue() > 1) {
                return false;
            }
        }

        return true;
    }

}