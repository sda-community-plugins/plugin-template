# Micro Focus Deployment Automation Plugin Template

This is a fully documented template that can use to make your own 
[Micro Focus Deployment Automation](https://www.microfocus.com/products/deployment-automation/) plugin from. 
It illustrates how to achieve the following in your own plugin:

  - prompt for and retrieve the value of all types of properties in a step
  - set a property output for a step
  - check if your step is running on Windows or Unix/Linux
  - execute a command line program
  - call a REST API
  - parse the output of command line programs to determine success or failure
  - use the _plugins-commons_ library to simplify your code
  - upgrade a plugin's version when addding or renaming properties and steps
  
Additional product documentation is provided in the [Integration Guide](http://help.serena.com/doc_center/sra/ver6_2_1/sda_integration_guide.pdf).
It is recommend that you read Chapter 7 in this document before you start writing your own plugin.  
  
### Creating your own plugin

To create your own plugin, copy or clone this repository and then update the following files and directories:

  - `pom.xml`:
    - change the `artifactId`, `name` and `description` elements for your own plugin.
  - `src\main\groovy\`:
    - create any new _groovy_ classes you want to use across multiple plugin steps here
    - see `ExampleCLIHelper.groovy` and `ExampleRESTHelper.groovy` for some examples.     
  - `src\main\zip\`:
    - create a new _.grooyy_ file for each step you create. 
    - add an entry into `plugin.xml` for each new step (see below).
  - `src\main\groovy\zip\plugin.xml`:
    - change the `<identifier>` element to refer to your plugin's `id` and `name`.
    - change the `<identifier>` element's `<version>` attribute if you have released your plugin before (or installed
      it into Deployment Automation) and are now creating a new version.
    - change the `<description>` element.
    - change the `<tag>` element to where you want your plugin to appear in the workflow designer palette.
      It is recommended that you try and keep it in the structure that has already been created.
    - add additional `<step-type>` elements making sure that the `<command>` element refers to the new file you
      have created for your step.
  - `src\main\groovy\zip\info.xml`:
    - this file is only used for documentation purposes but you can change the information in here for completeness.
  - `src\main\grooy\upgrade.xml`:
    - you need to add entries in here when you create a new version of your plugin so that existing processes can
      be upgraded successfully
    - you can specify new steps, renamed steps or properties - see the example provided     

### Building the plugin

To build the plugin you will need to clone the following repositories (at the same level as this repository):

 - [mavenBuildConfig](https://github.com/sda-community-plugins/mavenBuildConfig)
 - [plugins-build-parent](https://github.com/sda-community-plugins/plugins-build-parent)
 - [air-plugin-build-script](https://github.com/sda-community-plugins/air-plugin-build-script)
 
 and then compile using the following command:
``` 
   mvn clean package
```   

This will create a _.zip_ file in the `target` directory when you can then install into Deployment Automation
from the **System\Automation** page.

If you have any feedback or suggestions on this template then please contact me using the details below.

Kevin A. Lee

kevin.lee@microfocus.com
