import org.apache.commons.exec.*
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.RuntimeException

class Main{
	companion object {
		@JvmStatic
		private val log by lazy { LoggerFactory.getLogger(Main::class.java) }

		@JvmStatic
		fun main(args: Array<String>){
			val cmd = mutableListOf("adb")

			if (args.isNotEmpty()){
				cmd.add("-s")
				cmd.add(args[0])
			}

			cmd.addAll(listOf("logcat",
					"-d",
					"-v", "threadtime",
					"-v", "year",
					"-s", "System.out",
					"-t", "'2018-09-18 11:15:40.446'"))

			execute(*cmd.toTypedArray())
		}

		@JvmStatic
		private fun execute(vararg cmdLineParams: String) {
			assert(cmdLineParams.isNotEmpty()) { "At least one command line parameters has to be given, denoting the executable." }

			// Prepare the command to execute.
			val commandLine = cmdLineParams.joinToString (" ")
			val command = CommandLine.parse(commandLine)

			// Prepare the process stdout and stderr listeners.
			val processStdoutStream = ByteArrayOutputStream()
			val processStderrStream = ByteArrayOutputStream()
			val pumpStreamHandler = PumpStreamHandler(processStdoutStream, processStderrStream)

			// Prepare the process executor.
			val executor = DefaultExecutor()
			executor.streamHandler = pumpStreamHandler

			// Only exit value of 0 is allowed for the call to return successfully.
			executor.setExitValue(0)

			log.info("Command:")
			log.info(commandLine)

			val exitValue: Int
			try {
				exitValue = executor.execute(command)

			} catch (e: ExecuteException) {
				throw RuntimeException(String.format("Failed to execute a system command.\n"
						+ "Command: %s\n"
						+ "Captured exit value: %d\n"
						+ "Captured stdout: %s\n"
						+ "Captured stderr: %s",
						command.toString(),
						e.exitValue,
						if (processStdoutStream.toString().isNotEmpty()) processStdoutStream.toString() else "<stdout is empty>",
						if (processStderrStream.toString().isNotEmpty()) processStderrStream.toString() else "<stderr is empty>"),
						e)

			} catch (e: IOException) {
				throw RuntimeException(String.format("Failed to execute a system command.\n"
						+ "Command: %s\n"
						+ "Captured stdout: %s\n"
						+ "Captured stderr: %s",
						command.toString(),
						if (processStdoutStream.toString().isNotEmpty()) processStdoutStream.toString() else "<stdout is empty>",
						if (processStderrStream.toString().isNotEmpty()) processStderrStream.toString() else "<stderr is empty>"),
						e)
			} finally {
				log.info("Captured stdout:")
				log.info(processStdoutStream.toString())

				log.info("Captured stderr:")
				log.info(processStderrStream.toString())
			}
			log.info("Captured exit value: $exitValue")
			log.info("DONE executing system command")
		}

	}
}