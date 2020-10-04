# Mimic

Mimic Bot is a discord bot built originally for [The Programmer's Hangout](https://theprogrammershangout.com/) that creates sentences based on it's users.

## Add me!

[Invite me!](https://discord.com/api/oauth2/authorize?client_id=751109806722383902&permissions=329728&scope=bot)

### My required permissions

* Read Message History
* Send Messages
* Use External Emojis

## Features

* Generates sentences using a markov chain 
* Users can opt-in/opt-out
* Admins can add/remove/list channels with customised permissions per channel

### Commands

Command | Description | Example
------------ | ------------- | ------------- 
about | Displays info about the bot | `mimic!about`
all | Generate a random number of sentences from all opted in user messages | `mimic!all`
allstats | Display statistics for all users | `mimic!allstats`
channels | Lists all readable channels | `mimic!channels`
channels.add | Adds a channel. Default to read only access | `mimic!channels add 123456789 -r -w`
channels.edit | Edits added channel permissions. Collects user message history if read permission granted and deletes it if revoked. | `mimic!channels edit 123456789`
channels.full | Lists all channels registered | `mimic!channels full`
channels.remove | Removes a channel from the database. All related messages are also removed. | `mimic!channels remove 123456789`
help | show all commands or detailed help of one command | `mimic!help` or `mimic!help about`
opt-in | Opt-in for your messages to be read | `mimic!opt-in`
opt-out | Opt-out for all messages to be removed | `mimic!opt-out`
rand | Generate a random number of sentences from random user's messages! | `mimic!rand`
self | Generate a random number of sentences from your own messages! | `mimic!self` 
stats | Display statistics of your messages | `mimic!stats`

