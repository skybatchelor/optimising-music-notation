# optimising-music-notation
## Sibelius Plugin
The plugin has not been tested on MacOS.
### Installing the plugin
To load the plugin into Sibelius, copy `Mike.plg` into `%APPDATA%/Avid/Sibelius/Plugins/{CATEGORY}` where `{CATEGORY}` is the category you would like the plugin to appear in in the (alphabetically sorted) menu. On MacOS, the filepath is `~/Library/Application Support/Avid/Sibelius/Plugins/{CATEGORY}`.
You will also need to ensure you set the correct path to the installation directory (the one containing `run.bat` and `run.sh` by modifying `INSTALL_DIR` on line 337 of `Mike.plg` (or from within Sibelius), and to place the file `optimisingmusicnotation.jar` in this installation directory.
> Note: `INSTALL_DIR` *cannot* contain any spaces (using MS-DOS short names will work)


### Using the plugin
With a Sibelius score open:
- select the `Home` tab
- open the `Plug-ins` dropdown
- select `Mike` from `{CATEGORY}`
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
If anything in the plugin itself goes wrong, the easiest way to debug is to add `trace` calls. Editing the plugin is done through `File -> Plug-ins -> Edit Plug-ins -> Mike (user copy) -> Edit...`.

If something in the Java executable goes wrong, the error should be propogated through to Sibelius and appear as a MessageBox.

TODO
