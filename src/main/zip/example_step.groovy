import com.serena.plugin.*

import com.serena.air.StepFailedException
import com.serena.air.StepPropertiesHelper
import com.serena.air.TextAreaParser
import com.serena.air.ProcessHelper
import com.urbancode.air.AirPluginTool

final def PLUGIN_HOME = System.getenv()['PLUGIN_HOME']
final String lineSep = System.getProperty('line.separator')
final String osName = System.getProperty('os.name').toLowerCase(Locale.US)
final String pathSep = System.getProperty('path.separator')
final boolean windows = (osName =~ /windows/)
final boolean vms = (osName =~ /vms/)
final boolean os9 = (osName =~ /mac/ && !osName.endsWith('x'))
final boolean unix = (pathSep == ':' && !vms && !os9)

final def  apTool = new AirPluginTool(this.args[0], this.args[1])
final def  props  = new StepPropertiesHelper(apTool.getStepProperties(), true)

File workDir = new File('.').canonicalFile
String textBox  = props.notNull('textBox')
String secureBox = props.optional('secureBox')
String textAreaBox = props.optional('textAreaBox')
boolean checkBox = props.optionalBoolean('checkBox', true)
String selectBox = props.optional('selectBox')
String hiddenTextBox = props.optional('hiddenTextBox', '1')

// an example of hot to encrypt the password for storage
String encryptedSecureBox = PluginUtils.Encrypt(secureBox, PluginUtils.getSecretKey())

println "Working directory: ${workDir.canonicalPath}"
println "Text Box Input: ${textBox}"
println "Secure Box Input: ${secureBox}"
println "Encrypted Secure Box: ${encryptedSecureBox}"
println "Check Box Input: ${checkBox}"
println "Select Box Input: ${selectBox?:''}"
println "Hidden Text Area Box Input: ${hiddenTextBox?:'1'}\n"
println "Text Area Box Input:\n${textAreaBox?:''}\n"


try {
    TextAreaParser textAreaParser = new TextAreaParser()
    textAreaParser.delimiter = '='
    textAreaParser.detectComments(true)
    textAreaParser.skipEmpty(true)

    // an example of how to parse name=value entries in a Text Area Box
    Map<String, String> inputPropeMap = textAreaParser.parseToMap(textAreaBox)
    if (inputPropeMap.size() > 0) {
        println "Found the following values in Text Area Box:"
        for (prop in inputPropeMap) {
            if (prop != null) println "${prop}=${inputPropeMap[prop]}"
        }
    } else {
        println "No values fond in Text Area Box"
    }

    // an example of hot to execute a command line program
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

    // an example of how to set an output property
    apTool.setOutputProperty("osName", osName)
    apTool.setOutputProperties()

} catch (Exception e) {
    println "Error running step:"
    e.printStackTrace()
    System.exit(1)
}

System.exit(0)
