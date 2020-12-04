/**
 *
 */

const inputField = document.getElementById("chat-input");
const outputArea = document.getElementById("chat-area");



const socketRoute = document.getElementById("ws-route").value;
const socket = new WebSocket(socketRoute.replace("http","ws"));


//function login(){
//    const username =  document.getElementById("LoginName");
//    const password = document.getElementById("LoginPass");
//
//
//}

//window.onload = console.log(localStorage.getItem("storageName").value);

inputField.onkeydown = (event) => {
    if(event.key ==="Enter"){
        socket.send(inputField.value);
        inputField.value="";
    }
}

function sendM(){
    socket.send(inputField.value);
    inputField.value="";

}



socket.onopen = (event) => socket.send("New user connected.");

socket.onmessage = (event) =>{
    outputArea.value += "\n"+ event.data;
}