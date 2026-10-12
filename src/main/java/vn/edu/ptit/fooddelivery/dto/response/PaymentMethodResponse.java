package vn.edu.ptit.fooddelivery.dto.response;

import vn.edu.ptit.fooddelivery.entity.PaymentMethod;

public record PaymentMethodResponse(
        Long methodId,
        String methodCode,
        String methodName
) {
    public static PaymentMethodResponse from(PaymentMethod pm) {
        return new PaymentMethodResponse(pm.getMethodId(), pm.getMethodCode(), pm.getMethodName());
    }
}
