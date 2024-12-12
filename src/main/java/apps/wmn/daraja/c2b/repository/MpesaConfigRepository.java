package apps.wmn.daraja.c2b.repository;

import apps.wmn.daraja.c2b.entity.MpesaConfig;
import apps.wmn.daraja.c2b.enums.MpesaEnvironment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MpesaConfigRepository
    extends JpaRepository<MpesaConfig, Long>, JpaSpecificationExecutor<MpesaConfig> {
  Optional<MpesaConfig> findByShortcodeAndEnvironmentAndActiveTrue(
      String shortcode, MpesaEnvironment environment);

  Page<MpesaConfig> findByActiveTrue(Pageable pageable);

  List<MpesaConfig> findByEnvironmentAndActiveTrue(MpesaEnvironment environment);

  boolean existsByShortcodeAndEnvironment(String shortcode, MpesaEnvironment environment);

  Optional<MpesaConfig> findByUuid(UUID uuid);
}
