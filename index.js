//Logger first
const logger = require("log4js").getLogger("MinecraftCapes");
logger.level = "debug";
logger.info("Loading libraries...")

//Check env
const fs = require('fs')
try {
    if (!fs.existsSync('.env')) {
        fs.copyFileSync('.env.example', '.env');
    }
} catch (err) {
    logger.error("Could not read .env file");
    process.exit();
}

//STDIN Reader
const readline = require('readline');
var rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
    terminal: false
});

//Loads Libraries
require('dotenv').config()
const mc = require('minecraft-protocol');

/**
 * Start the MC Server
 */
logger.info("Starting MinecraftCapes Auth...");
const server = mc.createServer({
    host: process.env.SERVER_IP,
    port: process.env.SERVER_PORT,
    maxPlayers: 1,
    motd: process.env.MOTD,
    encryption: process.env.ENCRYPTION,
    'online-mode': process.env.ONLINE_MODE
});
logger.info("Started MinecraftCapes Auth on", process.env.SERVER_IP + ":" + process.env.SERVER_PORT);

/**
 * Handle client connections
 * Any error will result in a "failed to verify username"
 */
server.on('login', function (client) {
    logger.info(client.username, "requested auth code")
    client.end(
        "§8§l§m===============================\n\n" +
        `${getAuthCode(client.uuid)}` +
        "\n\n§8§l§m===============================\n" +
        "\n§bJoin our Minecraft Server" +
        "\n§a\u25A0 §ePlay.CapeCraft.Net §a\u25A0"
    );
})

/**
 * Get the auth code
 * @param {UUID} uuid
 */
function getAuthCode(uuid) {
    let errorMessage = "§c§lSomething went wrong\nPlease reconnect to try again"
    try {
        let blockedMessage = "§c§lThe specified account has been banned for violating our terms of service."

        uuid = uuid.replace("-", "");

        let authCode = "123456"
        let authMessage = `§fYour authorization code is\n§c§l\u00BB§f ${authCode} §c§l\u00AB`;

        return authMessage;
    } catch (error) {
        logger.error(error);
        return errorMessage;
    }
}

/**
 * Command handler
 */
rl.on('line', function (line) {
    line = line.toLocaleLowerCase();
    switch(line) {
        case "stop":
            logger.info("Stopping MinecraftCapes Auth...")
            server.close()
            process.exit();
    }
})