<h3 align="center">
  <img src="GitHubAssets/sj_lockup.png?raw=true" alt="Science Journal Logo" width="700">
</h3>

[![Twitter: @GScienceJournal](https://img.shields.io/badge/contact-@GScienceJournal-673fb4.svg?style=flat)](https://twitter.com/GScienceJournal)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

[Science Journal for iOS][appstore] allows you to gather data from the world around you. It uses sensors to
measure your environment, like light and sound, so you can graph your data, record your experiments,
and organize your questions and ideas. It's the lab notebook you always have with you.

<img src="GitHubAssets/image1.png?raw=true" alt="iOS screenshot showing experiments list." width="175"><img src="GitHubAssets/image2.png?raw=true" alt="iOS screenshot showing `Iodine Clock` experiment." width="175"><img src="GitHubAssets/image3.png?raw=true" alt="iOS screenshot showing `Iodine Clock` recording showing brightness sensor with various values." width="175"><img src="GitHubAssets/image4.png?raw=true" alt="iOS screenshot showing `Iodine Clock` recording with notes alongside brightness sensor with various values." width="175">

[Science Journal][play-store] allows you to gather data from the world around you. It uses sensors to measure your environment, like light and sound, so you can graph your data, record your experiments, and organize your questions and ideas. It's the lab notebook you always have with you.

Open Science Journal is the core of the Science Journal app with the same UI and sensor code and can be compiled and run on its own.

## Features

* Visualize and graph data from sensors.
* Connect to external sensors over BLE ([firmware code][firmware-github]).
* Annotate with pictures and notes.

## Building the app

Download the source, go into the OpenScienceJournal directory and run:

    ./gradlew app:installDebug

Alternatively, import the source code in OpenScienceJournal into Android Studio (File, Import Project).

Note: You'll need Android SDK version 27, build tools 23.0.3, and the Android Support Library to
compile the project. If you're unsure about this, use Android Studio and tick the appropriate boxes
in the SDK Manager.

The [OpenScienceJournal README](https://github.com/google/science-journal/tree/master/OpenScienceJournal)
contains details about the organization of the source code, and the relationship of this published source
to the [published app][play-store].

### Google Drive Sync

In order to enable Google Drive Sync, you must first create a Google API project and enable the Drive API 
for your app. See instructions in the 
[Google Drive API documentation](https://developers.google.com/drive/api/v2/enable-drive-api)

## Release names

We have fun choosing names for our releases.  Read the [stories][releasenames].

## Contributing

Please read our [guidelines for contributors][contributing].

## License

Open Science Journal is licensed under the [Apache 2 license][license].

## More

Science Journal is brought to you by [Making & Science][making-science], an initiative by Google. Open Science Journal is not an official Google product.

[play-store]: https://play.google.com/store/apps/details?id=com.google.android.apps.forscience.whistlepunk
[firmware-github]:https://github.com/google/science-journal-arduino
[contributing]: https://github.com/google/science-journal/blob/master/CONTRIBUTING.md
[releasenames]: https://github.com/google/science-journal/blob/master/RELEASES.md
[license]: https://github.com/google/science-journal/blob/master/LICENSE
[making-science]: https://makingscience.withgoogle.com
