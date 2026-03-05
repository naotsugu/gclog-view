# gclog-view

A simple viewer for GC log files.

![screenshot1](docs/images/screenshot-01.png)


## Usage

- Open a log file by dropping it onto the window or pressing `Ctrl`(`⌘`) + `o`.
- Zoom the graph using `Ctrl`(`⌘`) + mouse wheel, or the `+` and `-` keys.
- Pan the graph by dragging with the mouse or using the `←` and `→` keys.
- Set the X-axis range manually by double-clicking the graph.


## Installing

Download the latest [gclog-view release](https://github.com/naotsugu/gclog-view/releases) and unzip it.
Launch the application by running the executable file.

### Launching on macOS

macOS may prevent running applications from unidentified developers.
If you cannot run the application, you may need to remove the quarantine attribute to bypass the security warning.

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
