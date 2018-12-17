/* --------------------------------------------------------------------------------
 * This is an example "helper" class that is used to execute a command line program.
 * In this example we are going to execute native operating system commands but typically you
 * would an invoke a custom CLI executable like the "docker" command line program.
 * --------------------------------------------------------------------------------
 */

package com.serena.air.plugin.example

import com.urbancode.air.CommandHelper
import com.urbancode.air.ExitCodeException

class ExampleCLIHelper {
    private def cmdLine         // the command we are going to execute
    private String interpreter  // the interpreter we are going to run the command through
    private File workdir        // where we are going to run the command
    private String output       // the output from the command
    private CommandHelper ch

    /**
     * Default constructor
     * @param workdir The working directory for the CommandHelper CLI
     */
    ExampleCLIHelper(File workdir) {
        if (workdir) {
            this.workdir = workdir
        }
        ch = new CommandHelper(workdir)
    }

    String getWorkDir() {
        return this.workdir }

    String getOutput() {
        return this.output
    }

    /**
     * @param message A message to display when running the command
     * @param args An ArrayList of arguments to be executed by the command prompt
     * @return true if the command is run without any Standard Errors, false otherwise
     */
    def runCommand(def message, def args) {
        args.each() { arg ->
            cmdLine << arg
        }
        boolean status
        try {
            ch.runCommand("INFO - ${message}", cmdLine) { Process proc ->
                def (String out, String err) = captureCommand(proc)
                this.output = out
                if (err) {
                    println("ERROR - ${err}")
                    status = false
                }
                if (out) {
                    println("INFO - Command output:\n${out}")
                    status = true
                }
            }
        } catch (ExitCodeException ex) {
            error(ex.toString())
            return false
        }
        return status
    }

    /**
     * @param interpreter The the interpreter to execute the command line through, e.g. "cmd"
     * @param interpreterOptions Any options the interpreter needs, e.g. "/c"
     */
    def setInterpreter(String interpreter, String interpreterOptions) {
        if (interpreter) {
            cmdLine = ["${interpreter}"]
        }
        if (interpreterOptions) {
            interpreterOptions.split(" ").each { def opt ->
                cmdLine << opt
            }
        }
    }

    // --------------------------------------------------------------------------------

    /**
     * @param proc The process to retrieve the standard output and standard error from
     * @return An array containing the standard output and standard error of the process
     */
    private String[] captureCommand(Process proc) {
        StringBuffer out = new StringBuffer()
        StringBuffer err = new StringBuffer()
        proc.waitForProcessOutput(out, err)
        proc.out.close()
        return [out.toString(), err.toString()]
    }
}
