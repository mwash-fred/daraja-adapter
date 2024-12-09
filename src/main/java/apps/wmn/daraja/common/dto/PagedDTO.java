package apps.wmn.daraja.common.dto;

import java.util.List;

import apps.wmn.daraja.common.util.PageLinks;
import apps.wmn.daraja.common.util.PageMetadata;
import org.springframework.data.domain.Page;

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