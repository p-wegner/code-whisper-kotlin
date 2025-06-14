
# Aider Kotlin - AI Coding Assistant

A Kotlin CLI application inspired by Aider that helps developers by analyzing code and providing AI-powered suggestions using OpenAI's API.

## Features

- 📝 **Message-based interaction** with `-m` parameter
- 🔧 **File analysis** - Read and analyze multiple source files
- 🤖 **OpenAI integration** - Uses GPT-4 by default
- 🎯 **Extensible architecture** - Ready for multiple AI providers
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

### Set up OpenAI API Key
```bash
# Option 1: Environment variable
export OPENAI_API_KEY="your-api-key-here"

# Option 2: Command line argument
./gradlew run --args="--openai-api-key your-key -m 'Your message' file.kt"
```

### Command line options

```bash
Usage: aider [OPTIONS] [FILES...]

Options:
  -m, --message TEXT        Describe the changes you want (required)
  --model TEXT             Model to use (default: gpt-4)
  --openai-api-key TEXT    OpenAI API key
  -v, --verbose            Enable verbose output
  -h, --help               Show this help message
```

### Examples

```bash
# Analyze a single file
aider -m "Add input validation" src/main/kotlin/UserService.kt

# Analyze multiple files with verbose output
aider -v -m "Refactor these classes to use dependency injection" \
  src/main/kotlin/UserService.kt \
  src/main/kotlin/UserRepository.kt

# Use a different model
aider --model gpt-3.5-turbo -m "Add documentation" MyClass.kt

# Get help for specific functionality
aider -m "How can I improve error handling?" ErrorHandler.kt
```

## Architecture

- **CLI Layer** (`dev.aider.cli`) - Command line argument parsing
- **Core Logic** (`dev.aider.core`) - Main application logic
- **OpenAI Client** (`dev.aider.openai`) - API communication
- **File Management** (`dev.aider.file`) - File reading and analysis  
- **Output Formatting** (`dev.aider.output`) - User-friendly console output

## Extending for Other AI Providers

The architecture is designed to easily add support for other AI providers:

1. Create a new client in `dev.aider.providers`
2. Implement the common interface
3. Add provider selection logic in `AiderCore`

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
