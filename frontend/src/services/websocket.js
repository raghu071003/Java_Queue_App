import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

export const connectQueueSocket = (
  doctorId,
  onMessage
) => {

  const socket = new SockJS(
    "http://localhost:8080/ws"
  );

  const client = new Client({

    webSocketFactory: () => socket,

    reconnectDelay: 5000,

    onConnect: () => {

      console.log("Connected");

      client.subscribe(
        `/topic/queue/${doctorId}`,
        (message) => {

          const data = JSON.parse(
            message.body
          );

          onMessage(data);
        }
      );
    }
  });

  client.activate();

  return client;
};