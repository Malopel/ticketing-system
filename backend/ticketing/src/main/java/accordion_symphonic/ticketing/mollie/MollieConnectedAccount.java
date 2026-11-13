package accordion_symphonic.ticketing.mollie;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "mollie_connected_account",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_mollie_connected_account_organization_id",
                        columnNames = "organization_id"
                )
        }
)
public class MollieConnectedAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "organization_id",
            nullable = false
    )
    private String organizationId;

    @Column(
            name = "refresh_token",
            nullable = false,
            columnDefinition = "text"
    )
    private String refreshToken;

    @Column(
            name = "access_token_expires_at",
            nullable = false
    )
    private LocalDateTime accessTokenExpiresAt;

    protected MollieConnectedAccount() {
    }

    public MollieConnectedAccount(
            String organizationId,
            String refreshToken,
            LocalDateTime accessTokenExpiresAt
    ) {
        this.organizationId = organizationId;
        this.refreshToken = refreshToken;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
    }

    public void updateCredentials(
            String refreshToken,
            LocalDateTime accessTokenExpiresAt
    ) {
        this.refreshToken = refreshToken;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
    }
}