//Require
const logger = require("log4js").getLogger("MinecraftCapes");
require('dotenv').config()
const mc = require('minecraft-protocol');
const mcData = require('minecraft-data')

//Start Logs
logger.level = "debug";
logger.info("Starting MinecraftCapes Auth...");

//Minecraft Server
const server = mc.createServer({
    host: process.env.SERVER_IP,
    port: process.env.SERVER_PORT,
    maxPlayers: 1,
    motd: process.env.MOTD,
    encryption: process.env.ENCRYPTION,
    'online-mode': process.env.ONLINE_MODE
});
logger.info("Started MinecraftCapes Auth on", server.socketServer);

server.on('login', function (client) {
    let loginPacket = mcData.loginPacket

    client.write('login', {
        entityId: client.id,
        isHardcore: false,
        gameMode: 0,
        previousGameMode: 255,
        worldNames: loginPacket.worldNames,
        dimensionCodec: loginPacket.dimensionCodec,
        dimension: loginPacket.dimension,
        worldName: 'minecraft:overworld',
        hashedSeed: [0, 0],
        maxPlayers: server.maxPlayers,
        viewDistance: 10,
        reducedDebugInfo: false,
        enableRespawnScreen: true,
        isDebug: false,
        isFlat: false
    });
    client.write('position', {
        x: 0,
        y: 1.62,
        z: 0,
        yaw: 0,
        pitch: 0,
        flags: 0x00
    });
    var msg = {
        translate: 'chat.type.announcement',
        "with": [
            'Server',
            'Hello, world!'
        ]
    };
    client.write("chat", {
        message: JSON.stringify(msg),
        position: 0,
        sender: '0'
    });
});