package com.thermora.cli.command

/**
 * Parsed and validated command-line arguments.
 */
internal sealed interface CommandArguments {

    val command: CliCommand

    data object Help : CommandArguments {
        override val command = CliCommand.HELP
    }

    data object Version : CommandArguments {
        override val command = CliCommand.VERSION
    }

    data object ListModels : CommandArguments {
        override val command = CliCommand.LIST_MODELS
    }

    data class DescribeModel(
        val modelId: String,
        val schemaDirection: SchemaDirection? = null
    ) : CommandArguments {
        override val command = CliCommand.DESCRIBE_MODEL
    }

    data object Validate : CommandArguments {
        override val command = CliCommand.VALIDATE
    }

    data object Calculate : CommandArguments {
        override val command = CliCommand.CALCULATE
    }
}

/**
 * Direction of a model serialization schema.
 */
internal enum class SchemaDirection(val wireName: String) {

    INPUT("input"),

    OUTPUT("output");

    companion object {

        fun fromName(name: String): SchemaDirection? =
            entries.firstOrNull { it.wireName == name }
    }
}