
# Aider Kotlin - AI Coding Assistant

A Kotlin CLI application that helps developers by analyzing code and providing AI-powered suggestions using multiple AI providers (OpenAI, Anthropic, OpenRouter, DeepSeek).

## ✅ Implemented Features

- 📝 **Message-based interaction** with `-m` parameter
- 🔧 **File analysis** - Read and analyze multiple source files
- 🤖 **Multiple AI providers** - OpenAI, Anthropic, OpenRouter, and DeepSeek support
- 🎯 **Auto-apply edits** - Automatically apply suggested code changes using SEARCH/REPLACE blocks
- 📊 **Verbose output** - Detailed logging when needed
- 🔄 **Auto-retry mechanism** - Automatically retries if LLM fails to follow output format (max 3 retries)
- 📦 **Auto-commit** - Automatically commit changes after applying edits
- 🔍 **Edit parsing** - Parse SEARCH/REPLACE blocks from AI responses
- 🛠️ **Git integration** - Detect Git repositories and commit changes
- 🎛️ **Flexible model selection** - Support for various models across all providers
- 🌳 **Repository mapping** - Automatic repository structure analysis (4k tokens)
- 📚 **Chat history** - Persistent conversation history stored in `.aider/chat_history.md`

### Supported AI Providers & Models

#### OpenAI
- gpt-4 (default)
- gpt-3.5-turbo
- gpt-4-turbo
- gpt-4o

#### Anthropic (Claude)
- claude-3-opus-20240229
- claude-3-sonnet-20240229
- claude-3-haiku-20240307
- claude-3-5-sonnet-20241022

#### OpenRouter
- openai/gpt-4
- anthropic/claude-3-opus
- meta-llama/llama-2-70b-chat
- mistralai/mixtral-8x7b-instruct
- google/gemini-pro
- And many more...

#### DeepSeek
- deepseek-chat
- deepseek-coder
- deepseek-reasoner

## 🚧 Not Yet Implemented Features

- 🔄 **Interactive chat mode** - Currently only supports single-shot requests
- 📖 **Context management** - Smart selection of relevant files based on request
- 🔍 **Semantic search** - Find relevant code across the repository
- 🧪 **Test generation** - Automatic test creation for modified code
- 📋 **Diff preview** - Show changes before applying them
- 🎨 **Custom prompts** - User-defined system prompts or templates
- 📊 **Usage analytics** - Token usage and cost tracking
- 🔧 **Configuration files** - Support for .aiderrc or similar config files
- 🌐 **Web interface** - Currently CLI-only
- 📱 **Integration plugins** - IDE/editor extensions
- 🔄 **Watch mode** - Monitor files for changes and auto-suggest improvements
- 🏷️ **Tagging system** - Organize and categorize conversations
- 📈 **Performance metrics** - Code quality and performance analysis
- 🛡️ **Security scanning** - Automatic security vulnerability detection

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

### Build native executable (Windows)
```bash
# Build native executable
./gradlew nativeCompile

# The executable will be at: build/native/nativeCompile/aider-kotlin.exe
```

## Usage

### Basic usage
```bash
# Analyze code and ask for suggestions
./gradlew run --args="-m 'Add error handling to this function' src/main/kotlin/MyFile.kt"

# Using jar file
java -jar build/libs/aider-kotlin-1.0.0.jar -m "Refactor this code" MyFile.kt

# Using native executable (Windows)
./build/native/nativeCompile/aider-kotlin.exe -m "Add tests" MyFile.kt
```

### Set up API Keys
```bash
# OpenAI
export OPENAI_API_KEY="your-api-key-here"

# Anthropic
export ANTHROPIC_API_KEY="your-api-key-here"

# OpenRouter
export OPENROUTER_API_KEY="your-api-key-here"

# DeepSeek
export DEEPSEEK_API_KEY="your-api-key-here"
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
      --deepseek-api-key TEXT  DeepSeek API key
      --auto-apply             Automatically apply edits suggested by AI
      --auto-commit            Automatically commit changes after applying edits
      --max-retries INT        Maximum number of retries if LLM fails to follow format (default: 3)
      --clear-history          Clear the chat history and exit
  -f, --file TEXT              Files to add to the chat session (can be used multiple times)
  -v, --verbose                Enable verbose output
  -h, --help                   Show this help message
```

### Chat History

Aider automatically maintains a conversation history in `.aider/chat_history.md`. This file includes:

- Timestamp of each interaction
- User requests and AI responses
- Applied edits and affected files
- Model used for each request

The last 3 sessions are automatically included in the context for new requests to maintain conversation continuity.

```bash
# Clear chat history
aider --clear-history

# The history file is located at: .aider/chat_history.md
```

### Examples

```bash
# Analyze a single file
aider -m "Add input validation" src/main/kotlin/UserService.kt

# Analyze multiple files with verbose output
aider -v -m "Refactor these classes to use dependency injection" \
  src/main/kotlin/UserService.kt \
  src/main/kotlin/UserRepository.kt

# Auto-apply changes and commit them
aider --auto-apply --auto-commit -m "Add documentation" MyClass.kt

# Use different AI providers
aider --model gpt-3.5-turbo -m "Add documentation" MyClass.kt
aider --model claude-3-opus-20240229 -m "Optimize performance" MyClass.kt
aider --model openai/gpt-4 -m "Add tests" MyClass.kt
aider --model deepseek-chat -m "Refactor this code" MyClass.kt

# Retry mechanism with custom max retries
aider --auto-apply --max-retries 5 -m "Fix this bug" MyClass.kt

# Clear conversation history
aider --clear-history
```

## Architecture

- **CLI Layer** (`dev.aider.cli`) - Command line argument parsing
- **Core Logic** (`dev.aider.core`) - Main application logic and orchestration
- **AI Clients** (`dev.aider.openai`, `dev.aider.anthropic`, `dev.aider.openrouter`, `dev.aider.deepseek`) - API communication
- **File Management** (`dev.aider.file`) - File reading and analysis  
- **Output Formatting** (`dev.aider.output`) - User-friendly console output
- **Edit System** (`dev.aider.edit`) - Parse and apply SEARCH/REPLACE blocks
- **Git Integration** (`dev.aider.git`) - Git repository operations
- **Retry System** (`dev.aider.retry`) - Handle LLM format failures with intelligent retries
- **Repository Mapping** (`dev.aider.repomap`) - Analyze and map repository structure
- **Chat History** (`dev.aider.history`) - Persistent conversation history management

## How Auto-Apply Works

When `--auto-apply` is enabled, Aider:

1. **Requests structured output** - Asks the LLM to format responses using SEARCH/REPLACE blocks
2. **Parses edit blocks** - Extracts file paths, search content, and replacement content
3. **Validates format** - Ensures the LLM followed the required format
4. **Applies changes** - Searches for exact matches and replaces them
5. **Retries on failure** - If format validation fails, enhances the prompt and retries (up to `--max-retries`)
6. **Auto-commits** - If `--auto-commit` is enabled, commits successful changes to Git
7. **Records history** - Saves the conversation and results to chat history

### SEARCH/REPLACE Block Format

```
src/example.kt
<<<<<<< SEARCH
old code here
=======
new code here
>>>>>>> REPLACE
```

## Development

### Running tests
```bash
./gradlew test
```

### Building executable jar
```bash
./gradlew jar
```

### Building native executable
```bash
./gradlew nativeCompile
```

## Contributing

We welcome contributions! Areas where help is especially needed:

1. **Interactive chat mode** - Implement conversation history and context management
2. **IDE integrations** - Plugins for popular IDEs
3. **Performance optimizations** - Faster file processing and API calls
4. **Additional AI providers** - Support for more LLM APIs

## License

MIT License - see LICENSE file for details.
