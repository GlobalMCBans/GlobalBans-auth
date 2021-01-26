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
} catch(err) {
    logger.severe("Could not read .env file");
    process.exit();
}

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
    'online-mode': process.env.ONLINE_MODE,
    version: '1.16.4'
});
logger.info("Started MinecraftCapes Auth on", process.env.SERVER_IP + ":" + process.env.SERVER_PORT);

/**
 * Handle client connections
 */
server.on('login', function(client) {
    logger.info(client.username, "is connected with version", client.version)

    client.end(
        "§8§l§m===============================\n\n" +
        `${getAuthCode(client.uuid)}` +
        "\n\n§8§l§m===============================\n" +
        "\n§bJoin our Minecraft Server" +
        "\n§a\u25A0 §ePlay.CapeCraft.Net §a\u25A0"
    );
});

function getAuthCode(uuid) {
    let errorMessage = "§c§lSomething went wrong\nPlease reconnect to try again"
    let blockedMessage = "§c§lThe specified account has been banned for violating our terms of service."

    let authCode = "123456"
    let authMessage = `§fYour authorization code is\n§c§l\u00BB§f ${authCode} §c§l\u00AB`;

    return authMessage;
}