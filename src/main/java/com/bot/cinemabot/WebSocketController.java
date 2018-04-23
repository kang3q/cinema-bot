package com.bot.cinemabot;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

import com.bot.cinemabot.model.socket.Greeting;
import com.bot.cinemabot.model.socket.Message;

@RestController
public class WebSocketController {

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public Greeting broadcasting(Message message) {
        System.out.println(message.getName());
        return new Greeting(message.getName());
    }

}
