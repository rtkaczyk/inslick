package com.accode.slickvalues

import com.accode.slickvalues.api._
import zio.test._

abstract class SlickValuesSpec extends DefaultRunnableSpec {
  def spec = suite("SlickValues")(
    test("compiles") {
      val q = sqlv"select true".as[Boolean]
      assertTrue(true)
    }
  )
}
