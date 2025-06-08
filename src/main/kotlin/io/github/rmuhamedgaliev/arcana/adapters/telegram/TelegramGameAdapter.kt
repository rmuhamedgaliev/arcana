package io.github.rmuhamedgaliev.arcana.adapters.telegram

import io.github.rmuhamedgaliev.arcana.application.services.GameApplicationService
import io.github.rmuhamedgaliev.arcana.domain.model.Language
import io.github.rmuhamedgaliev.arcana.domain.model.story.StoryBeat
import io.github.rmuhamedgaliev.arcana.infrastructure.config.AppConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

/**
 * Adapter for the Telegram interface.
 * Allows playing games through Telegram.
 */
class TelegramGameAdapter(
    private val config: AppConfig,
    private val gameService: GameApplicationService = GameApplicationService(config)
) : TelegramLongPollingBot() {
    
    // Map of chat ID to current story ID
    private val activeStories = ConcurrentHashMap<Long, String>()
    
    // Map of chat ID to current beat ID
    private val activeBeats = ConcurrentHashMap<Long, StoryBeat>()
    
    // Map of chat ID to language
    private val userLanguages = ConcurrentHashMap<Long, Language>()
    
    // Coroutine scope for async operations
    private val scope = CoroutineScope(Dispatchers.IO)
    
    override fun getBotToken(): String = config.telegramBotToken
    
    override fun getBotUsername(): String = "ArcanaQuestBot"
    
    override fun onUpdateReceived(update: Update) {
        try {
            // Handle callback queries (button clicks)
            if (update.hasCallbackQuery()) {
                handleCallbackQuery(update)
                return
            }
            
            // Handle text messages
            if (update.hasMessage() && update.message.hasText()) {
                handleTextMessage(update)
                return
            }
        } catch (e: Exception) {
            logger.error(e) { "Error processing update" }
        }
    }
    
    /**
     * Handle a callback query (button click).
     *
     * @param update The update
     */
    private fun handleCallbackQuery(update: Update) {
        val callbackQuery = update.callbackQuery
        val chatId = callbackQuery.message.chatId
        val data = callbackQuery.data
        
        when {
            data.startsWith("choice:") -> {
                val choiceId = data.substring("choice:".length)
                handleChoice(chatId, choiceId)
            }
            data.startsWith("language:") -> {
                val languageCode = data.substring("language:".length)
                handleLanguageChange(chatId, languageCode)
            }
            data.startsWith("story:") -> {
                val storyId = data.substring("story:".length)
                handleStoryStart(chatId, storyId)
            }
            data == "start" -> {
                sendMainMenu(chatId)
            }
            data == "language" -> {
                sendLanguageMenu(chatId)
            }
            data == "stories" -> {
                sendStoryMenu(chatId)
            }
        }
    }
    
    /**
     * Handle a text message.
     *
     * @param update The update
     */
    private fun handleTextMessage(update: Update) {
        val message = update.message
        val chatId = message.chatId
        val text = message.text
        
        when (text) {
            "/start" -> {
                sendWelcomeMessage(chatId)
                sendMainMenu(chatId)
            }
            "/language" -> {
                sendLanguageMenu(chatId)
            }
            "/stories" -> {
                sendStoryMenu(chatId)
            }
            "/help" -> {
                sendHelpMessage(chatId)
            }
            else -> {
                // If a story is active, treat as a choice
                if (activeStories.containsKey(chatId) && activeBeats.containsKey(chatId)) {
                    val currentBeat = activeBeats[chatId]
                    if (currentBeat != null) {
                        // Try to parse as a number
                        val choiceIndex = text.toIntOrNull()?.minus(1)
                        if (choiceIndex != null && choiceIndex >= 0 && choiceIndex < currentBeat.choices.size) {
                            val choice = currentBeat.choices[choiceIndex]
                            handleChoice(chatId, choice.id)
                        } else {
                            sendMessage(chatId, "Invalid choice. Please select a valid option.")
                        }
                    }
                } else {
                    sendMessage(chatId, "I don't understand that command. Type /help for assistance.")
                }
            }
        }
    }
    
    /**
     * Handle a choice selection.
     *
     * @param chatId The chat ID
     * @param choiceId The choice ID
     */
    private fun handleChoice(chatId: Long, choiceId: String) {
        val storyId = activeStories[chatId] ?: return
        
        scope.launch {
            try {
                val playerId = chatId.toString()
                val nextBeat = gameService.makeChoice(playerId, storyId, choiceId)
                
                if (nextBeat != null) {
                    activeBeats[chatId] = nextBeat
                    sendBeat(chatId, nextBeat)
                } else {
                    sendMessage(chatId, "Something went wrong. Please try again.")
                }
            } catch (e: Exception) {
                logger.error(e) { "Error making choice" }
                sendMessage(chatId, "An error occurred. Please try again.")
            }
        }
    }
    
    /**
     * Handle a language change.
     *
     * @param chatId The chat ID
     * @param languageCode The language code
     */
    private fun handleLanguageChange(chatId: Long, languageCode: String) {
        val language = Language.fromCode(languageCode) ?: Language.EN
        userLanguages[chatId] = language
        sendMessage(chatId, "Language changed to ${language.displayName}")
        sendMainMenu(chatId)
    }
    
    /**
     * Handle starting a story.
     *
     * @param chatId The chat ID
     * @param storyId The story ID
     */
    private fun handleStoryStart(chatId: Long, storyId: String) {
        scope.launch {
            try {
                val playerId = chatId.toString()
                
                // Ensure player exists
                gameService.getOrCreatePlayer(playerId, "Telegram User")
                
                // Start the story
                val startBeat = gameService.startStory(playerId, storyId)
                
                if (startBeat != null) {
                    activeStories[chatId] = storyId
                    activeBeats[chatId] = startBeat
                    sendBeat(chatId, startBeat)
                } else {
                    sendMessage(chatId, "Failed to start story. Please try again.")
                }
            } catch (e: Exception) {
                logger.error(e) { "Error starting story" }
                sendMessage(chatId, "An error occurred. Please try again.")
            }
        }
    }
    
    /**
     * Send a welcome message.
     *
     * @param chatId The chat ID
     */
    private fun sendWelcomeMessage(chatId: Long) {
        sendMessage(chatId, "Welcome to the Text Quest Engine! Choose a story to begin your adventure.")
    }
    
    /**
     * Send a help message.
     *
     * @param chatId The chat ID
     */
    private fun sendHelpMessage(chatId: Long) {
        sendMessage(chatId, """
            *Text Quest Engine Help*
            
            Available commands:
            /start - Start the bot and show the main menu
            /language - Change your language
            /stories - Browse available stories
            /help - Show this help message
            
            During a story, you can select options by tapping the buttons or typing the number of the option.
        """.trimIndent())
    }
    
    /**
     * Send the main menu.
     *
     * @param chatId The chat ID
     */
    private fun sendMainMenu(chatId: Long) {
        val message = SendMessage.builder()
            .chatId(chatId.toString())
            .text("Main Menu")
            .replyMarkup(InlineKeyboardMarkup.builder().keyboard(listOf(
                listOf(
                    InlineKeyboardButton.builder()
                        .text("Browse Stories")
                        .callbackData("stories")
                        .build()
                ),
                listOf(
                    InlineKeyboardButton.builder()
                        .text("Change Language")
                        .callbackData("language")
                        .build()
                )
            )).build())
            .build()
        
        try {
            execute(message)
        } catch (e: Exception) {
            logger.error(e) { "Error sending main menu" }
        }
    }
    
    /**
     * Send the language menu.
     *
     * @param chatId The chat ID
     */
    private fun sendLanguageMenu(chatId: Long) {
        val buttons = Language.values().map { language ->
            InlineKeyboardButton.builder()
                .text(language.displayName)
                .callbackData("language:${language.code}")
                .build()
        }
        
        val message = SendMessage.builder()
            .chatId(chatId.toString())
            .text("Select a language:")
            .replyMarkup(InlineKeyboardMarkup.builder().keyboard(
                buttons.map { listOf(it) }
            ).build())
            .build()
        
        try {
            execute(message)
        } catch (e: Exception) {
            logger.error(e) { "Error sending language menu" }
        }
    }
    
    /**
     * Send the story menu.
     *
     * @param chatId The chat ID
     */
    private fun sendStoryMenu(chatId: Long) {
        // TODO: Load available stories dynamically
        val message = SendMessage.builder()
            .chatId(chatId.toString())
            .text("Select a story:")
            .replyMarkup(InlineKeyboardMarkup.builder().keyboard(listOf(
                listOf(
                    InlineKeyboardButton.builder()
                        .text("Adventure (demo)")
                        .callbackData("story:adventure")
                        .build()
                )
            )).build())
            .build()
        
        try {
            execute(message)
        } catch (e: Exception) {
            logger.error(e) { "Error sending story menu" }
        }
    }
    
    /**
     * Send a beat to the user.
     *
     * @param chatId The chat ID
     * @param beat The beat
     */
    private fun sendBeat(chatId: Long, beat: StoryBeat) {
        val language = userLanguages.getOrDefault(chatId, Language.EN)
        val text = beat.text.getTextWithFallback(language) ?: "No text available"
        
        if (beat.isEndBeat || beat.choices.isEmpty()) {
            // End of story
            val message = SendMessage.builder()
                .chatId(chatId.toString())
                .text("$text\n\n*The End*")
                .parseMode("Markdown")
                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(listOf(
                    listOf(
                        InlineKeyboardButton.builder()
                            .text("Back to Main Menu")
                            .callbackData("start")
                            .build()
                    )
                )).build())
                .build()
            
            try {
                execute(message)
                
                // Clear active story and beat
                activeStories.remove(chatId)
                activeBeats.remove(chatId)
            } catch (e: Exception) {
                logger.error(e) { "Error sending end beat" }
            }
        } else {
            // Story continues with choices
            val buttons = beat.choices.map { choice ->
                val choiceText = choice.text.getTextWithFallback(language) ?: "No text available"
                InlineKeyboardButton.builder()
                    .text(choiceText)
                    .callbackData("choice:${choice.id}")
                    .build()
            }
            
            val message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(
                    buttons.map { listOf(it) }
                ).build())
                .build()
            
            try {
                execute(message)
            } catch (e: Exception) {
                logger.error(e) { "Error sending beat" }
            }
        }
    }
    
    /**
     * Send a simple text message.
     *
     * @param chatId The chat ID
     * @param text The text
     */
    private fun sendMessage(chatId: Long, text: String) {
        val message = SendMessage.builder()
            .chatId(chatId.toString())
            .text(text)
            .parseMode("Markdown")
            .build()
        
        try {
            execute(message)
        } catch (e: Exception) {
            logger.error(e) { "Error sending message" }
        }
    }
}
