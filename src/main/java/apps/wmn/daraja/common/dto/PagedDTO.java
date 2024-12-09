package apps.wmn.daraja.common.dto;

import com.fortuneconnectltd.com.notifications.common.util.PageLinks;
import com.fortuneconnectltd.com.notifications.common.util.PageMetadata;
import java.util.List;
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