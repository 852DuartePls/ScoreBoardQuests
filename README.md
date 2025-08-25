# ScoreBoardQuests 
### An infinite quests scoreboard plugin

## *Commands:*

**Legend:**
> - Required: ()
> - Optional: <>
> - With permission only: [] , *
> - Flags: -s (silent) no message will be outputted

### * /sbquests reload 
Reload the plugin messages and texts.

### * /sbquests pay (player) (amount) <-s>
Processes a player's payment.

### * /sbquests forcecomplete (player) <quantity> <-s>
Immediately completes the player's current quest and optionally increases their streak by the specified quantity.
If none was specified, the streak will be increased by 1.

### /sbquests toggle [\<player>]
Toggles the visibility of the quests' scoreboard.
By default, the command will toggle the visibility of the scoreboard for the sender.

> [!NOTE]
> The base admin permission is `scoreboardquests.admin`.
> All other commands are enabled by default.

## Config — Messages
Located in `config.yml`

### Title:
Text that will be displayed as the title of the scoreboard. (Top text)

### Plugin_Prefix:
The plugin prefix that will be shown in chat.

Example: `[ScoreBoardText] Message`

### Bottom_Message:
Text that will be displayed at the bottom of the scoreboard.