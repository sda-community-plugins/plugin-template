// --------------------------------------------------------------------------------
// This is an example step to illustrate how to parse output (usually from command line tools)
// so that we can determine success or failure. The string to parse is actually in the step's
// <post-processing> element in plugin.xml
// --------------------------------------------------------------------------------

import com.serena.air.plugin.example.*

import com.serena.air.StepFailedException
import com.serena.air.StepPropertiesHelper
import com.urbancode.air.AirPluginTool


//
// Create some variables that we can use throughout the plugin step.
// These are mainly for checking what operating system we are running on.
//
final def PLUGIN_HOME = System.getenv()['PLUGIN_HOME']
final String lineSep = System.getProperty('line.separator')
final String osName = System.getProperty('os.name').toLowerCase(Locale.US)
final String pathSep = System.getProperty('path.separator')
final boolean windows = (osName =~ /windows/)
final boolean vms = (osName =~ /vms/)
final boolean os9 = (osName =~ /mac/ && !osName.endsWith('x'))
final boolean unix = (pathSep == ':' && !vms && !os9)

//
// Initialise the plugin tool and retrieve all the properties that were sent to the step.
//
final def  apTool = new AirPluginTool(this.args[0], this.args[1])
final def  props  = new StepPropertiesHelper(apTool.getStepProperties(), true)


//
// Set a variable for each of the plugin steps's inputs.
// We can check whether a required input is supplied (the helper will fire an exception if not) and
// if it is of the required type.
//
File workDir = new File('.').canonicalFile
String textAreaBox = props.notNull('textAreaBox')
//
// It is good practice to include a "debug" mode option so a user can trace any step failures
//
boolean debugMode = props.optionalBoolean("debugMode", false)

println "----------------------------------------"
println "-- STEP INPUTS"
println "----------------------------------------"

//
// Print out each of the property values.
//
println "Working directory: ${workDir.canonicalPath}"
println "Text Area Box value:\n${textAreaBox?:''}"
println "Debug mode value: ${debugMode}"
if (debugMode) { props.setDebugLoggingMode() }

println "----------------------------------------"
println "-- STEP EXECUTION"
println "----------------------------------------"

//
// The main body of the plugin step - wrap it in a try/catch statement for handling any exceptions.
//
try {
    // Just print out the content of the Text Area box.
    println textAreaBox
} catch (StepFailedException e) {
    //
    // Catch any exceptions we find and print their details out.
    //
    println "ERROR: ${e.message}"
    // An exit with a non-zero value will be deemed a failure
    System.exit 1
}

println "----------------------------------------"
println "-- STEP OUTPUTS"
println "----------------------------------------"

//
// An exit with a zero value means the plugin step execution will be deemed successful.
//
System.exit(0)
