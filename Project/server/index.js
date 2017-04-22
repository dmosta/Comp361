var app = require('express')();
var http = require('http').Server(app);
var io = require('socket.io')(http);
var connected={};
var games={};

function checkFormat(a, b){
    if(a!=null && typeof a === 'object' && b!=null && typeof b ==='object'){
        var keysA=Object.keys(a).sort();
        var keysB=Object.keys(b).sort();
        if(keysA.length!=keysB.length)
            return false;
        var ret=true;
        for(var i=0;i<keysA.length;i++){
            if(keysA[i]!=keysB[i])
                ret=false;
        }
        return ret;
    }else return false;
}

app.get('/', function(req, res){
    res.sendfile('index.html');
});

io.on('connection', function(socket){//peer connected
    var player={};
    player.valid=false;
    player.ingame=false;
    player.slot=-1;
    player.socket=socket;
    socket.on('disconnect', function () {
        if(player.valid){
            var hadGame=socket.id in games;
            if(player.ingame){
                var game=player.joined;
                for(var i=0;i<game.players.length;i++){
                    var other=game.players[i];
                    other.ingame=false;
                    other.socket.emit("player_left");
                }
            }
            delete games[socket.id];
            delete player.game;
            for(var socketID in connected){
                if(socketID !=socket.id){
                    connected[socketID].socket.emit("disconnected", {name:player.name, id:socket.id});
                    if(hadGame)
                        connected[socketID].socket.emit("cancel_game", {id:socket.id});
                }
            }
            console.log("disconnected");
            delete connected[socket.id];
        }
    });

    //For first connection we validate data to ensure it is sent from a client
    var lobbyFormat={name:"", hash:"", fingerprint:""};
    socket.on('lobby', function (data) {//peer wants to join lobby
        if(checkFormat(lobbyFormat, data) && data.hash==422010402){
            console.log("name: "+data.name+" id "+socket.id);
            player.name=data.name;
            player.valid=true;
            player.fingerprint=data.fingerprint;
            connected[socket.id]=player;
            var players=[];
            var gameList=[];
            for(var socketID in connected){
                if(socketID!=socket.id){
                    var other=connected[socketID];
                    other.socket.emit('player', {name:player.name, id:socket.id, fingerprint:player.fingerprint});
                    players.push({name:other.name, id:other.socket.id, fingerprint:other.fingerprint, slot:other.slot});
                }
            }
            for(var gameID in games){
                var game=games[gameID];
                var gamePlayers=[];
                for(var i=0;i<game.players.length;i++)
                    gamePlayers.push(game.players[i].socket.id);
                gameList.push({numPlayers:game.numPlayers, id:game.host.socket.id, password:game.password, preset:game.preset,
                    map:game.map, connected:game.players.length, players:gamePlayers, saved:game.saved, slots:game.slots, slot:game.slot, victoryPoints:game.victoryPoints});
            }
            socket.emit('players', {games:gameList, players:players, id:socket.id});
        }
    });

    socket.on('game', function(game){//peer wants to start a new game
        if(player.valid){
            game.host=player;
            game.players=[player];
            player.joined=game;
            player.game=game;
            player.slot=game.slot;
            games[socket.id]=game;
            for(var socketID in connected){
                if(socketID != socket.id){
                    connected[socketID].socket.emit('game', {numPlayers:game.numPlayers, id:socket.id, password:game.password,
                        map:game.map, preset:game.preset, players:[socket.id], saved:game.saved, slots:game.slots, slot:game.slot, victoryPoints:game.victoryPoints});
                }
            }
        }
    });

    socket.on('cancel_game', function(data){
        console.log("cancel_game");
        if(player.valid && socket.id in games){
            delete player.game;
            delete player.joined;
            for(var socketID in connected){
                if(socket.id!=socketID){
                    var peer=connected[socketID];
                    peer.socket.emit("cancel_game", {id:socket.id});
                }
                for(var i=0;i<games[socket.id].players.length;i++){
                    var other=games[socket.id].players[i];
                    delete other.joined;
                }
            }
            delete games[socket.id];
        }
    });

    socket.on('join', function(data){//peer wants to join a game
        if(player.valid){
            var game=connected[data.id].game;
            console.log(game.saved);
            if(game.saved){
                player.slot=data.slot;
                for(var i=0;i<game.slots.length;i++){
                    if(game.slots[i]==player.slot){
                        game.slots.splice(i, 1);
                        break;
                    }
                }
            }
            if(game.players.length<game.numPlayers){
                player.joined=game;
                for(var socketID in connected)
                    connected[socketID].socket.emit("join", {id:data.id, peer:socket.id, slot:data.slot});
                game.players.push(player);
            }
        }
    });
    socket.on('start_game', function(data){
        if(player.valid){
            var game=player.game;
            for(var socketID in connected)
                connected[socketID].socket.emit('start', {id:socket.id});
        }
    });

    socket.on('cancel_join', function(data){//peer cancelled joining a game
        if(player.valid && data.id in games){
            var game=games[data.id];
            if(game.saved){
                game.slots.push(player.slot);
            }
            delete player.joined;
            for(var i=0;i<game.players.length;i++){
                if(game.players[i]==player){
                    game.players.splice(i, 1);
                    break;
                }
            }
            for(var socketID in connected)
                connected[socketID].socket.emit('cancel_join', {id:data.id, peer:socket.id, slot:data.slot});
        }
    });
    socket.on("ready", function(data){
        if(player.valid){
            var game=player.joined;
            var others=[];
            player.ingame=true;
            var count=0;
            for(var i=0;i<game.players.length;i++){
                if(game.players[i].ingame){
                    others.push(game.players[i].socket.id);
                    count++;
                }
            }
            if(count==game.players.length-1)
                game.resetting=false;
            for(var i=0;i<game.players.length;i++)
                game.players[i].socket.emit('ready', others);
        }
    });
    socket.on("reset_game", function(data){
        if(player.valid){
            var game=player.joined;
            game.resetting=true;
            for(var i=0;i<game.players.length;i++){
                var other=game.players[i];
                other.ingame=false;
                other.socket.emit("reset_game");
            }
        }
    });
    socket.on('catan', function(data, ack){
        if(player.valid && !player.joined.resetting){
            var game=player.joined;
            var answers=0;
            if(!('global' in data))
                answers++;
            var needAck=(typeof ack === 'function');
            for(var i=0;i<game.players.length;i++){
                if(game.players[i]!=player || ('global' in data))
                    game.players[i].socket.emit('catan', data, function callback(){
                        answers++;
                        if(needAck && game.players.length==answers)
                            ack();
                    });
            }
        }
    });

});
http.listen(3000, function(){
    console.log('listening on *:3000');
});