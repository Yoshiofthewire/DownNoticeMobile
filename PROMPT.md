This is a Mobile app that monitors RSS feeds for various cloud providors and offers realtime data on Cloud Outages.
There should be notification when an update is pushed for each outage.
Opening the interface Should provide a time based list of what is currently down.
You can tap into each application to see just that application's notices
opening the setting button should provide 
1. an interface to add new rss, with options to select a name, icon and any updates
2. setting for refresh rate, light mode, dark mode, system color, etc.
3. about

Example RSS:
https://azure.status.microsoft/en-us/status/feed/
https://status.aws.amazon.com/rss/all.rss
https://status.cloud.google.com/en/feed.atom
https://www.githubstatus.com/history.rss
https://www.cloudflarestatus.com/history.atom

Technology:
Please develop this for Android.

Design:
The notification icon should change for current status.
Green - All Good
Yellow - Degraded
Red - Down
Black - Failed to fetch / parse feed
Use OS Native notifications
Please use a bottom nav with pages
1. Current Notices By Sevarity then date
2. Please use a Mobile Reflexive desgine based on the app in the DownNoticeDesktop folder  

Functional:
The Polling default should be 15 min, but should be changeable in the settings.
History should go back 48 hours
parce all feeds provided
The app should auto-start on phone startup

Icon:
Please provide cloud provider icons and a generic set for unknown providers

About:
Please provide the name of the application
version number
build date
a scroling text box with a GPL 2

Examples of Text in Each Status:

Down:
Disruption, Unavalible, Down, Interupted, unavalible, outage

Degraded:
Degraded, intermittent, issues

Green:
Resolved, Completed, SCHEDULED

Items with a future date should be Green

