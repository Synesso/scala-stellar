# Stellar for Scala / Horizon

[![codecov](https://codecov.io/gh/Synesso/scala-stellar/branch/master/graph/badge.svg)](https://codecov.io/gh/Synesso/scala-stellar)

This is the project for interacting with Horizon instances using pure Scala. Horizon is the public-facing interface
for the Stellar decentralized payments network.

You would use this library if you wish to query and transact on the Stellar network using Scala `Try` and `Future`.

## Getting Started

The simplest way to get started is to fork the [exemplar project](https://github.com/Synesso/scala-stellar-horizon-exemplar).

Alternatively, to add the library to an existing project, follow the [instructions on jitpack](https://jitpack.io/#synesso/scala-stellar/).

## Accessing Horizon

All access to Stellar is via Horizon. The `Horizon` type can be configured for any Horizon instance. For convenience,
the SDF instances are pre-defined.

```scala
import stellar.horizon._

// The SDF public Horizon instance, returning async (Future) values.
val sdfHorizonMainAsync: Horizon[Future] = Horizon.async()

// The SDF testnet Horizon instance, returning blocking (Try) values.
val sdfHorizonTestSync: Horizon[Try] = Horizon.sync(baseUrl = Horizon.Networks.Test)
````

 If you have provisioned your own Horizon instance, you can configure this via URL.
```scala
// A custom Horizon instance running locally in a docker container (configured separately).
val localHorizon: Horizon[Future] = Horizon.async(baseUrl = HttpUrl.parse("http://localhost:8000/"))
```

For brevity, the remainder of this guide will assume a synchronous Horizon instance (`Horizon[Try]`).

### Creating a Test Account

Creating and funding a new account from nothing is only possible on test networks. Generate a random account id and
request funding of the account via the Horizon's attached FriendBot instance.

```scala
val horizon = Horizon.sync(Horizon.Endpoints.Test)
val seed = Seed.random
val accountId = seed.accountId
val response = horizon.friendbot.create(accountId)
```

Note that attempting this on a Horizon instance that does not have FriendBot installed will fail.

### Horizon Meta information

You can determine if FriendBot is installed, along with other information about the Horizon instance, by checking the
[HorizonState](https://synesso.github.io/scala-stellar/api/stellar/horizon/HorizonState.html).

```scala
val state: Try[HorizonState] = horizon.meta.state
val friendBotUrl: Try[Option[HttpUrl]] = state.map(_.friendbotUrl)
```


### Account details

With an account newly created on the network, obtain the details of the account.

```scala
val accountDetail: Try[AccountDetail] = horizon.account.detail(accountId)
```


### Making a payment

```scala
val from: SigningKey
val to: Address
val response: Try[TransactionResponse] =
  horizon.transact(from).pay(
    sender = from.address,
    recipient = to,
    amount = Lumens(100)
  )
```