# Bertrand

Bertrand(Mimic Bot) is a discord bot built for [The Programmer's Hangout](https://theprogrammershangout.com/) that creates sentences based on it's users.

## Features

* Generates sentences using a markov chain 
* Users can opt-in/opt-out
* Admins can add/remove/list channels with customised permissions per channel

Note: for more info on commands and command parsing see [Disparse](https://github.com/BoscoJared/disparse)

## Setup

* Requires Postgres, Maven, Java 11+
* Create database with `CREATE DATABASE my_database`
* Create the following environment vars. (Replace placeholder values with your own)
```
B_HOST=jdbc:postgresql://localhost:5432/my_database
B_USER=my_user
B_PASS=my_password
B_TOKEN=my_discord_token
```
* Run bot via main.
* If making changes to the database, add a new migration file. Run the bot normally, then run the bot again and pass `--generate` to the main method.