# Stellar for Scala / Protocol

[![codecov](https://codecov.io/gh/Synesso/scala-stellar/branch/master/graph/badge.svg)](https://codecov.io/gh/Synesso/scala-stellar)

This is the project the encodes the primary data types for transactions within the Stellar network using Scala.

You would use this library if you wished parse and generate Stellar transaction XDR without necessarily needing to
interact with Horizon.

Unless you know you definitely want this project, you are probably looking for the [Horizon](../horizon/) project instead.
It is built upon this project and provides additional conveniences for interacting with the Stellar network.

## Useful values

### Seed

The seed is the private key for accounts in the Stellar network, based upon [Curve 25519](https://en.wikipedia.org/wiki/Curve25519)
and encoded into Stellar Key form.

```scala
import stellar.protocol._

// A random seed
val seed = Seed.random

// A specific seed
val seed = Seed("SCVBL5XE4NUIUHGEXL33FFNYWITQXJ63MCK2KBDMM24UVI3LSGCEKXYA")
```

### Account Id

The AccountId is the public-facing dual of the Seed and is how accounts are addressed in Stellar.

```scala
// A specific account id
val accountId = AccountId("GAF364EFVYYKK75ICJZL57HVG37PFCVRNWYRWPLXGJ2MYT622EQJ3RRR")

// A random account id
val accountId = AccountId.random

// The public account id for a Seed 
val accountId = Seed.random.accountId
```
