package exate.gator.interceptor.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Custom annotations used for marking ISO3166 Alpha-2 country codes for validating with hibernate validator. */
public class CountryCode {
    private static final String DEFAULT_MESSAGE = "country code is not iso3166 alpha-2 type";
    private CountryCode() {}

    /** Annotation for String type country codes, used for the country-code property. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Constraint(validatedBy = CountryCodeValidators.StringValidator.class)
    public @interface CountryCodeString {
        String message () default DEFAULT_MESSAGE;

        Class<? extends Payload>[] payload() default {};

        Class<?>[] groups() default {};
    }

    /** Annotation for Optional-String country codes, used for the data-owning-country-code property. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Constraint(validatedBy = CountryCodeValidators.OptionalValidator.class)
    public @interface CountryCodeOptional {
        String message() default DEFAULT_MESSAGE;

        Class<? extends Payload>[] payload() default {};

        Class<?>[] groups() default {};
    }
}
