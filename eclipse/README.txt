Eclipse 3.3.X instructions

Note:
These eclipse projects assume you have the GWT source tree already installed
and imported as eclipse project.  If you do not, you will have to remove the
references to the GWT projects from eclipse and substitute in the GWT .jar files
as external jar files.

---------- Required Google API Library for GWT variables ---------
Window->Preferences->General->Workspace->Linked Resources
Create a variable named "GWT_JSIO_ROOT" pointing to a copy of the gwt-api-interop source folder.

Window->Preferences->Java->Build Path->Classpath Variables
Create a variable named "GWT_HOME" pointing to a GWT install folder.
Create a variable named "JDK_HOME" pointing to the root of your JDK install
  (for example, C:\Program Files\jdk1.5.0_05 or /usr/lib/j2sdk1.5-sun)

Project->Java Build Path->Projects
You may need to swap out the gwt-dev-linux project for your project.

---------- All other settings ------------------------------------
All other eclipse settings can be found at:

http://google-web-toolkit.googlecode.com/svn/trunk/eclipse/README.txt
