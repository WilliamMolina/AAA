/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var wsURI = 'ws://' + document.location.host + document.location.pathname + 'subscribe'
        , websocket = new WebSocket(wsURI)
        , output = document.getElementById('output')
        , play = document.getElementById('play'),
        cities = document.getElementById('cities'),
        cityList = [],
        myTarget=-1,
        plane=document.getElementById('plane'),
        back=document.getElementById('back');
websocket.onopen = function (e) {
    onOpen(e);
};
websocket.onerror = function (e) {
    onError(e);
};
websocket.onmessage = function (e) {
    onMessage(e);
};
// Envio de datos a traves del websocket
function sendData(data) {
    // Los datos son un objeto de javascript, se convierte a un JSON
    websocket.send(JSON.stringify(data));
}
function onOpen(e) {
    writeToScreen('Conectado a ' + wsURI);
}
function onError(e) {
    writeToScreen('<span style="color: red;">Error: </span>' + e.data);
}
function onMessage(e) {

    var data = JSON.parse(e.data);
    switch (data.type) {
        case 'assignCity':
            back.style.backgroundImage="url('"+data.city.img+"')";
            back.style.backgroundSize="100% 100%";
        break;
        case 'simple':
            writeToScreen(data.message);
            break;
        case 'list':
            cityList = [];
            cities.innerHTML = "";
            for (var i = 0; i < data.cities.length; i++) {
                var c = document.createElement("button");
                var text = document.createTextNode(data.cities[i].name);
                c.onclick=function(evt){
                    sendData({'message':evt.target.textContent,'type':'target'});
                    myTarget=cityList.indexOf(evt.target.textContent);
                };
                c.appendChild(text);
                cities.appendChild(c);
                cityList.push(data.cities[i].name);
            }
            break;
        case 'newCity':
            var index = cityList.indexOf(data.message);
            if (index === -1) {
                var c = document.createElement("button");
                var text = document.createTextNode(data.message);
                c.onclick=function(evt){
                    sendData({'message':evt.target.textContent,'type':'target'});
                    myTarget=cityList.indexOf(evt.target.textContent);
                };
                c.appendChild(text);
                cities.appendChild(c);
                cityList.push(data.message);
            }
            break;
        case 'drop':
            var index = cityList.indexOf(data.message);
            if (index !== -1) {
                cities.removeChild(cities.childNodes[index]);
                cityList.splice(index,1);
                if(myTarget===index){
                    myTarget=-1;
                    sendData({'message':'','type':'clearTarget'});
                    alert("You must change your target");                    
                }
                
            }
            break;
        case 'assigned':
        case 'winner':
        case 'loser':
                alert(data.message);
            break;
        case 'move':
            var px=parseInt(data.message);
            plane.style.left=(px+10)+"px";
            if(px+10>window.innerWidth){
                sendData({'message':'','type':'reuse'});
            }
            else if(px+160>=window.innerWidth){
                sendData({'message':'','type':'delegate'});
            }
            break;
        case 'points':
            var p=data.points;
            var l=p.length;
            for(var i=0;i<l;i++){
                writeToScreen('User '+(i+1)+' : '+p[i]);
            }
            break;
    }
}
// Muestra un mensaje en el div "output"
function writeToScreen(message) {
    output.innerHTML += message + '<br />';
}
window.onload = function () {
    play.onclick = function (evt) {
        if(myTarget!==-1){
            sendData({'message':evt.target.textContent,'type':'play'});
        }else{
           alert("You must select a target city");
        }       
    };
};
