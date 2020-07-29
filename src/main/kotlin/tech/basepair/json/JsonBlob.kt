/*
 * Copyright 2020 Base Pair Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and limitations under the License.
 *
 */
package tech.basepair.json

import java.util.*

/**
 * Utility for building a String that represents a JSON object.
 * The JsonBlob object is immutable and adding to the JSON Object or JSON array will result in a new object each time.
 *
 * eg.
 *
 * <code>
 *   JsonBlob json = JsonBlob.obj(
 *     kv("1", "value"),
 *     kv("2", false),
 *     kv("3", numArray(1, 2, 3)),
 *     kv("4", obj(
 *       kv("1", "a"),
 *       kv("2", "b")
 *     )),
 *     kv("5", array(
 *       obj(kv("b",mask("b")))
 *     )),
 *     kv("6", unit()))
 * </code>
 *
 * Will produce: {"1":"value","2":false,"3":[1,2,3],"4":{"1":"a","2":"b"},"5":[{"b":"b"}],"6":null}
 *
 * @author jaiew
 */
sealed class JsonBlob {

  /**
   * Interface to represent objects that are serializable using the [appendTo] method
   */
  interface Blob {
    /**
     * Serializes the JSON out to a Printer
     */
    fun appendTo(printer: Printer) {}
  }

  /**
   * Represents a JSON value such as a JSON Object, String, boolean, or a number
   */
  interface JsonValue : Blob {
    @JvmDefault fun toJson(shouldMask: Boolean = false) = toJson(StringBuilderPrinter(shouldMask))

    @JvmDefault fun toJson(printer: Printer): String {
      appendTo(printer)
      return printer.toString()
    }
  }

  /**
   * Represents a JSON Object
   */
  interface JsonObject : JsonValue {
    fun put(value : JsonKeyValue): JsonObject
  }

  /**
   * Represents a JSON array.
   * Contains a list of JSON values.
   */
  interface JsonArray : JsonValue {
    val values: List<JsonValue>

    fun add(value: JsonValue) : JsonArray
  }

  /**
   * Represents the key-value pair within a JSON Object
   */
  interface JsonKeyValue : JsonValue {
    val key: String
    val value: JsonValue
  }

  /**
   * Represents a string primitive
   */
  interface JsonString : JsonValue {
    val str: String
  }

  /**
   * Represents a number primitive
   */
  interface JsonNumber : JsonValue {
    val number: Number
  }

  /**
   * Represents a boolean primitive
   */
  interface JsonBoolean : JsonValue {
    val bool: Boolean
  }

  companion object {

    private val EMPTY_JSON_VALUE : JsonValue = EmptyJsonValue()
    private val EMPTY_JSON_KEY_VALUE: JsonKeyValue = EmptyJsonKeyValue()
    private const val DEFAULT_MASK: String = "***masked***"

    /**
     * Creates a [JsonObject] that contains the given key-values passed in
     */
    @JvmStatic fun obj(vararg values: JsonKeyValue): JsonObject = JsonObjectImpl(*values)

    /**
     * Create a [JsonObject] that contains the given key-values from the list provided
     */
    @JvmStatic fun obj(values: List<JsonKeyValue>): JsonObject = JsonObjectImpl(*values.toTypedArray())

    /**
     * Creates an array of [JsonNumber] based on the numbers provided
     */
    @JvmName("numArray")
    @JvmStatic fun array(vararg numbers : Number?): JsonArray = JsonArrayImpl(*numbers.map { v(it) }.toTypedArray())

    /**
     * Creates an array of [JsonNumber] based on the provided list
     */
    @JvmName("numArray")
    @JvmStatic fun array(numbers: List<Number?>): JsonArray = JsonArrayImpl(*numbers.map { v(it) }.toTypedArray())

    /**
     * Creates an array of [JsonString] based on the strings provided.
     */
    @JvmName("strArray")
    @JvmStatic fun array(vararg strings : String?): JsonArray = JsonArrayImpl(*strings.map { v(it) }.toTypedArray())

    /**
     * Creates an array of [JsonString] based on the provided list
     */
    @JvmName("strArray")
    @JvmStatic fun array(strings: List<String?>): JsonArray = JsonArrayImpl(*strings.map { v(it) }.toTypedArray())

    /**
     *  Creates an array of [JsonBoolean] based on the booleans provided.
     */
    @JvmName("boolArray")
    @JvmStatic fun array(vararg booleans: Boolean): JsonArray = JsonArrayImpl(*booleans.map { v(it) }.toTypedArray())

    /**
     * Creates an array of [JsonBoolean] based on the provided list
     */
    @JvmName("boolArray")
    @JvmStatic fun array(booleans: List<Boolean>): JsonArray = JsonArrayImpl(*booleans.map { v(it) }.toTypedArray())

    /**
     * Creates an array of [JsonValue]
     */
    @JvmStatic fun array(vararg values: JsonValue): JsonArray = JsonArrayImpl(*values)

    /**
     * Creates an array of [JsonValue] based on the provied list
     */
    @JvmStatic fun array(values: List<JsonValue>): JsonArray = JsonArrayImpl(*values.toTypedArray())

    /**
     * Creates a key-value object that is passed into a [JsonObject]
     * @see obj
     */
    @JvmStatic fun kv(key: String, value: String?): JsonKeyValue = JsonKeyValueImpl(key, v(value))

    /**
     * Creates a key-value object that is passed into a [JsonObject]
     * @see obj
     */
    @JvmStatic fun kv(key: String, value: Boolean): JsonKeyValue = JsonKeyValueImpl(key, v(value))

    /**
     * Creates a key-value object that is passed into a [JsonObject]
     * @see obj
     */
    @JvmStatic fun kv(key: String, value: Number?): JsonKeyValue = JsonKeyValueImpl(key, v(value))

    /**
     * Creates a key-value object that is passed into a [JsonObject]
     * @see obj
     */
    @JvmStatic fun kv(key: String, value: JsonValue): JsonKeyValue = JsonKeyValueImpl(key, value)

    /**
     * Creates a [JsonObject] if one is returned from the supplier otherwise an empty [JsonObject] is returned
     */
    @JvmStatic fun obj(supplier: () -> JsonObject?) = supplier() ?: JsonObjectImpl()

    /**
     * Creates a [JsonArray] if one is returned from the supplier otherwise an empty [JsonArray] is returned
     */
    @JvmStatic fun array(supplier: () -> JsonArray?) = supplier() ?: JsonArrayImpl()

    /**
     * Creates a [JsonKeyValue] if one is returned from the supplier otherwise an empty [JsonKeyValue] is returned.
     * This empty key-value pair will be ignored by the Printer.
     */
    @JvmStatic fun kv(supplier: () -> JsonKeyValue?) = supplier() ?: EMPTY_JSON_KEY_VALUE

    /**
     * Creates a [JsonValue] if one is returned from the supplier otherwise returns an empty value
     * @see unit
     */
    @JvmStatic fun v(supplier: () -> JsonValue?) = supplier() ?: unit()

    /**
     *  Creates a [JsonValue] based on the provided string.
     *  If the string null then an empty value is returned
     *  @see unit
     */
    @JvmStatic fun v(str: String?): JsonValue = if (str != null) JsonStringImpl(str) else unit()

    /**
     * Creates an [JsonValue] based on the boolean provided
     */
    @JvmStatic fun v(bool: Boolean): JsonValue = JsonBooleanImpl(bool)

    /**
     * Creates a [JsonValue] based on the number provided.
     * If the number is null then empty value is returned.
     * @see unit
     */
    @JvmStatic fun v(number: Number?): JsonValue = if (number != null) JsonNumberImpl(number) else unit()

    /**
     * Allows setting null on a value
     *     kv("key", none()) -> "key": null
     */
    @JvmStatic
    fun unit(): JsonValue {
      return object : JsonValue {
        override fun appendTo(printer: Printer) {
          printer.append(null)
        }
      }
    }

    /**
     * Creates a masked value and when the [Printer] is set to mask the value will be replaced with the masked value
     *
     *     // Below results in: {"key": "***masked***"} instead of {"key": "some value"} when masked JSON is requested
     *     val masked = obj(kv("key", mask("some value")))
     *
     * @see [JsonValue.toJson]
     */
    @JvmStatic fun mask(value: String?, mask: String = DEFAULT_MASK): JsonValue = MaskedJsonValue(v(value), mask)

    /**
     * Creates a masked value and when the [Printer] is set to mask the value will be replaced with the masked value
     *
     *     // Below results in: {"key": "***masked***"} instead of {"key": 123} when masked JSON is requested
     *     val masked = obj(kv("key", mask(123)))
     *
     * @see [JsonValue.toJson]
     */
    @JvmStatic fun mask(value: Number?, mask: String = DEFAULT_MASK): JsonValue = MaskedJsonValue(v(value), mask)

    /**
     * Creates a masked value and when the [Printer] is set to mask the value will be replaced with the masked value
     *
     *      // Below results in: {"key": "***masked***"} instead of {"key": {"a": "b"}} when masked JSON is requested
     *     val masked = obj(kv("key", mask( obj("a", "b") )))
     */
    @JvmStatic fun mask(value: JsonValue, mask: String = DEFAULT_MASK): JsonValue = MaskedJsonValue(value, mask)

  }

  private class EmptyJsonValue : JsonValue

  private class JsonObjectImpl internal constructor(vararg _values: JsonKeyValue) : JsonObject {
    private val values : Map<String, JsonKeyValue>

    init {
      values = _values.associateByTo(mutableMapOf(), { it.key } )
    }

    override fun put(value: JsonKeyValue): JsonObject {
      return JsonObjectImpl(value, *values.values.toTypedArray())
    }

    override fun appendTo(printer: Printer) {
      val iterator = values.asSequence().filter { it.value !is EmptyJsonKeyValue }.iterator()

      printer.append("{")
      while(iterator.hasNext()) {
        iterator.next().value.appendTo(printer)
        if (iterator.hasNext()) {
          printer.append(",")
        }
      }
      printer.append("}")
    }
  }

  private class EmptyJsonKeyValue : JsonKeyValue {
    override val key: String
      get() = ""
    override val value: JsonValue
      get() = EMPTY_JSON_VALUE
  }

  private class JsonBooleanImpl(_bool: Boolean): JsonBoolean {
    override val bool = _bool

    override fun appendTo(printer: Printer) {
      printer.append(bool.toString())
    }
  }

  private class JsonStringImpl(_str: String): JsonString {
    override val str = _str

    override fun appendTo(printer: Printer) {
      printer.append('"').append(str).append('"')
    }
  }

  private class JsonNumberImpl(_number: Number): JsonNumber {
    override val number = _number

    override fun appendTo(printer: Printer) {
      printer.append(number.toString())
    }
  }

  private class MaskedJsonValue(_value: JsonValue, _mask: String): JsonValue {
    val value: JsonValue = _value
    val maskedValue: JsonValue = JsonStringImpl(_mask)

    override fun appendTo(printer: Printer) {
      if (printer.shouldMask()) {
        maskedValue.appendTo(printer)
      } else {
        value.appendTo(printer)
      }
    }
  }

  private class JsonKeyValueImpl(_key: String, _value: JsonValue): JsonKeyValue {
    override val key = _key
    override val value = _value

    override fun appendTo(printer: Printer) {
      printer.append('"').append(key).append('"').append(':')
      value.appendTo(printer)
    }
  }

  private class JsonArrayImpl(vararg _values: JsonValue): JsonArray {
    override val values : List<JsonValue> = _values.toList()

    override fun add(value: JsonValue): JsonArray {
      return JsonArrayImpl(value, *values.toTypedArray())
    }

    override fun appendTo(printer: Printer) {
      printer.append("[")
      for (i in values.indices) {
        if (i > 0) {
          printer.append(",")
        }
        values[i].appendTo(printer)
      }
      printer.append("]")
    }
  }

  /**
   * An interface for the Printer that serializes the JSON
   * The default implementation is a StringBuilder Printer that outputs the StringBuilder contents
   * but a implementation could append the JSON to a file.
   */
  interface Printer : Appendable {
    val mask: Boolean

    fun shouldMask() = mask
  }

  /**
   * A Printer allows the printing of the JSON to an appendable
   * This default printer can be used which appends to a StringBuilder.
   * An implementation could be supplied that appends to any Appendable class
   */
  class StringBuilderPrinter(_mask: Boolean = false) : Printer {
    private val appendable = StringBuilder()
    override val mask = _mask

    override fun append(csq: CharSequence?): Appendable {
      appendable.append(csq)
      return this
    }

    override fun append(csq: CharSequence?, p1: Int, p2: Int): Appendable {
      appendable.append(csq)
      return this
    }

    override fun append(c: Char): Appendable {
      appendable.append(c)
      return this
    }

    override fun toString() = appendable.toString()
  }


}
