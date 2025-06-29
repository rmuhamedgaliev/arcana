package io.github.rmuhamedgaliev.arcana.adapters.cli

import io.github.rmuhamedgaliev.arcana.application.services.GameApplicationService
import io.github.rmuhamedgaliev.arcana.domain.model.Language
import io.github.rmuhamedgaliev.arcana.domain.model.story.StoryBeat
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * Adapter for the console interface.
 * Allows playing games through the command line.
 */
class ConsoleGameAdapter(private val gameService: GameApplicationService) {
    private val scanner = Scanner(System.`in`)
    private var playerId: String? = null
    private var currentStoryId: String? = null
    private var currentBeat: StoryBeat? = null
    private var language = Language.EN

    /**
     * Start the console interface.
     */
    fun start() = runBlocking {
        println("Welcome to the Text Quest Engine!")
        println("=================================")

        // Get or create player
        playerId = getPlayerId()

        // Main menu loop
        var running = true
        while (running) {
            println("\nMain Menu:")
            println("1. Start a new game")
            println("2. Change language (current: ${language.displayName})")
            println("3. Exit")
            print("Choose an option: ")

            when (scanner.nextLine().trim()) {
                "1" -> startNewGame()
                "2" -> changeLanguage()
                "3" -> running = false
                else -> println("Invalid option. Please try again.")
            }
        }

        println("Thank you for playing!")
    }

    /**
     * Get the player ID, creating a new player if necessary.
     *
     * @return The player ID
     */
    private suspend fun getPlayerId(): String {
        print("Enter your name: ")
        val name = scanner.nextLine().trim()

        // Generate a random ID if not already set
        val id = UUID.randomUUID().toString()

        // Create player
        gameService.getOrCreatePlayer(id, name)

        return id
    }

    /**
     * Start a new game.
     */
    private suspend fun startNewGame() {
        // TODO: Load available stories and let the player choose one
        println("\nAvailable stories:")
        println("1. Adventure (demo)")
        print("Choose a story: ")

        val choice = scanner.nextLine().trim()
        if (choice != "1") {
            println("Invalid choice or story not available.")
            return
        }

        currentStoryId = "adventure"

        // Start the story
        val startBeat = gameService.startStory(playerId!!, currentStoryId!!)
        if (startBeat == null) {
            println("Failed to start story.")
            return
        }

        currentBeat = startBeat

        // Enter game loop
        gameLoop()
    }

    /**
     * Change the current language.
     */
    private fun changeLanguage() {
        println("\nAvailable languages:")
        Language.values().forEachIndexed { index, lang ->
            println("${index + 1}. ${lang.displayName}")
        }
        print("Choose a language: ")

        val choice = scanner.nextLine().trim().toIntOrNull()
        if (choice != null && choice >= 1 && choice <= Language.values().size) {
            language = Language.values()[choice - 1]
            println("Language changed to ${language.displayName}")
        } else {
            println("Invalid choice. Language not changed.")
        }
    }

    /**
     * Main game loop.
     */
    private suspend fun gameLoop() {
        var playing = true

        while (playing && currentBeat != null) {
            // Display current beat
            println("\n" + (currentBeat!!.text.getTextWithFallback(language) ?: "No text available"))

            // Check if this is an end beat
            if (currentBeat!!.isEndBeat) {
                println("\nThe End")
                playing = false
                continue
            }

            // Display choices
            val validChoices = currentBeat!!.choices
            if (validChoices.isEmpty()) {
                println("\nNo valid choices available. The story ends here.")
                playing = false
                continue
            }

            println("\nChoices:")
            validChoices.forEachIndexed { index, choice ->
                println("${index + 1}. ${choice.text.getTextWithFallback(language) ?: "No text available"}")
            }

            // Get player choice
            print("Choose an option: ")
            val choiceIndex = scanner.nextLine().trim().toIntOrNull()

            if (choiceIndex != null && choiceIndex >= 1 && choiceIndex <= validChoices.size) {
                val choice = validChoices[choiceIndex - 1]

                // Make the choice
                val nextBeat = gameService.makeChoice(playerId!!, currentStoryId!!, choice.id)
                if (nextBeat == null) {
                    println("Invalid choice or error processing choice.")
                    playing = false
                } else {
                    currentBeat = nextBeat
                }
            } else {
                println("Invalid choice. Please try again.")
            }
        }

        // Reset current beat and story
        currentBeat = null
        currentStoryId = null
    }
}
