package apps.wmn.daraja.common.dto;

import java.util.List;

import apps.wmn.daraja.common.util.PageLinks;
import apps.wmn.daraja.common.util.PageMetadata;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;


@Schema(description = "Paged Response wrapper")
public record PagedDTO<T>(
        List<T> content,
        PageMetadata metadata,
        PageLinks links
) {
    public static <T> PagedDTO<T> from(Page<T> page, String baseUrl) {
        return new PagedDTO<>(
                page.getContent(),
                PageMetadata.from(page),
                PageLinks.from(page, baseUrl)
        );
    }
}