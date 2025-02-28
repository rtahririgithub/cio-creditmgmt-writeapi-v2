package com.telus.credit.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = AttachmentSizeValidation.class)
@Target( { ElementType.METHOD, ElementType.FIELD, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAttachmentSize {

    String message() default "1111";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
