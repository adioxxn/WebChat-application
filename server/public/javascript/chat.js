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

var receiver = "To:All"
//function login(){
//    const username =  document.getElementById("LoginName");
//    const password = document.getElementById("LoginPass");
//
//
//}

//window.onload = console.log(localStorage.getItem("storageName").value);

inputField.onkeydown = (event) => {
    if(event.key ==="Enter"){
        socket.send(inputField.value+receiver);
        inputField.value="";
    }
}

function sendM(){
    socket.send(inputField.value+receiver);
    inputField.value="";



}

function sendR(){
    receiver="To:"+inviteField.value;
    console.log(invitePeople)
    invitePeople.innerHTML  = "You are talk to: "+inviteField.value;
    inviteField.value="";

}




socket.onopen = (event) => socket.send("I am connected."+receiver);

socket.onclose = (event) => socket.emit('news', { hello: 'world' });


socket.onmessage = (event) =>{
    outputArea.value += "\n"+ event.data;
}