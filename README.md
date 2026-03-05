# gclog-view

A simple viewer for GC log files.

![screenshot1](docs/images/screenshot-01.png)


## Usage

- Drop a GC log file onto the window, or press `Ctrl`(`⌘`) + `o` to select a log file.
- You can zoom in and out of the graph with `Ctrl`(`⌘`) + mouse wheel, or `+` and `-` keys.
- You can pan the graph by dragging it or using `←` and `→` keys.
- Double-click on the graph to manually set the X-axis range.


## Installing

Download the latest [gclog-view release](https://github.com/naotsugu/gclog-view/releases) and unzip it.
Launch the application by running the executable file.

### Launching on macOS

By default, macOS only allows applications from the official App Store.

If you can't run the downloaded application, you may need to remove the quarantine attribute.
This will bypass the security warning.

1.  Open a Terminal window.
2.  Run the following command:

```shell
sudo xattr -r -d com.apple.quarantine /Applications/gclog-view.app
```


## Building

To build the application from source, run the following commands:

```shell
git clone https://github.com/naotsugu/gclog-view.git
cd gclog-view
./gradlew clean build
```

To run the application directly from the source, use:

```shell
./gradlew run
```
