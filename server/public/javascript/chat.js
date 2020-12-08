/**
 *
 */

const inputField = document.getElementById("chat-input");
const outputArea = document.getElementById("chat-area");
const inviteField = document.getElementById("invite-input");
const invitePeople = document.getElementById("invite_people");
const username = document.getElementById("username");
const socketRoute = document.getElementById("ws-route").value;
const socket = new WebSocket(socketRoute.replace("http","ws"));

var doc = ["All"];
var chatroom = new Map();
chatroom.set("All","")

var receiver = "To:All"


//window.onload = console.log(localStorage.getItem("storageName").value);

inputField.onkeydown = (event) => {
    if(event.key ==="Enter"){
        socket.send(inputField.value+receiver);
        inputField.value="";
    }
}

function sendM(){
    //username+": "+
//    console.log(username.value)
    socket.send(inputField.value+receiver);
    inputField.value="";

}
//change add different chatter
function sendR(){
//    receiver="To:"+inviteField.value;
    doc.push(inviteField.value);
    chatroom.set(inviteField.value,"");

    //redraw all the chat room
    var str = "<ul>"
    doc.forEach(function(slide) {
      str += '<button id='+slide+ ' onclick="prints(this.id)">'+ slide + '</button>';
    });
    str += '</ul>';
    document.getElementById("Container").innerHTML = str;

}

//function that change chatroom
function prints(input){

    var current = receiver.substring(3,receiver.length)
    invitePeople.innerHTML  = "You are talk to: "+input;
    inviteField.value="";
    receiver="To:"+input;
    chatroom.set(current,outputArea.value)
    outputArea.value = chatroom.get(input)

}

//show you have connected
socket.onopen = (event) => socket.send("I have joined the chatroom."+receiver);
//tell system you have close
window.onbeforeunload = function (event) {
    socket.send("Close")
};
//handle the message receive from server
socket.onmessage = (event) =>{
    console.log(event.data)
    var string = event.data.split("To:");
    var name = string.pop()
    console.log(string)
    if(doc.indexOf(name)>-1){//if it is to all people
        var chat = chatroom.get(name)
        chatroom.set(name, chat+"\n"+event.data.substring(0,event.data.length-name.length-3))
        if(name ==  receiver.substring(3,receiver.length)){
            outputArea.value += "\n"+ event.data.substring(0,event.data.length-name.length-3);
        }
    }
    else{
        var senderName = event.data.split(":").shift()
        console.log(senderName)
        //if it is from someone you know
        if(doc.includes(senderName)){
            var chat = chatroom.get(senderName)
            chatroom.set(senderName,chat+"\n"+event.data.substring(0,event.data.length-name.length-3))
            if(senderName ==  receiver.substring(3,receiver.length)){
                        outputArea.value += "\n"+ event.data.substring(0,event.data.length-name.length-3);
                    }
        }

        else{//if it is from someone you not know yet
            doc.push(senderName)
            var str = "<ul>"
            doc.forEach(function(slide) {
              str += '<button id='+slide+ ' onclick="prints(this.id)">'+ slide + '</button>';
            });
            str += '</ul>';
            document.getElementById("Container").innerHTML = str;
            chatroom.set(senderName,event.data.substring(0,event.data.length-name.length-3))

        }

    }




}