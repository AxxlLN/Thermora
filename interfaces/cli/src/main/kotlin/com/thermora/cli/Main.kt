package com.thermora.cli

fun main(args: Array<String>) {
    when (args.firstOrNull()) {
        null, "--help", "-h" -> printHelp()
        "--version", "-v" -> println("Thermora CLI 0.1.0")
        else -> {
            System.err.println("Unknown command: ${args[0]}")
            printHelp()
        }
    }
}

private fun printHelp() {
    println(
        """
        Thermora CLI 0.1.0
        Thermal Analysis Engine

        Usage: thermora <command>

        Options:
          --help, -h       Show help
          --version, -v    Show version
        """.trimIndent()
    )
}
