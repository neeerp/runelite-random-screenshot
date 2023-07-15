# Random Screenshot
A RuneLite plugin that takes random screenshots as you go about your adventures.

You can find your randomly taken screenshots under the `Random Screenshots` folder in your RuneLite Screenshots folder.

Feel free to suggest improvements/features on this repository's issues page, or submit a pull request!

## Configuration
Every game tick, there is a 1 in `n` chance that a screenshot is taken (with `n` configurable via the "Sample Weight"
option). _This does not guarantee a screenshot will be taken every `n` ticks! It's random!_ Moreover, the roll to take a
screenshot does not happen whenever the bank pin UI is open.

For convenience, here's a table with the number of ticks in a given interval of real time.


| Real Time        | Ticks  |
|------------------|--------|
| 600 milliseconds | 1      |
| 6 seconds        | 10     |
| 30 seconds       | 50     |
| 1 minute         | 100    |
| 5 minutes        | 500    |
| 10 minutes       | 1000   |
| 15 minutes       | 1500   |
| 30 minutes       | 3000   |
| 1 hour           | 6000   |
| 6 hours          | 36000  |
| 12 hours         | 72000  |
| 1 day            | 144000 |

## Attributions
### Core Screenshot Plugin
This was built atop of the core Screenshot plugin, which was lifted and stripped down in `ScreenshotUtil.java`.

### Plugin Icon
<a href="https://www.flaticon.com/free-icons/ui" title="ui icons">Ui icons created by mim_studio - Flaticon</a>
