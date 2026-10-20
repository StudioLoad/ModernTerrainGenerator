
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class CopyFile : DefaultTask() {

    @get:InputFile
    abstract val sourceFile: RegularFileProperty

    @get:OutputFile
    abstract val destination: RegularFileProperty

    @TaskAction
    fun copyFile() {
        val source = sourceFile.get().asFile
        val target = destination.get().asFile

        target.parentFile.mkdirs()
        source.copyTo(target, overwrite = true)
    }
}
