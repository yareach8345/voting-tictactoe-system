package com.yareach.voting_tictactoe_system.common.extension

import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Any.logger(): Logger = LoggerFactory.getLogger(this.javaClass)