package exate.gator.interceptor.constraints;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Optional;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class CountryCodeValidators {
    private CountryCodeValidators() {}

    private static final Set<String> COUNTRY_CODES = Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2);

    public static class StringValidator implements ConstraintValidator<CountryCode.CountryCodeString, String> {
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return Objects.nonNull(value) && value.length() == 2 && COUNTRY_CODES.contains(value);
        }
    }

    public static class OptionalValidator implements ConstraintValidator<CountryCode.CountryCodeOptional, Optional<String>> {
        @Override
        public boolean isValid(Optional<String> value, ConstraintValidatorContext context) {
            return value.isEmpty() || (value.get().length() == 2 && COUNTRY_CODES.contains(value.get()));
        }
    }

}
