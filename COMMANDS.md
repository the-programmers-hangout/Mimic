# Commands
## all

Generate a random number of sentences from all opted in user messages!
* **Cooldown:** 5 seconds per user
* **Required Permission:** N/A
* **Required Role:** N/A
#### Arguments
* `-h`, `--help` : show usage of a particular command

#### Usage
## experimental

Trigram experiment. [this command may change, break, or disappear over time]
* **Cooldown:** 5 seconds per user
* **Required Permission:** N/A
* **Required Role:** N/A
#### Arguments
* `-h`, `--help` : show usage of a particular command

#### Usage
## channels.edit

Edits added channel permissions. Collects user message history if read permission granted and deletes it if revoked.
* **Cooldown:** 5 seconds per user
* **Required Permission:** BAN_MEMBERS
* **Required Role:** N/A
#### Arguments
* `-h`, `--help` : show usage of a particular command
* `-r`, `--read` : Whether the bot should read from the channel. Defaults to false
* `-w`, `--write` : Whether the bot can write to the channel. Defaults to false

#### Usage
## channels.add

Add channels. Allows configurable read and write permissions.
* **Cooldown:** 5 seconds per user
* **Required Permission:** BAN_MEMBERS
* **Required Role:** N/A
#### Arguments
* `-h`, `--help` : show usage of a particular command
* `-r`, `--read` : Whether the bot should read from the channel. Defaults to false
* `-w`, `--write` : Whether the bot can write to the channel. Defaults to false

#### Usage
## about

Displays info about the bot
* **Cooldown:** 60 seconds per user
* **Required Permission:** N/A
* **Required Role:** N/A
#### Arguments
* `-h`, `--help` : show usage of a particular command

#### Usage
## start

Provide the start of a sentence and let mimic finish it! Use quotations around your sentence!
* **Cooldown:** 5 seconds per user
* **Required Permission:** N/A
* **Required Role:** N/A
#### Arguments
* `-h`, `--help` : show usage of a particular command

#### Usage
* "I'm really" -> Tells mimic to start generating a sentence with "I'm really"
## channels

Lists all read-only channels registered
* **Cooldown:** 5 seconds per user
* **Required Permission:** N/A
* **Required Role:** N/A
#### Arguments
* `-h`, `--help` : show usage of a particular command

#### Usage
## channels.full

Lists all channels registered
* **Cooldown:** 5 seconds per user
* **Required Permission:** BAN_MEMBERS
* **Required Role:** N/A
#### Arguments
* `-h`, `--help` : show usage of a particular command

#### Usage
## help

show all commands or detailed help of one command
* **Cooldown:** N/A
* **Required Permission:** N/A
* **Required Role:** N/A
#### Arguments
* `-p`, `--page` : select a specific page to showcase

#### Usage
## rand

Generate a random number of sentences from random user's messages!
* **Cooldown:** 5 seconds per user
* **Required Permission:** N/A
* **Required Role:** N/A
#### Arguments
* `-h`, `--help` : show usage of a particular command

#### Usage
## allstats

Display statistics for all users
* **Cooldown:** 60 seconds per channel
* **Required Permission:** N/A
* **Required Role:** N/A
#### Arguments
* `-h`, `--help` : show usage of a particular command

#### Usage
## stats

Display statistics of your messages
* **Cooldown:** 5 seconds per user
* **Required Permission:** N/A
* **Required Role:** N/A
#### Arguments
* `-h`, `--help` : show usage of a particular command

#### Usage
## opt-in

Opt-in for your messages to be read.
* **Cooldown:** 10 seconds per user
* **Required Permission:** N/A
* **Required Role:** N/A
#### Arguments
* `-h`, `--help` : show usage of a particular command

#### Usage
## opt-out

Opt-out for all messages to be removed.
* **Cooldown:** 10 seconds per user
* **Required Permission:** N/A
* **Required Role:** N/A
#### Arguments
* `-h`, `--help` : show usage of a particular command

#### Usage
## channels.remove

Remove channels from the database. All related messages are also removed.
* **Cooldown:** 5 seconds per user
* **Required Permission:** BAN_MEMBERS
* **Required Role:** N/A
#### Arguments
* `-h`, `--help` : show usage of a particular command

#### Usage
## config

Setup per-server config
* **Cooldown:** 5 seconds per user
* **Required Permission:** BAN_MEMBERS
* **Required Role:** N/A
#### Arguments
* `-h`, `--help` : show usage of a particular command
* `-o`, `--opt` : Set the required role for using the opt-in command

#### Usage
## self

Generate a random number of sentences from your own messages!
* **Cooldown:** 5 seconds per user
* **Required Permission:** N/A
* **Required Role:** N/A
#### Arguments
* `-h`, `--help` : show usage of a particular command

#### Usage
