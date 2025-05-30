# Makefile for Arcana Bot

# Variables
REGISTRY ?= 192.168.1.205:5000
IMAGE_NAME ?= arcana-bot
TAG ?= latest
PLATFORM ?= linux/amd64
FULL_IMAGE = $(REGISTRY)/$(IMAGE_NAME):$(TAG)

# Gradle commands
GRADLE ?= ./gradlew

.PHONY: all build push deploy clean test run run-daemon stop logs help

# Default target
all: build push

# Build container image
build:
	$(GRADLE) shadowJar
	podman build --build-arg BUILDKIT_INLINE_CACHE=1 --platform $(PLATFORM) -t $(FULL_IMAGE) -f Containerfile .

# Push image to registry
push:
	podman push $(FULL_IMAGE)

# Build and push in one command
deploy: build push
	@echo "Successfully deployed $(FULL_IMAGE)"

# Run container locally
TELEGRAM_BOT_TOKEN ?= your_token
BOT_USERNAME ?= your_bot_username

run:
	podman run -it --rm \
		-e TELEGRAM_BOT_TOKEN=$(TELEGRAM_BOT_TOKEN) \
		-e BOT_USERNAME=$(BOT_USERNAME) \
		-e GAMES_DIRECTORY=/app/games \
		-v $(PWD)/games:/app/games:Z \
		--name arcana-bot-temp \
		$(FULL_IMAGE)

# Run with daemon mode
run-daemon:
	podman run -d \
		--name arcana-bot \
		-e TELEGRAM_BOT_TOKEN=$(TELEGRAM_BOT_TOKEN) \
		-e BOT_USERNAME=$(BOT_USERNAME) \
		-e GAMES_DIRECTORY=/app/games \
		-v $(PWD)/games:/app/games:Z \
		--restart unless-stopped \
		$(FULL_IMAGE)

# Development commands
test:
	$(GRADLE) test

# Clean up
clean:
	$(GRADLE) clean
	-podman rmi $(FULL_IMAGE)

# Container management
stop:
	-podman stop arcana-bot
	-podman rm arcana-bot

logs:
	podman logs -f arcana-bot

# Debug - run with shell
debug:
	podman run -it --rm \
		-e TELEGRAM_BOT_TOKEN=$(TELEGRAM_BOT_TOKEN) \
		-e BOT_USERNAME=$(BOT_USERNAME) \
		-v $(PWD)/games:/app/games:Z \
		--entrypoint /bin/bash \
		$(FULL_IMAGE)

# Check if image exists
check:
	podman images | grep $(IMAGE_NAME) || echo "Image not found"
	podman image inspect $(FULL_IMAGE) 2>/dev/null || echo "Image $(FULL_IMAGE) not found"

# Help command
help:
	@echo "Available targets:"
	@echo "  build          - Build container image"
	@echo "  push           - Push image to registry"
	@echo "  deploy         - Build and push image"
	@echo "  run            - Run container interactively"
	@echo "  run-daemon     - Run container in background"
	@echo "  test           - Run tests"
	@echo "  clean          - Clean build artifacts and images"
	@echo "  stop           - Stop and remove container"
	@echo "  logs           - Show container logs"
	@echo "  debug          - Run container with bash shell"
	@echo "  check          - Check if image exists"
	@echo ""
	@echo "Variables:"
	@echo "  REGISTRY           - Registry URL (default: $(REGISTRY))"
	@echo "  IMAGE_NAME         - Image name (default: $(IMAGE_NAME))"
	@echo "  TAG                - Image tag (default: $(TAG))"
	@echo "  PLATFORM           - Target platform (default: $(PLATFORM))"
	@echo "  TELEGRAM_BOT_TOKEN - Telegram bot token (default: $(TELEGRAM_BOT_TOKEN))"
	@echo "  BOT_USERNAME       - Telegram bot username (default: $(BOT_USERNAME))"
