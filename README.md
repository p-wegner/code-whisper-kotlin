
# Aider Kotlin - AI Coding Assistant

A Kotlin CLI application that helps developers by analyzing code and providing AI-powered suggestions using multiple AI providers (OpenAI, Anthropic, OpenRouter).

## Features

- 📝 **Message-based interaction** with `-m` parameter
- 🔧 **File analysis** - Read and analyze multiple source files
- 🤖 **Multiple AI providers** - OpenAI, Anthropic, and OpenRouter support
- 🎯 **Auto-apply edits** - Automatically apply suggested code changes
- 📊 **Verbose output** - Detailed logging when needed

## Installation

### Prerequisites
- Java 17 or later
- Kotlin 1.9.22 or later

### Build from source
```bash
git clone <repository-url>
cd aider-kotlin
./gradlew build
```

## Usage

### Basic usage
```bash
# Analyze code and ask for suggestions
./gradlew run --args="-m 'Add error handling to this function' src/main/kotlin/MyFile.kt"

# Using jar file
java -jar build/libs/aider-kotlin-1.0.0.jar -m "Refactor this code" MyFile.kt
```

### Set up API Keys
```bash
# OpenAI
export OPENAI_API_KEY="your-api-key-here"

# Anthropic
export ANTHROPIC_API_KEY="your-api-key-here"

# OpenRouter
export OPENROUTER_API_KEY="your-api-key-here"
```

### Command line options

```bash
Usage: aider [OPTIONS] [FILES...]

Options:
  -m, --message TEXT           Describe the changes you want (required)
      --model TEXT             Model to use (default: gpt-4)
      --openai-api-key TEXT    OpenAI API key
      --anthropic-api-key TEXT Anthropic API key
      --openrouter-api-key TEXT OpenRouter API key
  -v, --verbose                Enable verbose output
  -h, --help                   Show this help message
```

### Examples

```bash
# Analyze a single file
aider -m "Add input validation" src/main/kotlin/UserService.kt

# Analyze multiple files with verbose output
aider -v -m "Refactor these classes to use dependency injection" \
  src/main/kotlin/UserService.kt \
  src/main/kotlin/UserRepository.kt

# Use different AI providers
aider --model gpt-3.5-turbo -m "Add documentation" MyClass.kt
aider --model claude-3-opus-20240229 -m "Optimize performance" MyClass.kt
aider --model openai/gpt-4 -m "Add tests" MyClass.kt
```

## Supported Models

### OpenAI
- gpt-4 (default)
- gpt-3.5-turbo
- gpt-4-turbo

### Anthropic
- claude-3-opus-20240229
- claude-3-sonnet-20240229
- claude-3-haiku-20240307

### OpenRouter
- openai/gpt-4
- anthropic/claude-3-opus
- meta-llama/llama-2-70b-chat
- And many more...

## Architecture

- **CLI Layer** (`dev.aider.cli`) - Command line argument parsing
- **Core Logic** (`dev.aider.core`) - Main application logic
- **AI Clients** (`dev.aider.openai`, `dev.aider.anthropic`, `dev.aider.openrouter`) - API communication
- **File Management** (`dev.aider.file`) - File reading and analysis  
- **Output Formatting** (`dev.aider.output`) - User-friendly console output

## Development

### Running tests
```bash
./gradlew test
```

### Building executable jar
```bash
./gradlew jar
```

## License

MIT License - see LICENSE file for details.
