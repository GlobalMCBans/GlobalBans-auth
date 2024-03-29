//Logger first
const log4js = require("log4js");
log4js.configure({
    appenders: {
        file: { type: "file", filename: "error.log" },
        console: { type: "console" },
        warnings: { type: 'logLevelFilter', appender: 'file', level: 'warn' }
    },
    categories: { default: { appenders: ["warnings", "console"], level: "info" } }
});
const logger = log4js.getLogger(process.env.AUTH_SERVER_NAME);
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

//Load the MOTD as well
var serverMotd = process.env.AUTH_SERVER_MOTD.replace(/&/g, '§').replace(/\\u([0-9a-fA-F]{4})/g, (m,cc)=>String.fromCharCode("0x"+cc));

/**
 * Start the MC Server
 */
var server;
startServer();
function startServer() {
    logger.info("Starting " + process.env.AUTH_SERVER_NAME + "...");
    server = mc.createServer({
        host: process.env.AUTH_SERVER_IP,
        port: process.env.AUTH_SERVER_PORT,
        maxPlayers: 1,
        motd: serverMotd,
        encryption: process.env.AUTH_SERVER_ENCRYPTION.toLowerCase() == "true",
        'online-mode': process.env.AUTH_SERVER_ONLINE_MODE.toLowerCase() == "true",
        hideErrors: true,
        beforePing: (response, client) => {
            if(serverIcon) {
                response.favicon = serverIcon
            }
            response.version.protocol = client.protocolVersion
        }
    });
    logger.info("Started "+ process.env.AUTH_SERVER_NAME +" Auth on", process.env.AUTH_SERVER_IP + ":" + process.env.AUTH_SERVER_PORT);
    logger.info("---- Server Running! ----");

    /**
     * Handle client connections
     * Any error will result in a "failed to verify username"
     */
    server.on('login', client => {
        logger.info(client.username, "is requesting an auth code")

        getAuthCode(client.uuid, client.username).then(res => {
            client.emit('end');
            client.end(
                "§8§l§m===============================\n\n" +
                "§a§l" + process.env.AUTH_SERVER_NAME + "\n\n" +
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
        formData.append('key', process.env.AUTH_API_KEY);
        formData.append('uuid', uuid.replace(/-/g, ""));
        formData.append('username', username);

        let data;
        const response = await fetch(process.env.AUTH_API_ENDPOINT, {
            method: 'POST',
            body: formData,
            headers: formData.getHeaders()
        })

        if(!response.ok) {
            logger.error(`${uuid} Recieved an invalid ${response.status} response from ${process.env.AUTH_SERVER_NAME}!`)
        } else {
            data = await response.json();
        }

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
            logger.info("Stopping " + process.env.AUTH_SERVER_NAME + " Auth...")
            server.close()
            process.exit();
        case "restart":
            logger.info("Stopping " + process.env.AUTH_SERVER_NAME + " Auth...")
            server.close()
            startServer();
    }
})