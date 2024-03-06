# optimising-music-notation
## Sibelius Plugin

### Installing the plugin
To load the plugin into Sibelius, copy `Stenberg.plg` into `%APPDATA%/Avid/Sibelius/Plugins/Stenberg` where `Stenberg` is the category you would like the plugin to appear in in the (alphabetically sorted) menu. On MacOS, the filepath is `~/Library/Application Support/Avid/Sibelius/Plugins/Stenberg`.

You also need to install the converter program by running `stenbergconverter.msi` on Windows or `stenbergconverter.dmg` on MacOS and following the prompts.

### Using the plugin
With a Sibelius score open:
- select the `Home` tab
- open the `Plug-ins` dropdown
- select `Convert to Stenberg Notation` from `Stenberg`
- choose your destination (any extensions here will be ignored)
- start the conversion and view the PDFs in your chosen output location

To pass on information only required in the novel notation, create a boxed letter (`Text->Styles->Boxed text`) and anchor it to the appropriate location:
| Text  | Meaning   |    
|-------------- | -------------- |  
| w    | Artistic whitespace |
| n    | New line     |      
| s    | New section |   
| C    | Capital note |

### Debugging the plugin
If anything in the plugin itself goes wrong, the easiest way to debug is to add `trace` calls. Editing the plugin is done through `File -> Plug-ins -> Edit Plug-ins -> Convert to Stenberg Notation (user copy) -> Edit...`.

If something in the Java executable goes wrong, the error should be propagated through to Sibelius and appear as a MessageBox. _This doesn't happen without `run.bat` and appropriate code changes within the `Convert` method_.

## Building installers

To build an installer for the converter program, first run `maven clean package` in the project root to generate a `.jar` file, then navigate to the directory containing the `.jar` file and:

- For Windows, run `jpackage --input . --name stenbergconverter --main-jar <jar name> --main-class uk.ac.cam.optimisingmusicnotation.Main --type msi --win-console`
- For MacOS, run `jpackage --input . --name stenbergconverter --main-jar <jar name> --main-class uk.ac.cam.optimisingmusicnotation.Main --type dmg`
