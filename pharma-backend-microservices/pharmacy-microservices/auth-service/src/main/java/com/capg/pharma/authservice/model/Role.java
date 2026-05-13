package com.capg.pharma.authservice.model;

/**
 * Enumeration of user roles in the pharmacy platform.
 *
 * <p>Roles are stored in the {@code user_roles} table and embedded in JWT tokens.
 * Downstream services use these roles for method-level authorization via
 * {@code @PreAuthorize("hasRole('ADMIN')")}.</p>
 */
public enum Role {

    /** Standard customer — can browse medicines, upload prescriptions, place orders. */
    CUSTOMER,

    /** Administrator — full access including inventory, prescription approval, dashboard. */
    ADMIN
}
