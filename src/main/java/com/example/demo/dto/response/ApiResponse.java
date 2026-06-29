package com.example.demo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@JsonInclude(JsonInclude.Include.NON_NULL) // annotation này: những fields null, sẽ không hiện trong response
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ApiResponse<T> {

    @Builder.Default // nếu ko gán thì code sẽ bị builder gán lại bằng 0
    int code = 1000; // mặc định 1000 là success
    String message;
    T result;
}
