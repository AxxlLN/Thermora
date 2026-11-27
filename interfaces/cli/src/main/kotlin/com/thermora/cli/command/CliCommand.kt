package com.thermora.cli.command

/**
 * Commands supported by CLI Protocol V1.
 */
internal enum class CliCommand(val commandName: String) {

    HELP("help"),

    VERSION("version"),

    LIST_MODELS("list-models"),

    DESCRIBE_MODEL("describe-model"),

    VALIDATE("validate"),

    CALCULATE("calculate");

    companion object {

        private val commandsByName =
            entries.associateBy { it.commandName }

        fun fromName(name: String): CliCommand? =
            commandsByName[name]
    }
}