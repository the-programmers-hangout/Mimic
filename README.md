# Mimic

Mimic Bot is a discord bot built originally for [The Programmer's Hangout](https://theprogrammershangout.com/) that creates sentences based on it's users.

## Add me!

Invite link coming soon!

Server admins/owners will need to run the configuration command before using the bot. If you want to lock the `mimic!opt-in` command behind a role use `mimic!config -o yourRoleName`. If you do not want to lock the opt-in command behind a role use `mimic!config`.

### My required permissions

* Read Message History
* Send Messages
* Use External Emojis

## Features

* Generates sentences using a markov chain 
* Users can opt-in/opt-out
* Admins can add/remove/list channels with customised permissions per channel

### Commands

[Visit the commands page!](COMMANDS.md)

### Privacy

Mimic is explicitly opt-in. 
* Messages from users that are not opted-in will not be stored.
* Message edits from opted-in users will be reflected in the database.
* Messages that are deleted by opted-in users will be deleted from the database.
* Opting out will remove all user messages and user details from the database.
* Users that are opted-in and leave a server will have their details and messages deleted.