import com.serena.plugin.*

import com.serena.air.StepFailedException
import com.serena.air.StepPropertiesHelper
import com.serena.air.TextAreaParser
import com.serena.air.ProcessHelper
import com.urbancode.air.AirPluginTool

//
// Create some variables that we can use throughout the plugin step
// These are mainly for checking what operating system we are running on
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
// Initialise the plugin and retrieve all the properties that were sent to the step
//
final def  apTool = new AirPluginTool(this.args[0], this.args[1])
final def  props  = new StepPropertiesHelper(apTool.getStepProperties(), true)

//
// Set a variable for each of the plugin steps's inputs
// We can check whether a required step is supplied (the helper's will fire an exception if not) and
// if it is of the required type
//
File workDir = new File('.').canonicalFile
String textBox  = props.notNull('textBox')
String secureBox = props.optional('secureBox')
String textAreaBox = props.optional('textAreaBox')
boolean checkBox = props.optionalBoolean('checkBox', true)
String selectBox = props.optional('selectBox')
String hiddenTextBox = props.optional('hiddenTextBox', '1')

//
// Print out each of the property values
//
println "Working directory: ${workDir.canonicalPath}"
println "Text Box value: ${textBox}"
println "Secure Box value: ${secureBox}"
println "Check Box value: ${checkBox}"
println "Select Box value: ${selectBox?:''}"
println "Hidden Text Area Box value: ${hiddenTextBox?:'1'}\n"
println "Text Area Box value:\n${textAreaBox?:''}\n"
//
// This is an example of how to encrypt a password value if you want to store it on
// the file system (or a third party tool requires an encrypted value
//
// an example of hot to encrypt the password for storage
String encryptedSecureBox = PluginUtils.Encrypt(secureBox, PluginUtils.getSecretKey())
println "Encrypted value: ${encryptedSecureBox}"


//
// The main body of the plugin step - wrap in a try/catch statement for handling any exceptions
//
try {
    //
    // This is an example of how to parse "name=key" values in a Text Area Box
    //
    TextAreaParser textAreaParser = new TextAreaParser()
    textAreaParser.delimiter = '='
    textAreaParser.detectComments(true)
    textAreaParser.skipEmpty(true)
    Map<String, String> inputPropeMap = textAreaParser.parseToMap(textAreaBox)
    if (inputPropeMap.size() > 0) {
        println "Found the following values in Text Area Box:"
        for (prop in inputPropeMap) {
            if (prop != null) println "${prop}=${inputPropeMap[prop]}"
        }
    } else {
        println "No values fond in Text Area Box"
    }

    //
    // This is an example of hot to execute a command line program
    //
    if (windows) {
        println "Running on Windows"
        def command = "dir"
        command << "/W"
        command << "/S"
        ProcessHelper.run(new ProcessBuilder(command as String[]), "Error running command")
    } else if (unix) {
        println "Running on Unix"
        def command = "ls"
        command << "-ltr"
        ProcessHelper.run(new ProcessBuilder(command as String[]), "Error running command")
    } else {
        throw new StepFailedException("Running on unsupported platform")
    }

    //
    // This is an example of how to set an output property so that is can be used in other steps
    // It can be refered to via "Step Name/Output Property Name"
    //
    apTool.setOutputProperty("osName", osName)
    apTool.setOutputProperties()

} catch (Exception e) {
    //
    // Catch any exceptions we find and print their details out
    //
    println "Error running step:"
    e.printStackTrace()
    System.exit(1)
}

//
// An exit with a zero value means the plugin step execution will be deemed successful
//
System.exit(0)
