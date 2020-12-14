/**
 *
 */
//initial data
const inputField = document.getElementById("chat-input");
const outputArea = document.getElementById("chat-area");
const inviteField = document.getElementById("invite-input");
const invitePeople = document.getElementById("invite_people");
const title = document.getElementById("title");
const socketRoute = document.getElementById("ws-route").value;
const socket = new WebSocket(socketRoute.replace("http","ws"));

var doc = ["All"];
var chatroom = new Map();
chatroom.set("All","")
var activeUser = []//store all active user

var receiver = "To:All"


//send message when key enter down
inputField.onkeydown = (event) => {
    if(event.key ==="Enter"){
        socket.send(inputField.value+receiver);
        inputField.value="";
    }
    document.getElementById("error").style.display = "none";

}
//send message when press enter
function sendM(){
    socket.send(inputField.value+receiver);
    inputField.value="";
    document.getElementById("error").style.display = "none";

}
//change add different chatter
function sendR(){
    document.getElementById("error").style.display = "none";
    if(activeUser.includes(inviteField.value)){//if that user are online, add it to the user
        doc.push(inviteField.value);
        chatroom.set(inviteField.value,"");
        inviteField.value=""
        //redraw all the chat room
        var str = "<ul>"
        doc.forEach(function(slide) {
          str += '<button id='+slide+ ' onclick="change(this.id)">'+ slide + '</button>';
        });
        str += '</ul>';
        document.getElementById("Container").innerHTML = str;
    }
    else{
      document.getElementById("error").style.display = "block";//display error message

    }
    }

//function that change chatroom
function change(input){
    document.getElementById("error").style.display = "none";
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

    var list = event.data.split(",")
    var work = list.shift()
    if(work == "ActiveUser"){//if it is update user request
        activeUser = list
        var str = "<ul>"
        list.forEach(function(slide) {
            str += '<ul>'+ slide + '</ul>';
        });
        str += '</ul>';
        document.getElementById("ActiveUser").innerHTML = str;


    }
    else{//if it is msg
        var string = event.data.split("To:");
        var name = string.pop()
        var username = title.innerHTML.split("'").shift()
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
        else if (senderName == username){
            doc.push(name)
            var str = "<ul>"
            doc.forEach(function(slide) {
                str += '<button id='+slide+ ' onclick="change(this.id)">'+ slide + '</button>';
            });
            str += '</ul>';
            document.getElementById("Container").innerHTML = str;
            chatroom.set(name,event.data.substring(0,event.data.length-name.length-3))
                    }
            else{//if it is from someone you not know yet
                doc.push(senderName)
                var str = "<ul>"
                doc.forEach(function(slide) {
                  str += '<button id='+slide+ ' onclick="change(this.id)">'+ slide + '</button>';
                });
                str += '</ul>';
                document.getElementById("Container").innerHTML = str;
                chatroom.set(senderName,event.data.substring(0,event.data.length-name.length-3))
            }

        }
    }

}