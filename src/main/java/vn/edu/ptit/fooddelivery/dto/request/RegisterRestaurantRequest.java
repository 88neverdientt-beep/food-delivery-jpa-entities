package vn.edu.ptit.fooddelivery.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRestaurantRequest(
        @NotBlank(message = "Tên nhà hàng không được để trống")
        @Size(max = 150)
        String restaurantName,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        String email,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự")
        String password,

        @NotBlank(message = "Số điện thoại không được để trống")
        @Size(max = 20)
        String phoneNumber,

        @NotBlank(message = "Địa chỉ không được để trống")
        @Size(max = 255)
        String address,

        @NotNull(message = "Vĩ độ không được để trống")
        Double latitude,

        @NotNull(message = "Kinh độ không được để trống")
        Double longitude
) {}
