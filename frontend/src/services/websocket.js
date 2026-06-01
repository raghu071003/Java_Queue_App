import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import { API_BASE_URL } from "../api/axios";

export const connectQueueSocket = (
  doctorId,
  onMessage
) => {

  const socket = new SockJS(
    `${API_BASE_URL}/ws`
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