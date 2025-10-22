package com.gitlab.notscripter.composecli.commands

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.mordant.input.interactiveSelectList
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import com.github.ajalt.mordant.terminal.prompt
import com.gitlab.notscripter.composecli.compose.getTemplateDir
import com.gitlab.notscripter.composecli.compose.t
import com.gitlab.notscripter.composecli.compose.updateTemplate
import java.io.File
import java.nio.file.Files

private enum class Template {
    Empty_Activity,
    Empty_Activity_Without_Test,
}

class Init : SuspendingCliktCommand() {
    override fun help(context: Context) =
        "  Kickstart a new Jetpack Compose app from your terminal"

    private val projectName by
        option("-n", "--name").prompt("Name").help("App name (e.g., MyComposeApp)")
    private val projectId by
        option("-p", "--package").prompt("Package").help("Package name (e.g., com.example.app)")
    private val projectPath by
        option("-l", "--location")
            .file()
            .help("Where to create the app (default: current directory)")

    override suspend fun run() {
        val templateName =
            t.interactiveSelectList(
                entries = Template.values().map { it.name.replace("_", " ") },
                title = "Choose template:",
            )
        if (templateName == null) {
            t.println(red("Template is reqruied"))
            return
        }

        val templateDir = getTemplateDir(templateName.replace(" ", ""))

        if (templateDir == null || !templateDir.exists()) {
            t.println(red("Template not found"))
            return
        }

        val tempDir = Files.createTempDirectory("compose-cli-template").toFile()
        templateDir.copyRecursively(tempDir, overwrite = true)

        val updateTemplateOutput = updateTemplate(templateDir, tempDir, projectName, projectId)
        if (!updateTemplateOutput) return

        val destination = projectPath ?: File(projectName)
        if (destination.exists()) {
            t.println(red("Directory already exists"))
            return
        }

        tempDir.copyRecursively(destination, overwrite = true)
        t.println(green("✔️ Project '${projectName}' created"))
    }
}
