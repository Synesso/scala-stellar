package stellar.horizon

import okhttp3.HttpUrl
import stellar.protocol.NetworkId

/**
 * Configuration specifying the location and network id of a Stellar network.
 *
 * @param id  unique public passphrase used to differentiate signed transactions on different networks.
 * @param url the base url for the network.
 */
case class Network(id: NetworkId, url: HttpUrl)
