package com.tr.utils

import org.slf4j.Logger

fun readUserConsoleInput(prompt: String, logger: Logger, validation: (String) -> Boolean): String {
    var response: String
    do {
        logger.info(prompt)
        response = readln()
    } while (!validation(response))
    return response
}