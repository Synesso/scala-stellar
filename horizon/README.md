# Stellar for Scala / Horizon

[![codecov](https://codecov.io/gh/Synesso/scala-stellar/branch/master/graph/badge.svg)](https://codecov.io/gh/Synesso/scala-stellar)

This is the project for interacting with Horizon instances using pure Scala. Horizon is the public-facing interface
for the Stellar decentralized payments network.

You would use this library if you wish to query and transact on the Stellar network using Scala `Try` and `Future`.

## Getting Started

The simplest way to get started is to fork the [exemplar project](https://github.com/Synesso/scala-stellar-horizon-exemplar).

Alternatively, to add the library to an existing project, follow the [instructions on jitpack](https://jitpack.io/#synesso/scala-stellar/).

## Querying Stellar

All access to Stellar is via Horizon. The `Horizon` type can be configured for any Horizon instance. For convenience,
the SDF instances are pre-defined. If you have provisioned your own Horizon instance, you can configure this via URL.

```scala
import stellar.horizon._

// The SDF public Horizon instance, returning async (Future) values.
val sdfHorizonMainAsync: Horizon[Future] = Horizon.async()

// The SDF testnet Horizon instance, returning blocking (Try) values.
val sdfHorizonTestSync: Horizon[Try] = Horizon.sync(baseUrl = Horizon.Endpoints.Test)

// A custom Horizon instance running locally in a docker container (configured separately).
val localHorizon: Horizon[Future] = Horizon.async(baseUrl = HttpUrl.parse("http://localhost:8000/"))
```

### Operations

`Horizon` provides operations grouped into several themes:

* `horizon`
  * `account`
    * `detail` - given an account id, provides the public details of that account.
  * `meta`
    * `state` - current state and capabilities of the instance, including protocol & software versions, ledger
                  ids known and whether a testnet faucet (friendbot) is available.
