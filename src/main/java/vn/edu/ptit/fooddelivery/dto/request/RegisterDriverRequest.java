package vn.edu.ptit.fooddelivery.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterDriverRequest(
        @NotBlank(message = "Họ và tên không được để trống")
        @Size(max = 150)
        String fullName,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        String email,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự")
        String password,

        @NotBlank(message = "Số điện thoại không được để trống")
        @Size(max = 20)
        String phoneNumber,

        @NotBlank(message = "Biển số xe không được để trống")
        @Size(max = 20)
        String licensePlate
) {}
