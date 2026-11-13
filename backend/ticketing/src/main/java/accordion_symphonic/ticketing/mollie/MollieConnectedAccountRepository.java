package accordion_symphonic.ticketing.mollie;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MollieConnectedAccountRepository
        extends JpaRepository<MollieConnectedAccount, Long> {

    Optional<MollieConnectedAccount> findByOrganizationId(
            String organizationId
    );
}