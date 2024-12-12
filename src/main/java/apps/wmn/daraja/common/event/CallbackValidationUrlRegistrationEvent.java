package apps.wmn.daraja.common.event;

import apps.wmn.daraja.c2b.enums.MpesaEnvironment;

public record CallbackValidationUrlRegistrationEvent(
        String shortcode, MpesaEnvironment environment, String confirmationUrl, String validationUrl) {}
