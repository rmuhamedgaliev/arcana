package io.github.rmuhamedgaliev.arcana

import io.github.rmuhamedgaliev.arcana.adapters.cli.ConsoleGameAdapter
import io.github.rmuhamedgaliev.arcana.adapters.telegram.TelegramGameAdapter
import io.github.rmuhamedgaliev.arcana.application.services.GameApplicationService
import io.github.rmuhamedgaliev.arcana.infrastructure.config.AppConfig
import mu.KotlinLogging
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

private val logger = KotlinLogging.logger {}

/**
 * Main entry point for the application.
 * Can launch either the console interface or the Telegram bot interface.
 */
fun main(args: Array<String>) {
    // Get configuration from environment variables with default values
    val config = AppConfig.fromEnvironment()
    
    // If bot token is provided, launch Telegram bot interface
    if (config.telegramBotToken.isNotBlank()) {
        launchTelegramBot(config)
    } else {
        // Otherwise, launch console interface
        launchConsoleInterface(config)
    }
}

/**
 * Launch the Telegram bot interface.
 * 
 * @param config The application configuration
 */
private fun launchTelegramBot(config: AppConfig) {
    try {
        logger.info { "Starting Telegram bot..." }
        val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
        val telegramAdapter = TelegramGameAdapter(config)
        botsApi.registerBot(telegramAdapter)
        logger.info { "Bot started successfully!" }
        logger.info { "Games directory: ${config.gamesDirectory}" }
    } catch (e: TelegramApiException) {
        logger.error(e) { "Error starting bot" }
    }
}

/**
 * Launch the console interface.
 * 
 * @param config The application configuration
 */
private fun launchConsoleInterface(config: AppConfig) {
    try {
        logger.info { "Starting console interface..." }
        val gameService = GameApplicationService(config)
        val consoleAdapter = ConsoleGameAdapter(gameService)
        consoleAdapter.start()
    } catch (e: Exception) {
        logger.error(e) { "Error in console interface" }
    }
}
