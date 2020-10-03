# Contributing Guidelines

Before beginning, please make sure you have read and understood the [Code of Conduct](CODE_OF_CONDUCT.md)

## What to work on

All work should be decided on before you begin working. Either through DMs on Discord (Toby Larone#1985) or through Issues in this repository. Please don't expect that unorganised, undiscussed, or random PRs will be accepted.

## Setup

* Create a discord bot, you'll need to allow the "Server Member Intent" from the bot configuration page.
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
* If making changes to the database, add a new migration file. Run the bot normally (so flyway runs the migrations), then run the bot again and pass `--generate` to the main method so that Jooq generates the relevant database changes.

Note: for more info on commands and command parsing see [Disparse](https://github.com/BoscoJared/disparse)

## Committing changes

Try to keep commits small in size. Many small commits are much nicer than few large commits.

Commit messages should follow this format `[module] lowercase context for commit`
`[module]` Can represent the area being worked on. Examples include:
* `[db]` For database changes
* `[command]` For command changes
* `[meta]` For meta repository changes (e.g. readme)
* `[markov]` For markov implementation specific changes

If in doubt, just ask.

If you have WIP or incomplete commits, squash them. If you have commits doing too many things that aren't necessary, split them into separate PRs (probably with a different issue). Keep commits as small as it needs to be to satisfy an issue.

### Merging

All code must be rebased against current master and able to be fast-forward merged on-top of master. There are no merge commits, and master is a completely linear history where every single commit is able to be built, tested, and used.


## Contact

Preferably you can contact me through Discord (Toby Larone#1985 - You can find me on [JVMRally](https://discord.gg/MuZKQWM) or [The Programmer's Hangout](https://discord.gg/programming)), or [email me](mailto:mark@markg.co.uk?subject=[Github]%20Mimic%20bot)