package com.example.demo.validator;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

// Đây là class chịu trách nhiệm logic cho custom annotation
// Implement ConstraintValidator chứa 2 tham số: tên custome annotation và kiểu dữ liệu mà fields được gắn annotation
public class DobValidator implements ConstraintValidator<DobConstraint, LocalDate> {

    private int min; // field chứa giá trị tử tham số được truyền vào đây

    // hàm này check field được gắn annotation chuẩn valid không
    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {

        if (Objects.isNull(value)) return true;

        // tính số năm từ ngày truyền vào cho tới ngày hiện tại (là tính tuổi user)
        long years = ChronoUnit.YEARS.between(value, LocalDate.now(ZoneId.systemDefault()));

        return years >= min; // tuổi không dưới 18
    }

    // Hàm giúp lấy được những tham số được truyền vào (ví dụ @DobConstraint(min = 18) -> ta có thể get được 18)
    // Hàm này luôn chạy trước hàm isValid
    @Override
    public void initialize(DobConstraint constraintAnnotation) {

        ConstraintValidator.super.initialize(constraintAnnotation);

        min = constraintAnnotation.min(); // lấy min từ ngoài gán vào field min để hàm isValid dùng
    }
}
