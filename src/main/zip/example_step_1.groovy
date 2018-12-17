// --------------------------------------------------------------------------------
// This is an example step to illustrate most of the basic operations including:
//   - retrieving step inputs
//   - executing command line programs
//   - executing REST APIs
//   - setting step outputs
// --------------------------------------------------------------------------------

import com.serena.air.plugin.example.*

import com.serena.air.StepFailedException
import com.serena.air.StepPropertiesHelper
import com.serena.air.TextAreaParser
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
// We can get Deployment Automation URL, current user and password from AirPluginTool
// We will use this later in ExampleRESTHelper
//
final def daUser = apTool.getAuthTokenUsername()
final def daPass = apTool.getAuthToken()
final def daUrl = System.getenv("AH_WEB_URL")

//
// Set a variable for each of the plugin steps's inputs.
// We can check whether a required input is supplied (the helper will fire an exception if not) and
// if it is of the required type.
//
File workDir = new File('.').canonicalFile
String textBox  = props.notNull('textBox')
String secureBox = props.optional('secureBox')
String textAreaBox = props.optional('textAreaBox')
boolean checkBox = props.optionalBoolean('checkBox', true)
boolean anotherCheckBox = props.optionalBoolean('anotherCheckBox', true)

String selectBox = props.optional('selectBox')
String hiddenTextBox = props.optional('hiddenTextBox', '1')
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
println "Text Box value: ${textBox}"
println "Secure Box value: ${secureBox}"
// This is an example of how to encrypt a password value if you want to store it on
// the file system (or a third party tool requires an encrypted value.
String encryptedSecureBox = PluginUtils.Encrypt(secureBox, PluginUtils.getSecretKey())
println "Encrypted value: ${encryptedSecureBox}"
println "Check Box value: ${checkBox}"
println "Another Check Box value: ${anotherCheckBox}"
println "Select Box value: ${selectBox?:''}"
println "Hidden Text Area Box value: ${hiddenTextBox?:'1'}"
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
    //
    // This is an example of how to parse "key=value" entries in a Text Area Box
    //
    TextAreaParser textAreaParser = new TextAreaParser()
    textAreaParser.delimiter = '='
    textAreaParser.detectComments(true)
    textAreaParser.skipEmpty(true)
    Map<String, String> inputPropsMap = textAreaParser.parseToMap(textAreaBox)
    if (inputPropsMap.size() > 0) {
        println "Found the following keys and values in the Text Area Box:"
        inputPropsMap.each{ k, v -> println "\t${k} = ${v}" }
    } else {
        println "No values found in Text Area Box"
    }

    println "----------------------------------------"

    //
    // This is an example of calling a REST API (in this case Deployment Automation itself) through a helper.
    //
    println "INFO - Running REST command"
    ExampleRESTHelper daHelper = new ExampleRESTHelper(daUrl, daUser, daPass)
    def componentsJson =  daHelper.getComponents()
    println "Found the following components:"
    print "\t"
    for (def comp : componentsJson) {
        print "${comp?.name} "
    }
    print "\n"

    println "----------------------------------------"

    //
    // This is an example of how to execute a command line program again through a helper
    //
    def command // the command we are going to execute
    ExampleCLIHelper helper = new ExampleCLIHelper(workDir)
    if (windows) {
        println "INFO - Running CLI command on Windows"
        command = ["dir"]
        command << "/W"
        command << "/S"
        helper.setInterpreter("cmd", "/c")
    } else if (unix) {
        println "INFO - Running CLI command on Linux/Unix"
        command = ["ls"]
        command << "-ltr"
        helper.setInterpreter("bash", "-c")
    } else {
        throw new StepFailedException("Running on unsupported platform")
    }

    if (helper.runCommand("Executing ${command}", command)) {
        println helper.getOutput()
    } else {
        throw new StepFailedException("Error executing command: ${command}")
    }
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
// This is an example of how to set an output property so that it can be used in other steps.
// It can be referred to via "Step Name/Output Property Name"
//
apTool.setOutputProperty("osName", osName)
println("Setting \"osName\" output property to \"${osName}\"")
apTool.setOutputProperties()

//
// An exit with a zero value means the plugin step execution will be deemed successful.
//
System.exit(0)
