package apps.wmn.daraja.c2b.controllers;

import apps.wmn.daraja.c2b.dto.*;
import apps.wmn.daraja.c2b.enums.MpesaEnvironment;
import apps.wmn.daraja.c2b.service.MpesaPaymentService;
import apps.wmn.daraja.common.dto.ApiResponse;
import apps.wmn.daraja.common.dto.PagedDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/daraja/payments")
@Tag(name = "Mpesa Payments", description = "APIs for managing Mpesa payments and transactions")
@RequiredArgsConstructor
public class MpesaPaymentController {

  private final MpesaPaymentService paymentService;

  @Operation(
      summary = "Initiate STK Push payment",
      description = "Initiates an M-Pesa STK Push payment request to the customer's phone")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "STK Push initiated successfully",
        content = @Content(schema = @Schema(implementation = StkPushResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "Invalid request parameters"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "Error initiating STK Push")
  })
  @PostMapping("/stk/push")
  public ResponseEntity<ApiResponse<StkPushResponse>> initiateSTKPush(
      @Valid @RequestBody StkPushRequest request) {
    log.info("Received STK push request for phone: {}", request.phoneNumber());
    return ResponseEntity.ok(
        ApiResponse.success(
            paymentService.initiateSTKPush(request, MpesaEnvironment.valueOf(request.environment())), "STK Push initiated successfully"));
  }

  @Operation(
      summary = "Process STK Push callback",
      description = "Processes the callback received from M-Pesa after STK Push completion")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "Callback processed successfully",
        content = @Content(schema = @Schema(implementation = PaymentView.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "Invalid callback data")
  })
  @PostMapping("/stk/callback")
  public ResponseEntity<ApiResponse<PaymentView>> processStkCallback(
      @Valid @RequestBody StkCallback callback) {
    log.info("Received STK callback for request ID: {}", callback.merchantRequestId());
    return ResponseEntity.ok(
        ApiResponse.success(
            paymentService.processStkCallback(callback), "STK callback processed successfully"));
  }

  @Operation(
      summary = "Process C2B callback",
      description = "Processes the callback received from M-Pesa for C2B transactions")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "C2B callback processed successfully",
        content = @Content(schema = @Schema(implementation = PaymentView.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "Invalid callback data")
  })
  @PostMapping("/c2b/callback")
  public ResponseEntity<ApiResponse<PaymentView>> processC2BCallback(
      @Valid @RequestBody C2bCallback callback) {
    log.info("Received C2B callback for transaction: {}", callback.transId());
    return ResponseEntity.ok(
        ApiResponse.success(
            paymentService.processC2BCallback(callback), "C2B callback processed successfully"));
  }

  @Operation(
      summary = "Get payment by ID",
      description = "Retrieves payment details using payment UUID")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "Payment found",
        content = @Content(schema = @Schema(implementation = PaymentView.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "Payment not found")
  })
  @GetMapping("/{paymentId}")
  public ResponseEntity<ApiResponse<PaymentView>> getPayment(
      @Parameter(description = "Payment UUID", required = true) @PathVariable UUID paymentId) {
    return ResponseEntity.ok(
        ApiResponse.success(
            paymentService.getPayment(paymentId), "Payment retrieved successfully"));
  }

  @Operation(
      summary = "Get payment by transaction ID",
      description = "Retrieves payment details using M-Pesa transaction ID")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "Payment found",
        content = @Content(schema = @Schema(implementation = PaymentView.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "Payment not found")
  })
  @GetMapping("/transaction/{transactionId}")
  public ResponseEntity<ApiResponse<PaymentView>> getPaymentByTransactionId(
      @Parameter(description = "M-Pesa transaction ID", required = true) @PathVariable
          String transactionId) {
    return ResponseEntity.ok(
        ApiResponse.success(
            paymentService.getPaymentByTransactionId(transactionId),
            "Payment retrieved successfully"));
  }

  @Operation(
      summary = "Search payments",
      description = "Search payments with filters and pagination")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "200",
      description = "Search results retrieved",
      content = @Content(schema = @Schema(implementation = PagedDTO.class)))
  @GetMapping("/search")
  public ResponseEntity<ApiResponse<PagedDTO<PaymentView>>> searchPayments(
      @Parameter(description = "Customer phone number") @RequestParam(required = false)
          String phoneNumber,
      @Parameter(description = "Transaction status") @RequestParam(required = false) String status,
      @Parameter(description = "Start date (yyyy-MM-dd'T'HH:mm:ss)")
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime startDate,
      @Parameter(description = "End date (yyyy-MM-dd'T'HH:mm:ss)")
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime endDate,
      Pageable pageable,
      HttpServletRequest request) {

    Page<PaymentView> page =
        paymentService.getPayments(phoneNumber, status, startDate, endDate, pageable);

    return ResponseEntity.ok(
        ApiResponse.success(
            PagedDTO.from(page, request.getRequestURL().toString()),
            "Payments retrieved successfully"));
  }

  @Operation(
      summary = "Get payments by phone number",
      description = "Retrieves all payments for a specific phone number")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "200",
      description = "Payments retrieved successfully",
      content = @Content(schema = @Schema(implementation = PaymentView.class)))
  @GetMapping("/phone/{phoneNumber}")
  public ResponseEntity<ApiResponse<List<PaymentView>>> getPaymentsByPhoneNumber(
      @Parameter(description = "Customer phone number", required = true) @PathVariable
          String phoneNumber) {
    return ResponseEntity.ok(
        ApiResponse.success(
            paymentService.getPaymentsByPhoneNumber(phoneNumber),
            "Payments retrieved successfully"));
  }

  @Operation(
      summary = "Validate payment status",
      description = "Validates the current status of a payment with M-Pesa")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "Payment status validated",
        content = @Content(schema = @Schema(implementation = PaymentView.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "Payment not found")
  })
  @GetMapping("/validate/{transactionId}")
  public ResponseEntity<ApiResponse<PaymentView>> validatePaymentStatus(
      @Parameter(description = "M-Pesa transaction ID", required = true) @PathVariable
          String transactionId) {
    return ResponseEntity.ok(
        ApiResponse.success(
            paymentService.validatePaymentStatus(transactionId),
            "Payment status validated successfully"));
  }
}
