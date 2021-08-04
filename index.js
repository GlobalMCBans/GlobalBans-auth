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

//Load Favicon
var serverIcon = "data:image/png;base64,";
try {
    if(fs.existsSync('server-icon.png')) {
        serverIcon += fs.readFileSync('server-icon.png', 'base64')
    }
} catch (err) {
    logger.error(err)
}

//STDIN Reader
const readline = require('readline');
var rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
    terminal: false
});

//Loads Libraries
const fetch = require('node-fetch');
const FormData = require('form-data');
require('dotenv').config()
const mc = require('minecraft-protocol');

/**
 * Start the MC Server
 */
var server;
startServer();
function startServer() {
    logger.info("Starting MinecraftCapes Auth...");
    server = mc.createServer({
        host: process.env.SERVER_IP,
        port: process.env.SERVER_PORT,
        maxPlayers: 1,
        motd: process.env.MOTD.replace(/&/g, '§'),
        encryption: process.env.ENCRYPTION.toLowerCase() == "true",
        'online-mode': process.env.ONLINE_MODE.toLowerCase() == "true",
        beforePing: (response, client) => {
            if(serverIcon) {
                response.favicon = serverIcon
            }
            response.version.protocol = client.version
        }
    });
    logger.info("Started MinecraftCapes Auth on", process.env.SERVER_IP + ":" + process.env.SERVER_PORT);

    /**
     * Handle client connections
     * Any error will result in a "failed to verify username"
     */
    server.on('login', client => {
        logger.info(client.username, "is requesting an auth code")

        getAuthCode(client.uuid, client.username).then(res => {
            client.end(
                "§8§l§m===============================\n\n" +
                "§a§lMinecraftCapes\n\n" +
                `${res}` +
                "\n\n§8§l§m===============================\n"
            );
            logger.info(client.username, "has been served")
        });
    })
}

/**
 * Get the auth code
 * @param {String} uuid
 * @param {String} username
 */
async function getAuthCode(uuid, username) {
    let errorMessage = "§c§lSomething went wrong\nPlease reconnect to try again"
    try {
        let blockedMessage = "§c§lYour account has been banned for violating our terms of service."

        let formData = new FormData();
        formData.append('key', process.env.API_KEY);
        formData.append('uuid', uuid.replace(/-/g, ""));
        formData.append('username', username);

        let response = await fetch(process.env.AUTH_ENDPOINT, {
            method: 'POST',
            body: formData,
            headers: formData.getHeaders()
        });
        let data = await response.json();

        let authCode = null
        if(data != null && data.success) {
            authCode = data.code
            let authMessage = `§fYour authorization code is\n§c§l\u00BB§f ${authCode} §c§l\u00AB`;
            return (authCode == null) ? errorMessage : authMessage;
        } else if(data != null && !data.success) {
            if(data.banned != null && data.banned) {
                return blockedMessage
            } else {
                return errorMessage
            }
        } else {
            return errorMessage;
        }
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
        case "restart":
            logger.info("Stopping MinecraftCapes Auth...")
            server.close()
            startServer();
    }
})