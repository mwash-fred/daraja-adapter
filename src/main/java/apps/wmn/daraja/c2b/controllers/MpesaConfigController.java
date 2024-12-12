package apps.wmn.daraja.c2b.controllers;

import apps.wmn.daraja.c2b.dto.CreateMpesaConfigRequest;
import apps.wmn.daraja.c2b.dto.MpesaConfigResponse;
import apps.wmn.daraja.c2b.entity.MpesaConfig;
import apps.wmn.daraja.c2b.enums.MpesaEnvironment;
import apps.wmn.daraja.c2b.service.MpesaConfigService;
import apps.wmn.daraja.common.dto.PagedDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/daraja/config")
@Tag(name = "Mpesa Configuration", description = "APIs for managing Mpesa integration configurations")
@RequiredArgsConstructor
public class MpesaConfigController {
    private final MpesaConfigService configService;

    @Operation(
            summary = "Get Mpesa configuration",
            description = "Retrieves the active Mpesa configuration for a specific shortcode and environment"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Configuration found",
                    content = @Content(schema = @Schema(implementation = apps.wmn.daraja.common.dto.ApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No active configuration found for the specified shortcode and environment"
            )
    })
    @GetMapping("/{shortcode}")
    public ResponseEntity<apps.wmn.daraja.common.dto.ApiResponse<MpesaConfigResponse>> getConfig(
            @Parameter(description = "Mpesa shortcode", required = true)
            @PathVariable String shortcode,

            @Parameter(description = "Mpesa environment (SANDBOX/PRODUCTION)", required = true)
            @RequestParam MpesaEnvironment environment) {

        MpesaConfig config = configService.getConfig(shortcode, environment);
        return ResponseEntity.ok(apps.wmn.daraja.common.dto.ApiResponse.success(
                MpesaConfigResponse.from(config),
                "Configuration retrieved successfully"
        ));
    }

    @Operation(
            summary = "Create new Mpesa configuration",
            description = "Creates a new Mpesa configuration with encrypted credentials"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Configuration created successfully",
                    content = @Content(schema = @Schema(implementation = apps.wmn.daraja.common.dto.ApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Configuration already exists for the shortcode and environment"
            )
    })
    @PostMapping
    public ResponseEntity<apps.wmn.daraja.common.dto.ApiResponse<MpesaConfigResponse>> createConfig(
            @Parameter(description = "Mpesa configuration details", required = true)
            @Valid @RequestBody CreateMpesaConfigRequest request) {

        MpesaConfig config = configService.createConfig(request.toEntity());
        return new ResponseEntity<>(
                apps.wmn.daraja.common.dto.ApiResponse.success(
                        MpesaConfigResponse.from(config),
                        "Configuration created successfully"
                ),
                HttpStatus.CREATED
        );
    }

    @Operation(
            summary = "Update Mpesa configuration",
            description = "Updates an existing Mpesa configuration, re-encrypting any changed sensitive data"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Configuration updated successfully",
                    content = @Content(schema = @Schema(implementation = apps.wmn.daraja.common.dto.ApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Configuration not found"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<apps.wmn.daraja.common.dto.ApiResponse<MpesaConfigResponse>> updateConfig(
            @Parameter(description = "Configuration UUID", required = true)
            @PathVariable UUID id,

            @Parameter(description = "Updated configuration details", required = true)
            @Valid @RequestBody CreateMpesaConfigRequest request) {

        MpesaConfig config = configService.updateConfig(id, request.toEntity());
        return ResponseEntity.ok(apps.wmn.daraja.common.dto.ApiResponse.success(
                MpesaConfigResponse.from(config),
                "Configuration updated successfully"
        ));
    }

    @Operation(
            summary = "Deactivate Mpesa configuration",
            description = "Deactivates an existing Mpesa configuration"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Configuration deactivated successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Configuration not found"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<apps.wmn.daraja.common.dto.ApiResponse<Void>> deactivateConfig(
            @Parameter(description = "Configuration UUID", required = true)
            @PathVariable UUID id) {

        configService.deactivateConfig(id);
        return ResponseEntity.ok(apps.wmn.daraja.common.dto.ApiResponse.success(
                null,
                "Configuration deactivated successfully"
        ));
    }

    @Operation(
            summary = "List active Mpesa configurations",
            description = "Retrieves all active Mpesa configurations with pagination"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of active configurations",
            content = @Content(schema = @Schema(implementation = PagedDTO.class))
    )
    @GetMapping
    public ResponseEntity<apps.wmn.daraja.common.dto.ApiResponse<PagedDTO<MpesaConfigResponse>>> listActiveConfigs(
            @Parameter(description = "Pagination parameters")
            Pageable pageable,
            HttpServletRequest request) {

        Page<MpesaConfig> configPage = configService.listActiveConfigs(pageable);
        String baseUrl = request.getRequestURL().toString();

        PagedDTO<MpesaConfigResponse> pagedResponse = PagedDTO.from(
                configPage.map(MpesaConfigResponse::from),
                baseUrl
        );

        return ResponseEntity.ok(apps.wmn.daraja.common.dto.ApiResponse.success(
                pagedResponse,
                "Configurations retrieved successfully"
        ));
    }
}
