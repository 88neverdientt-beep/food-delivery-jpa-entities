package vn.edu.ptit.fooddelivery.dto.response;

/** Thông tin đăng nhập trả về client (không có token thực - auth đơn giản hoá). */
public record AuthResponse(
        Long id,
        String name,
        String email,
        /** "CUSTOMER", "RESTAURANT", hoặc "DRIVER" */
        String role
) {}
