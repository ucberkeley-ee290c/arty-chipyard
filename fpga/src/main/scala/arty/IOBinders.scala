package chipyard.fpga.arty

import chisel3._
import chisel3.experimental.{IO}

import freechips.rocketchip.util._
import freechips.rocketchip.devices.debug._

import sifive.blocks.devices.spi._
import sifive.blocks.devices.gpio._

import chipyard.iobinders.{ComposeIOBinder, OverrideIOBinder}

import testchipip._

class WithDebugResetPassthrough extends ComposeIOBinder({
  (system: HasPeripheryDebugModuleImp) => {
    // Debug module reset
    val io_ndreset: Bool = IO(Output(Bool())).suggestName("ndreset")
    io_ndreset := system.debug.get.ndreset

    // JTAG reset
    val sjtag = system.debug.get.systemjtag.get
    val io_sjtag_reset: Bool = IO(Input(Bool())).suggestName("sjtag_reset")
    sjtag.reset := io_sjtag_reset

    (Seq(io_ndreset, io_sjtag_reset), Nil)
  }
})

/*
class WithTSITLIOPassthrough extends OverrideIOBinder({
  (system: HasPeripheryTSIHostWidget) => {
    require(system.tsiTLMem.size == 1) // not exposing this
    require(system.tsiSerial.size == 1)
    val io_tsi_serial_pins_temp = IO(DataMirror.internal.chiselTypeClone[TSIHostWidgetIO](system.tsiSerial.head)).suggestName("tsi_serial")
    io_tsi_serial_pins_temp <> system.tsiSerial.head
    (Seq(io_tsi_tl_mem_pins_temp, io_tsi_serial_pins_temp), Nil)
  }
})*/

// note: we dont want IO cells because it's easier to bind signals in harness.
// chiptop signals dont matter
class WithQSPIPassthrough extends OverrideIOBinder({
  (system: HasPeripherySPIFlashModuleImp) => {
    val (ports: Seq[SPIPortIO]) = system.qspi.zipWithIndex.map({ case (s, i) =>
      val name = s"spi_${i}"
      val port = IO(new SPIPortIO(s.c)).suggestName(name)
      port <> s
      port
    })
    (ports, Nil)
  }
})

class WithGPIOPassthrough extends OverrideIOBinder({
  (system: HasPeripheryGPIOModuleImp) => {
    val (ports: Seq[GPIOPortIO]) = system.gpio.zipWithIndex.map({ case (s, i) =>
      val name = s"gpio_${i}"
      val port = IO(new GPIOPortIO(s.c)).suggestName(name)
      port <> s
      port
    })
    (ports, Nil)
  }
})


