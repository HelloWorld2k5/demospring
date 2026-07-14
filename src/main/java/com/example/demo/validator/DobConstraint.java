package com.example.demo.validator;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

// Custom annotation dùng để validate data
@Target({FIELD}) // custom annotation này được dùng ở đâu, trong TH này ta dùng ở fields
@Retention(RUNTIME) // chạy ở cấp độ runtime
@Constraint(validatedBy = {DobValidator.class}) // lớp chịu trách nhiệm logic cho annotation này
public @interface DobConstraint {

    String message() default "Invalid date of birth!";

    int min();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
