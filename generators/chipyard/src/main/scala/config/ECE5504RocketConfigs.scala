package chipyard

import chisel3._

import org.chipsalliance.cde.config.{Config, Parameters}
import freechips.rocketchip.rocket._
import freechips.rocketchip.subsystem._

// BAR-fetchers (your tree uses package `barf`)
import barf.{WithHellaCachePrefetcher, WithTLICachePrefetcher, WithTLDCachePrefetcher}
import barf.{SingleNextLinePrefetcherParams, SingleStridedPrefetcherParams, SingleAMPMPrefetcherParams, MultiNextLinePrefetcherParams}

/**********************************************************************
 * Base / cache configs (kept from your original)
 **********************************************************************/

class ECE5504AbstractRocketConfig extends Config(
  new chipyard.config.WithNPerfCounters ++
  new chipyard.config.WithBroadcastManager ++ // remove L2
  new freechips.rocketchip.subsystem.WithNBigCores(1) ++ // Rocket-chip core
  // new chipyard.config.WithSystemBusFrequencyAsDefault ++
  new chipyard.config.WithSystemBusFrequency(500.0) ++
  new chipyard.config.WithMemoryBusFrequency(500.0) ++
  new chipyard.config.WithPeripheryBusFrequency(500.0) ++
  new chipyard.config.AbstractConfig)

/**********************************************************************
 * Cache Blocks
 **********************************************************************/

class ECE5504RocketConfig extends Config(
  new WithL1ICacheSets(64) ++
  new WithL1ICacheWays(1) ++
  new WithL1DCacheSets(64) ++
  new WithL1DCacheWays(8) ++
  new ECE5504AbstractRocketConfig ++
  new WithCacheBlockBytes(64)) // Size of each line in a set (i.e 64 bytes), DO NOT MODIFY..!!!

class ECE5504RocketL2Config extends Config(
  new WithInclusiveCache(nWays = 8, capacityKB = 64) ++
  new ECE5504RocketConfig)

/**********************************************************************
 * Cache prefetching
 **********************************************************************/

// Non-prefetching baseline: make L1D non-blocking (BAR-fetchers requires nMSHRs > 0)
class ECE5504RocketNoPrefetchConfig extends Config(
  new freechips.rocketchip.subsystem.WithNonblockingL1(nMSHRs = 2) ++ // non-blocking L1D
  new freechips.rocketchip.subsystem.WithNBanks(2) ++           // increase broadcast hub trackers
  new ECE5504AbstractRocketConfig)

// Prefetching config (L1D prefetcher). You have one core (tile 0), hence Seq(0).
// Choose ONE of the parameter lines below.
class ECE5504RocketPrefetchConfig extends Config(
  // L1D Next-Line prefetcher:
  // new WithHellaCachePrefetcher(Seq(0), SingleNextLinePrefetcherParams()) ++
  // L1D AMPM prefetcher:
  // new WithHellaCachePrefetcher(Seq(0), SingleAMPMPrefetcherParams()) ++
  // L1D Strided prefetcher (example from your snippet):
  new WithHellaCachePrefetcher(Seq(0), SingleStridedPrefetcherParams()) ++

  // Optional: add TL-level prefetchers (between L1s and L2) and enable their wiring.
  // new WithTLICachePrefetcher(MultiNextLinePrefetcherParams()) ++   // for I$
  // new WithTLDCachePrefetcher(SingleAMPMPrefetcherParams()) ++      // for D$
  // new chipyard.config.WithTilePrefetchers ++

  new ECE5504RocketNoPrefetchConfig)

// --- Removed obsolete hook ---
// class WithL1Prefetcher extends Config((site, here, up) => {
//   case BuildL1Prefetcher => Some((p: Parameters) => Module(new ExampleL1Prefetcher()(p)))
// })
