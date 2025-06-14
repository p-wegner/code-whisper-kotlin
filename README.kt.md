
# Aider Kotlin - AI Coding Assistant

A Kotlin CLI application inspired by Aider that helps developers by analyzing code and providing AI-powered suggestions using multiple AI providers.

## Features

### ✅ Implemented Features
- 📝 **Message-based interaction** with `-m` parameter  
- 🔧 **File analysis** - Read and analyze multiple source files
- 🤖 **Multiple AI providers** - OpenAI, Anthropic, OpenRouter, DeepSeek
- 🎯 **Flexible model selection** - Support for various models from different providers
- ⚡ **Auto-apply edits** - Automatically apply AI-suggested code changes with `--auto-apply`
- 🔄 **Auto-retry mechanism** - Retry failed requests with intelligent error handling
- 📦 **Auto-commit** - Automatically commit changes to git with `--auto-commit`
- 🔒 **Git integration** - Preserve uncommitted changes by auto-committing before applying new edits
- 🔍 **Search/Replace edit format** - Precise code modifications using SEARCH/REPLACE blocks
- 📊 **Verbose output** - Detailed logging when needed
- 🏗️ **Extensible architecture** - Easy to add new AI providers

### ❌ Unsupported Features (compared to original Aider)
- 💬 **Interactive chat mode** - Currently only supports single message requests
- 📚 **Chat history** - No persistent conversation history
- 🗺️ **Repository map generation** - No automatic codebase mapping
- 📁 **File tree analysis** - No automatic file discovery
- 🔧 **Configuration files** - No `.aider.conf.yml` support
- 🎨 **Syntax highlighting** - Plain text output only
- 📝 **Diff preview** - No visual diff before applying changes
- 🌐 **Web interface** - Command line only
- 🔌 **Plugin system** - No extensibility through plugins
- 📊 **Usage analytics** - No built-in metrics or reporting

## Installation

### Prerequisites
- **For JAR execution**: Java 17 or later
- **For native executable**: GraalVM 21+ with native-image (for building only)

### Option 1: Download Pre-built Releases
Download the latest release from the releases page:
- `aider-windows-complete-X.X.X.zip` - Contains both native .exe and .jar with batch script
- `aider-windows-X.X.X.zip` - Native Windows executable only

### Option 2: Build from Source

#### Build JAR (Cross-platform)
```bash
git clone <repository-url>
cd aider-kotlin
./gradlew build
```

#### Build Native Windows Executable
**Requirements**: GraalVM 21+ with native-image installed

```bash
# Install GraalVM native-image (if not already installed)
# On Windows with GraalVM:
# gu install native-image

# Build native Windows executable
./gradlew buildWindowsExe

# Create distribution zip with executable
./gradlew distWindowsExe

# Create complete Windows distribution (exe + jar + batch)
./gradlew distWindows
```

#### Available Build Tasks
- `./gradlew jar` - Build executable JAR
- `./gradlew buildWindowsExe` - Build native Windows executable
- `./gradlew createWindowsBatch` - Create Windows batch script for JAR
- `./gradlew distWindows` - Create complete Windows distribution
- `./gradlew distWindowsExe` - Create distribution with native executable only

## Usage

### Basic Usage

#### Using Native Executable (Windows)
```bash
# Direct execution (no Java required)
aider.exe -m "Add error handling to this function" src/main/kotlin/MyFile.kt
```

#### Using JAR (Cross-platform)
```bash
# Using gradle wrapper
./gradlew run --args="-m 'Add error handling to this function' src/main/kotlin/MyFile.kt"

# Using built JAR directly
java -jar build/libs/aider-1.0.0.jar -m "Refactor this code" MyFile.kt

# Using Windows batch script (Windows only)
aider.bat -m "Refactor this code" MyFile.kt
```

### Set up API Keys

#### Environment Variables (Recommended)
```bash
# OpenAI
export OPENAI_API_KEY="your-openai-key-here"

# Anthropic
export ANTHROPIC_API_KEY="your-anthropic-key-here"

# OpenRouter
export OPENROUTER_API_KEY="your-openrouter-key-here"

# DeepSeek
export DEEPSEEK_API_KEY="your-deepseek-key-here"
```

#### Command Line Arguments
```bash
aider --openai-api-key your-key -m "Your message" file.kt
aider --anthropic-api-key your-key --model claude-3-sonnet-20240229 -m "Your message" file.kt
aider --deepseek-api-key your-key --model deepseek-chat -m "Your message" file.kt
```

### Command Line Options

```bash
Usage: aider [OPTIONS] [FILES...]

Options:
  -m, --message TEXT           Describe the changes you want (required)
  --model TEXT                Model to use (default: gpt-4)
  -f, --file TEXT             Files to add to the chat session (can be used multiple times)
  --openai-api-key TEXT       OpenAI API key
  --anthropic-api-key TEXT    Anthropic API key  
  --openrouter-api-key TEXT   OpenRouter API key
  --deepseek-api-key TEXT     DeepSeek API key
  --auto-apply                Automatically apply edits suggested by AI
  --auto-commit               Automatically commit changes after applying edits
  --max-retries INT           Maximum number of retries if LLM fails (default: 3)
  -v, --verbose               Enable verbose output
  -h, --help                  Show this help message
```

### Examples

```bash
# Analyze a single file
aider -m "Add input validation" src/main/kotlin/UserService.kt

# Analyze multiple files with verbose output
aider -v -m "Refactor these classes to use dependency injection" \
  src/main/kotlin/UserService.kt \
  src/main/kotlin/UserRepository.kt

# Auto-apply changes and commit to git
aider --auto-apply --auto-commit -m "Add error handling" ErrorHandler.kt

# Use different AI providers
aider --model claude-3-sonnet-20240229 -m "Add documentation" MyClass.kt
aider --model deepseek-chat -m "Optimize this algorithm" Algorithm.kt
aider --model openrouter/anthropic/claude-3.5-sonnet -m "Review code" Code.kt

# Use multiple files with -f parameter
aider -f UserService.kt -f UserRepository.kt -m "Add caching layer"
```

## Supported Models

### OpenAI
- `gpt-4` (default)
- `gpt-4-turbo`
- `gpt-3.5-turbo`
- And other OpenAI models

### Anthropic
- `claude-3-opus-20240229`
- `claude-3-sonnet-20240229` 
- `claude-3-haiku-20240307`
- `claude-3-5-sonnet-20241022`

### DeepSeek
- `deepseek-chat`
- `deepseek-coder`

### OpenRouter
- `openrouter/anthropic/claude-3.5-sonnet`
- `openrouter/openai/gpt-4`
- And hundreds of other models via OpenRouter

## Architecture

- **CLI Layer** (`dev.aider.cli`) - Command line argument parsing
- **Core Logic** (`dev.aider.core`) - Main application logic and orchestration
- **AI Clients** (`dev.aider.openai`, `dev.aider.anthropic`, etc.) - API communication with different providers
- **File Management** (`dev.aider.file`) - File reading and analysis
- **Edit System** (`dev.aider.edit`) - Parse and apply SEARCH/REPLACE edits
- **Git Integration** (`dev.aider.git`) - Git operations and change management
- **Output Formatting** (`dev.aider.output`) - User-friendly console output
- **Retry Logic** (`dev.aider.retry`) - Intelligent retry mechanism for failed requests

## Development

### Running Tests
```bash
./gradlew test
```

### Building All Artifacts
```bash
# Build everything
./gradlew build jar buildWindowsExe distWindows
```

### Adding New AI Providers

The architecture makes it easy to add support for new AI providers:

1. Create a new client in `dev.aider.providers.newprovider`
2. Implement the API communication
3. Add model definitions
4. Add provider selection logic in `AiderCore`
5. Add command line parameter support

## Performance

### Native Executable Benefits
- **Faster startup** - No JVM warmup time
- **Lower memory usage** - No JVM overhead
- **No Java dependency** - Runs on systems without Java installed
- **Smaller distribution** - Single executable file

### JAR Benefits  
- **Cross-platform** - Runs on any system with Java
- **Easier debugging** - Full JVM tooling support
- **Dynamic loading** - Runtime flexibility

## Troubleshooting

### Native Executable Issues
- Ensure GraalVM native-image is properly installed
- Check that all required build dependencies are available
- Use `--verbose` flag for detailed error information

### API Issues
- Verify API keys are set correctly
- Check network connectivity
- Use `--verbose` flag to see detailed API communication
- Try `--max-retries` with a higher value for unstable connections

## License

MIT License - see LICENSE file for details.
