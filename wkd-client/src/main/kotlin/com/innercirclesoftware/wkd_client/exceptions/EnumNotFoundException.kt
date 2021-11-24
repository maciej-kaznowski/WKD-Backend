package com.innercirclesoftware.wkd_client.exceptions

class EnumNotFoundException(
    val clazz: Class<out Enum<*>>,
    val name: String,
    val values: Array<*>,
) : RuntimeException("Enum with name='$name' not found in class='$clazz'. values=$values")