package stellar.horizon

/**
 * The state of authorization of an account.
 *
 * @param required  authorization is required
 * @param revocable authorization can be revoked
 * @param immutable the authorization state can never be changed
 */
case class AuthFlags(required: Boolean, revocable: Boolean, immutable: Boolean)
