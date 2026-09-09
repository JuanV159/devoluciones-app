package cl.nxtara.devoluciones.api.validation;

import cl.nxtara.devoluciones.domain.validation.RutChilenoValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RutChilenoConstraintValidator implements ConstraintValidator<RutChileno, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // @NotBlank se encarga del vacío
        }
        return RutChilenoValidator.esValido(value);
    }
}
