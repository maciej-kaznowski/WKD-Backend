package com.innercirclesoftware.wkd_client.exceptions

class WkdHttpException(
    val errorCode: Int,
    val errorBody: String?,
    val errorMessage: String?
) : RuntimeException("HTTP error response. code=$errorCode, message='$errorMessage', body='$errorBody")