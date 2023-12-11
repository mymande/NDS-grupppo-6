# Evaluation lab - Node-RED

## Group number: 06

## Group members

- Andrea Carbonetti
- Federico Mandelli
- Pasquale Scalise

## Description of message flows
Once a telegram message is received it is passed to a parser responsible of decoding the message, the parser is composed of 3 function node that respectively decode the type of request, the city and the date of the forecast. Each node of the parser add in a corresponding field the parsed information consuming the part of the string and setting each step the correct field for an openWeather query sending it to the OpenWeather node. Each node, if it is unable to recognize a string, sends an error message to the user. After a response is received it is sent to a function node responsible of formatting the response string. The response string is then added to the message and through a text node is sent to the Telegram Sender node that sends it to the corresponding chat.

## Extensions 

## Bot URL 
@node_red_lab_group_06_bot
Telegram bot API: 5800924072:AAGSgsWSSep45ISX7ti1id8lWKCZZfMTzNk
