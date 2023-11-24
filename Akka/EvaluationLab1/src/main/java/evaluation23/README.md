# Evaluation lab - Akka

## Group number: 06

## Group members

- Andrea Carbonetti
- Federico Mandelli
- Pasquale Scalise

## Description of message flows

The first kind of messages is sent from the main to the Dispatcher and they tell the Dispatcher to create the Processors; then other messages tell the Dispatcher witch Processors have been created. Then the Sensors get the Dispatcher reference from the main via messages. The main tells the Sensors to generate temperature readings, and the Sensors send them to the Dispatcher. When the Dispatcher receives a reading, it forwards the message to the selected Processor depending on the policy. A message is then sent from the main to the Dispatcher to change its policy. The sensors are asked from the main for a new round of readings that is then send to the dispatcher and the Processors (as described above). A message is sent to the FaultySensor to specify the Dispatcher, then the last round of reading is generated following the above pattern.
