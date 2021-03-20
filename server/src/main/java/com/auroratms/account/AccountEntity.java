package com.auroratms.account;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.vault.repository.mapping.Secret;


/**
 * Represents Stripe Connect account associated with
 */
@Data
@NoArgsConstructor
@Secret
public class AccountEntity {

    // profile id of user who created this stripe account
    @Id
    private String profileId;

    // id of a Stripe account
    private String accountId;

    // status of Connect activation
    private boolean activated;

    private String accountPublicKey;

    private String accountSecretKey;
}
