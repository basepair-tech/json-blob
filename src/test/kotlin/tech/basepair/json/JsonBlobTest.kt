// Copyright 2020 Base Pair Pty Ltd.

package tech.basepair.json

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class JsonBlobTest {

  @Test
  fun testNull() {
    val value = JsonBlob.unit()
    val json = value.toJson()
    assertEquals("null", json)
  }

  @Test
  fun testJsonValue() {
    val value = JsonBlob.v("value")
    val json = value.toJson()
    assertEquals("\"value\"", json)
  }

  @Test
  fun testNullStringJsonValue() {
    val str: String? = null
    val value = JsonBlob.v(str)
    val json = value.toJson()
    assertEquals("null", json)
  }

  @Test
  fun testNullNumberJsonValue() {
    val num: Number? = null
    val value = JsonBlob.v(num)
    val json = value.toJson()
    assertEquals("null", json)
  }

  @Test
  fun testBooleanValue() {
    val bool = true
    val value = JsonBlob.v(bool)
    val json = value.toJson()
    assertEquals("true", json)
  }

  @Test
  fun testNumberValue() {
    val num = 2
    val value = JsonBlob.v(num)
    val json = value.toJson()
    assertEquals("2", json)
  }

  @Test
  fun testDecimalNumberValue() {
    val num = 2.1
    val value = JsonBlob.v(num)
    val json = value.toJson()
    assertEquals("2.1", json)
  }

  @Test
  fun testBigDecimalNumberValue() {
    val num = BigDecimal.valueOf(1.01)
    val value = JsonBlob.v(num)
    val json = value.toJson()
    assertEquals("1.01", json)
  }

  @Test
  fun testStringValue() {
    val str = "abc"
    val value = JsonBlob.v(str)
    val json = value.toJson()
    assertEquals("\"abc\"", json)
  }

  @Test
  fun testNumArray() {
    val array = JsonBlob.array(1, 2, 3)
    val json = array.toJson()
    assertEquals("[1,2,3]", json)
  }

  @Test
  fun testNumListArray() {
    val array = JsonBlob.array(listOf(1,2,3))
    val json = array.toJson()
    assertEquals("[1,2,3]", json)
  }

  @Test
  fun testStringArray() {
    val array = JsonBlob.array("a", "b", "c")
    val json = array.toJson()
    assertEquals("[\"a\",\"b\",\"c\"]", json)
  }

  @Test
  fun testStringListArray() {
    val array = JsonBlob.array(listOf("a","b","c"))
    val json = array.toJson()
    assertEquals("[\"a\",\"b\",\"c\"]", json)
  }

  @Test
  fun testBooleanArray() {
    val array = JsonBlob.array(true, false, false)
    val json = array.toJson()
    assertEquals("[true,false,false]", json)
  }

  @Test
  fun testBooleanListArray() {
    val array = JsonBlob.array(listOf(true, false, false))
    val json = array.toJson()
    assertEquals("[true,false,false]", json)
  }

  @Test
  fun testJsonValueArray() {
    val array = JsonBlob.array(JsonBlob.v("a"), JsonBlob.v(1), JsonBlob.v(true))
    val json = array.toJson()
    assertEquals("[\"a\",1,true]", json)
  }

  @Test
  fun testJsonValueListArray() {
    val array = JsonBlob.array(listOf(JsonBlob.v("a"), JsonBlob.v(1), JsonBlob.v(true)))
    val json = array.toJson()
    assertEquals("[\"a\",1,true]", json)
  }

  @Test
  fun testJsonKeyValue() {
    val kv = JsonBlob.kv("key", "value")
    val json = kv.toJson()
    assertEquals("\"key\":\"value\"", json)
  }

  @Test
  fun testJsonObject() {
    val obj = JsonBlob.obj(JsonBlob.kv("key1", "value1"), JsonBlob.kv("key2", "value2"))
    val json = obj.toJson()
    assertEquals("{\"key1\":\"value1\",\"key2\":\"value2\"}", json)
  }

  @Test
  fun testObjectInArray() {
    val array = JsonBlob.array(JsonBlob.obj(JsonBlob.kv("key1", "value1")))
    val json = array.toJson()
    assertEquals("[{\"key1\":\"value1\"}]", json)
  }

  @Test
  fun testNestedJsonObject() {
    val obj = JsonBlob.obj(
        JsonBlob.kv("key1",
            JsonBlob.obj(
                JsonBlob.kv("key2", "value2")
            )
        )
    )
    val json = obj.toJson()
    assertEquals("{\"key1\":{\"key2\":\"value2\"}}", json)
  }

  @Test
  fun testEmptySupplierJsonValue() {
    val someValue = false
    val value = JsonBlob.v { if (someValue) { JsonBlob.v(1) } else { null } }
    val json = value.toJson()
    assertEquals("null", json)
  }

  @Test
  fun testSupplierJsonValue() {
    val someValue = true
    val value = JsonBlob.v { if (someValue) { JsonBlob.v(1) } else { null } }
    val json = value.toJson()
    assertEquals("1", json)
  }

  @Test
  fun testEmptySupplierKeyValueJson() {
    val someValue = false
    val keyValues = JsonBlob.kv { if (someValue) { JsonBlob.kv("key1", 1) } else { null } }
    val json = keyValues.toJson()
    assertEquals("", json)
  }

  @Test
  fun testSupplierKeyValueJson() {
    val someValue = true
    val keyValues = JsonBlob.kv { if (someValue) { JsonBlob.kv("key1", 1) } else { null } }
    val json = keyValues.toJson()
    assertEquals("\"key1\":1", json)
  }

  @Test
  fun testEmptySupplierArray() {
    val someValue = false
    val array = JsonBlob.array { if (someValue) { JsonBlob.array(1,2,3) } else { null } }
    val json = array.toJson()
    assertEquals("[]", json)
  }

  @Test
  fun testSupplierArray() {
    val someValue = true
    val array = JsonBlob.array { if (someValue) { JsonBlob.array(1,2,3) } else { null } }
    val json = array.toJson()
    assertEquals("[1,2,3]", json)
  }

  @Test
  fun testEmptySupplierObject() {
    val someValue = false
    val obj = JsonBlob.obj { if (someValue) { JsonBlob.obj(JsonBlob.kv("key1", true)) } else { null } }
    val json = obj.toJson()
    assertEquals("{}", json)
  }

  @Test
  fun testSupplierObject() {
    val someValue = true
    val obj = JsonBlob.obj { if (someValue) { JsonBlob.obj(JsonBlob.kv("key1", true)) } else { null } }
    val json = obj.toJson()
    assertEquals("{\"key1\":true}", json)
  }

  @Test
  fun testEmptySupplierKvInObject() {
    val someValue = false
    val obj = JsonBlob.obj(
        JsonBlob.kv("key1", "value1"),
        JsonBlob.kv { if (someValue) { JsonBlob.kv("key2", "value2") } else { null } }
    )
    val json = obj.toJson()
    assertEquals("{\"key1\":\"value1\"}", json)
  }

  @Test
  fun testSupplierKvInObject() {
    val someValue = true
    val obj = JsonBlob.obj(
        JsonBlob.kv("key1", "value1"),
        JsonBlob.kv { if (someValue) { JsonBlob.kv("key2", "value2") } else { null } }
    )
    val json = obj.toJson()
    assertEquals("{\"key1\":\"value1\",\"key2\":\"value2\"}", json)
  }

  @Test
  fun testMaskStringValue() {
    val obj = JsonBlob.obj(JsonBlob.kv("key1", JsonBlob.mask("value1")))
    val json = obj.toJson()
    val maskedJson = obj.toJson(true)

    assertEquals("{\"key1\":\"value1\"}", json)
    assertEquals("{\"key1\":\"***masked***\"}", maskedJson)
  }

  @Test
  fun testMaskNumberValue() {
    val obj = JsonBlob.obj(JsonBlob.kv("key1", JsonBlob.mask(123456789)))
    val json = obj.toJson()
    val maskedJson = obj.toJson(true)

    assertEquals("{\"key1\":123456789}", json)
    assertEquals("{\"key1\":\"***masked***\"}", maskedJson)
  }

  @Test
  fun testMaskJsonValue() {
    val obj = JsonBlob.obj(JsonBlob.kv("key1", JsonBlob.mask(JsonBlob.obj(JsonBlob.kv("key2", "value")))))
    val json = obj.toJson()
    val maskedJson = obj.toJson(true)

    assertEquals("{\"key1\":{\"key2\":\"value\"}}", json)
    assertEquals("{\"key1\":\"***masked***\"}", maskedJson)
  }

}
