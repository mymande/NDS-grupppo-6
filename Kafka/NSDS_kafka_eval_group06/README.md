# Evaluation lab - Apache Kafka

## Group number: 06

## Group members

- Andrea Carbonetti
- Federico Mandelli
- Pasquale Scalise

## Exercise 1

- Number of partitions allowed for inputTopic (1, n)
- Number of consumers allowed (1, m)
- Consumer can be of the same group or of different groups, if m consumer are in the same group and m>n some consumers are idle
    - Consumer 1: <GroupA>
    - Consumer 2: <GroupA>
    - ...
    - Consumer m: <GroupA>

## Exercise 2

- Number of partitions allowed for inputTopic (1, n)
- Number of consumers allowed (1, m)
- Each of the m consumer needs to be in its own group, even different from the AtMostOncePrinter
    - Consumer 1: <GroupB>
    - Consumer 2: <GroupC>
    - ...
    - Consumer n: <GroupN>