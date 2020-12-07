/**
 *
 */

const socketRoute = document.getElementById("ws-route").value;
const socket = new WebSocket(socketRoute.replace("http","ws"));

function login(){
    const username =  document.getElementById("LoginName").value;
    const password = document.getElementById("LoginPass").value;
    const User = ["Login",username,password];
    socket.send(User);

}

socket.onmessage = (event) =>{
    console.log(event.data);
}