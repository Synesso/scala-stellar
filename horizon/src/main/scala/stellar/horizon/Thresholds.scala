package stellar.horizon

/**
 * The thresholds for operations on an account.
 *
 * @param low The weight required for a valid transaction including the Allow Trust and Bump Sequence operations.
 * @param med The weight required for a valid transaction including the Create Account, Payment, Path Payment, Manage
 *            Buy Offer, Manage Sell Offer, Create Passive Sell Offer, Change Trust, Inflation, and Manage Data operations.
 * @param high The weight required for a valid transaction including the Account Merge and Set Options operations.
 */
case class Thresholds(low: Int, med: Int, high: Int)
