package stellar.horizon

case class TransactionResponse(
  hash: String,
  ledger: Long,
  envelopeXdr: String,
  resultXdr: String,
  resultMetaXdr: String
)
