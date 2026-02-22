package com.hrm.service;

/**
 * Generic wrapper cho kết quả trả về từ Service layer.
 *
 * TẠI SAO CẦN CLASS NÀY?
 *
 * Khi GUI gọi service.checkIn(), có 2 tình huống:
 *   1. Thành công → cần trả về ChamCong object + thông báo "Check-in thành công"
 *   2. Thất bại  → cần trả về thông báo lỗi "Bạn đã check-in rồi hôm nay"
 *
 * NẾU KHÔNG CÓ ServiceResult, bạn phải:
 *   - Trả về null khi lỗi → GUI không biết LÝ DO lỗi
 *   - Throw exception → quá nặng nề cho validation thông thường
 *   - Dùng Map → không type-safe, khó đọc
 *
 * ServiceResult giải quyết tất cả:
 *   ServiceResult<ChamCong> result = service.checkIn(nvId);
 *   if (result.isSuccess()) {
 *       ChamCong data = result.getData();  // Type-safe!
 *       showSuccess(result.getMessage());
 *   } else {
 *       showError(result.getMessage());     // Lý do lỗi rõ ràng
 *   }
 *
 * PATTERN NÀY GỌI LÀ: Result/Either pattern
 * Rất phổ biến trong enterprise Java, tương tự:
 *   - Rust: Result<T, E>
 *   - Kotlin: Result<T>
 *   - TypeScript: { success: boolean, data?: T, error?: string }
 *
 * GENERICS <T> là gì?
 * T là kiểu dữ liệu "tùy ý" — sẽ được xác định khi sử dụng:
 *   ServiceResult<ChamCong>       → T = ChamCong
 *   ServiceResult<DangKyLamThem>  → T = DangKyLamThem
 *   ServiceResult<Void>           → T = Void (không cần data)
 *
 * @param <T> Kiểu dữ liệu của data trả về khi thành công
 */
public class ServiceResult<T> {
    private final boolean success;
    private final String message;
    private final T data;

    private ServiceResult(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    /**
     * Tạo kết quả THÀNH CÔNG.
     * @param data    Object kết quả (vd: ChamCong vừa tạo)
     * @param message Thông báo hiển thị cho người dùng
     */
    public static <T> ServiceResult<T> success(T data, String message) {
        return new ServiceResult<>(true, message, data);
    }

    /**
     * Tạo kết quả THẤT BẠI.
     * @param message Lý do thất bại (vd: "Bạn đã check-in rồi hôm nay")
     */
    public static <T> ServiceResult<T> error(String message) {
        return new ServiceResult<>(false, message, null);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}